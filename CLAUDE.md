# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# 编译打包
mvn clean package

# 运行（主类: cn.org.hentai.simulator.app.SimulatorApp）
mvn spring-boot:run

# 单独运行 jar
java -jar target/jtt808-simulator-1.0-SNAPSHOT.jar
```

前置依赖：JDK 17、MySQL（库名 `simulator`）、Netty 服务端（默认 127.0.0.1:20021）。
数据库表在应用启动时由 `schema.sql` 自动创建（`CREATE TABLE if not exists`）。

应用端口：18888（Web 管理界面）。

## Architecture

JT/T 808 部标协议终端模拟器。模拟车辆通过 Netty TCP 连接到部标服务器，上报位置、处理下发指令。目标是 10 万辆车并发在线的压力测试。

### 包结构（DAG 分层）

```
cn.org.hentai.simulator
├── domain/                # 领域层（纯数据，无业务逻辑，零依赖）
│   ├── entity/            # 数据库实体（Route, RoutePoint, StayPoint 等）
│   ├── model/             # 内存业务模型（DrivePlan, Point, TaskInfo, XRoute 等）
│   └── enums/             # 枚举（TaskState, TaskStatus, LogType）
├── infrastructure/        # 基础设施层
│   ├── persistence/       # 数据持久化
│   │   ├── mapper/        # MyBatis Mapper 接口
│   │   └── example/       # MyBatis Example 查询类
│   └── util/              # 工具类
├── service/               # 服务层（业务逻辑）
│   ├── RouteManager       # 线路缓存与行驶计划生成
│   ├── TaskManager        # 任务生命周期管理
│   ├── ScheduleTaskManager # 定时任务管理
│   └── *Service           # CRUD 服务
├── web/                   # Web 接口层
│   ├── controller/        # Spring MVC 控制器
│   └── vo/                # 视图对象（Page, Result）
├── engine/                # 模拟引擎
│   ├── core/              # AbstractDriveTask, SimpleDriveTask
│   ├── event/             # 事件系统（EventDispatcher, @Listen）
│   ├── net/               # Netty 连接池（ConnectionPool）
│   ├── runner/            # 线程池（RunnerManager）
│   └── log/               # 引擎日志
└── app/                   # 启动入口（SimulatorApp）
```

DAG 依赖方向：`domain ← infrastructure ← engine ← service ← web`

### Core Simulation Engine（engine）

事件驱动 + EventLoop 线程模型：

- **AbstractDriveTask** — 模拟任务抽象基类，生命周期方法 `init()` → `startup()`，状态机 idle → driving → parking → terminated
- **SimpleDriveTask** — 具体实现：JTT808 协议通信、设备注册/鉴权、位置上报（T0200，5 秒间隔）
- **EventDispatcher** + `@Listen` 注解 — 事件路由与分发，支持 attachment 参数按消息 ID 二次路由
- **RunnerManager** — ScheduledExecutorService 线程池调度，执行事件回调与定时/延时任务
- **ConnectionPool** — Netty 客户端连接池，处理 JTT808 编解码
- **TaskState**: idle → driving → parking → terminated

### Route & Track（service）

- **RouteManager** — 线路加载、缓存、行驶计划（DrivePlan）生成，轨迹随机化（每次行驶轨迹不同但路线相同，不应通过 OSRM 等路由引擎强制所有设备走完全相同的路线）
- 路线数据：轨迹点（RoutePoint）+ 停留点（StayPoint）+ 问题路段（TroubleSegment）
- 坐标系：项目全程使用 WGS84 坐标系

### Web Layer（web）

Spring Boot + MyBatis + FreeMarker 模板，前端基于 jQuery/Bootstrap + Leaflet/OpenStreetMap。

| Controller | 路径 | 职责 |
|---|---|---|
| RouteController | /route/* | 线路 CRUD、轨迹点与停留点管理 |
| TaskController | /task/* | 创建并启动单个模拟任务 |
| BatchController | /batch/* | 批量创建任务（压力测试） |
| MonitorController | /monitor/list/* | 实时任务状态监控 |
| MapMonitorController | /monitor/* | 地图页实时轨迹 |

数据库实体使用 MyBatis Generator 生成的 Example 模式（RouteExample 等），Mapper XML 在 `resources/cn/org/hentai/simulator/infrastructure/persistence/mapper/`。

### Database Tables

- `x_route` — 线路（名称、速度范围、里程、站点 JSON）
- `x_route_point` — 线路轨迹点（经纬度）
- `x_stay_point` — 停留点（经纬度、停留时长范围、触发概率）
- `x_trouble_segment` — 问题路段（起止索引、事件代码、触发概率）
- `x_schedule_task` — 定时行程任务

### Protocol

使用 `org.yzh:jtt808-protocol` 库处理 JTT808 编解码。支持的消息类型包括 T0100（注册）、T0102（鉴权）、T0001（终端通用应答）、T0200（位置上报）、T8100（注册应答）、T8300（文本下发）等。位置上报直接使用 WGS84 坐标。

### Extending with New Server Message Handlers

在 SimpleDriveTask（或自定义子类）中添加方法，使用 `@Listen` 注解监听事件：

```java
// 监听连接成功
@Listen(when = EventEnum.connected)
public void onConnected() { ... }

// 监听特定消息ID，attachment 值为消息ID
@Listen(when = EventEnum.message_received, attachment = "8801")
public void onCameraCaptureCommand(JTT808Message msg) { ... }
```

定时/延时任务 API（在 AbstractDriveTask 中）：
- `executeAfter(Executable, milliseconds)` — 延时执行
- `executeConstantly(Executable, interval)` — 定时循环执行

## Key Conventions

- 前端页面为 FreeMarker 模板（`.ftlh`），公共组件在 `templates/inc/`（resource.ftlh / sidebar.ftlh / footer.ftlh）
- 静态资源在 `static/proton/` 下，使用 jQuery 2.1.1 + Bootstrap
- `static/js/common.js` 含全局函数：`confirmDialog(text, onOk, onCancel)`、`toastr(type, msg)`、`setCurrentMenu(id)`、`$.fn.paginate()`。弹窗/提示 UI 统一走这些函数，CSS class 遵循 BEM（`.confirm__body`、`.toast--success`），JS 选择器必须与 BEM class 精确匹配
- CSS 共享样式（`.card-section__*`、`.page-header`、`.page-body`）在 `static/css/common.css`，页面特有样式写在各 `.ftlh` 的内联 `<style>` 块
- 配置集中在 `application.yml`，包括数据库、车辆服务器地址
- `simulator.mode` 配置模拟模式（当前为 `stress`）
- 修改 `static/` 下的 JS/CSS 后浏览器可能缓存旧文件，验证时需强制刷新（DevTools: `ignoreCache` 或 Ctrl+Shift+R）

### 地图监控（monitor）

- 地图使用 Leaflet + CartoDB Voyager 瓦片，车辆动画库为 `static/js/LeafletAutoCar.js`（`BMapLib.AutoCar.js` 为旧版百度地图，未使用）
- 车辆图标（`static/img/vehicle.png`）车头朝上，后端 `LBSUtils.caculateAngle` 返回正北为 0° 的罗盘角，前端直接 `rotate(direction)`
- 后端在 `SimpleDriveTask.reportLocation()` 中通过 `Point.setDirection()` 写入方向角，前端经 `/monitor/position` 获取
- 当前位置更新为直接跳变（无平滑动画），如需恢复需在 `LeafletAutoCar.moveTo` 中重新实现
