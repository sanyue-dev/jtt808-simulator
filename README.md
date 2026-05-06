<div align="center">
	<img src="./doc/logo.png" />
</div>

# jtt808-simulator

`jtt808-simulator` 是一个基于 Java 17 / Spring Boot 的 JT/T 808 终端模拟器。它可以按照线路轨迹模拟车辆行驶，连接真实 JT/T 808 服务端，完成注册、鉴权、位置上报和下行消息处理，主要用于协议联调、业务验证和多终端压力测试。

当前产品主路径是：

1. 在线路管理中维护车辆行驶线路。
2. 在“创建行程任务”中启动单个终端，或在“批量创建行程任务”中启动多个终端。
3. 在“实时行程监控”中观察全部行程任务的整体运行状态，并停止单个或全部运行中任务。

> 本项目仍在开发中。目标是支撑最高 100,000 终端规模的稳定压测，但大规模压测是否可达取决于客户端机器、操作系统参数、JVM、网络、源端口容量和服务端能力。不要把本地单机默认配置理解为天然可以承载 100,000 TCP 连接。

## 当前能力

- JT/T 808 2013 协议终端模拟。
- Netty TCP 连接管理。
- 单个行程任务创建与启动。
- 批量行程任务创建，车辆数量即模拟终端数量。
- 批量启动前置检查，包括文件描述符、源端口容量和启动参数检查。
- 全局行程任务监控，包含任务状态、连接、注册、鉴权、位置上报、失败、断连、终止和运行资源指标。
- 行程任务分页查询，避免监控页面一次性加载全部任务。
- 停止单个任务和停止全部运行中任务。
- 车辆实时交互面板：在地图页面查看单个任务的位置、日志，并切换报警标志位和车辆状态位。
- 线路轨迹随机化：每次行驶沿同一线路生成不同轨迹点，避免所有终端完全重合。

暂未作为当前主线完成的能力：

- JT/T 808 2019 协议支持。
- JT/T 1078 音视频模拟。
- 100,000 终端完整生产级验证。

## 技术栈

- Java 17
- Spring Boot
- MyBatis
- MySQL
- Netty
- FreeMarker
- jQuery / Bootstrap / Leaflet
- `org.yzh:jtt808-protocol`

## 快速开始

### 前置条件

- JDK 17
- MySQL
- 可连接的 JT/T 808 服务端

创建数据库：

```sql
CREATE DATABASE simulator DEFAULT CHARACTER SET utf8mb4;
```

默认配置在 `src/main/resources/application.yml`：

```yaml
server:
  port: 18888
spring:
  datasource:
    username: root
    password: root
    url: jdbc:mysql://127.0.0.1:3306/simulator?autoReconnect=true&useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
vehicle-server:
  addr: 127.0.0.1
  port: '20021'
```

数据库表会在应用启动时通过 `schema.sql` 自动创建。

### 启动应用

```bash
./mvnw spring-boot:run
```

打开：

```text
http://127.0.0.1:18888
```

打包运行：

```bash
./mvnw clean package
java -jar target/jtt808-simulator-1.0-SNAPSHOT.jar
```

## 页面入口

| 页面 | 路径 | 用途 |
|---|---|---|
| 线路管理 | `/route/index` | 创建和维护行驶线路 |
| 创建行程任务 | `/task/index` | 启动单个模拟终端 |
| 批量创建行程任务 | `/batch/index` | 按车辆数量批量启动模拟终端 |
| 实时行程监控 | `/monitor/list/index` | 查看全局指标、分页任务列表、停止任务 |
| 地图监控 | `/monitor/view?id={taskId}` | 查看单个任务的当前位置、日志，并切换报警标志位和状态位 |

## 使用流程

### 1. 创建线路

先在线路管理中创建一条行驶线路。线路包含轨迹点、速度范围、里程和停留点等信息。模拟任务启动后会基于线路生成行驶计划，并在运行中周期性上报位置。

项目中的坐标按 WGS84 处理。如果接入的平台或地图使用其他坐标系，需要在平台侧或数据处理链路中自行转换。

### 2. 启动单个终端

进入 `/task/index`，选择线路并填写：

- 车牌号
- 终端 ID
- SIM 卡号
- 服务端 IP
- 服务端端口
- 初始里程

提交后会创建一个行程任务，并立即连接目标 JT/T 808 服务端。

### 3. 批量启动终端

进入 `/batch/index`，通过“车辆数量”设置要启动的模拟终端数量。这里的车辆数量就是终端数量。

批量启动支持：

- 选择一条或多条线路。
- 设置车牌号、终端 ID、SIM 卡号生成格式。
- 设置目标服务端地址和端口。
- 设置位置上报间隔。
- 设置运行时长，达到时长后自动停止。
- 设置 ramp-up 批次大小和批次间隔，控制启动节奏。

批量启动前会执行前置检查。检查失败时接口会明确返回失败原因，不会通过静默降级、跳过任务或虚假成功来掩盖问题。

### 4. 观察和停止任务

进入 `/monitor/list/index` 查看所有行程任务的整体运行情况。

监控页面包含：

- 总任务数、运行中、停车、已终止等状态计数。
- 连接成功/失败。
- 注册成功/失败。
- 鉴权成功/失败。
- 位置上报总数和上报速率。
- 发送失败、断连、协议异常。
- JVM heap、线程数、文件描述符、CPU、调度延迟等运行资源指标。
- 分页任务列表和关键字/状态筛选。
- 停止单个任务。
- 停止全部运行中任务。

压测时建议一直保留监控页面，确认连接、注册、鉴权、位置上报、失败和停止结果是否一致。

## 压测建议

小规模联调可以直接从几十或几百终端开始。进入 10,000 及以上规模前，至少需要确认：

- 客户端机器的文件描述符上限足够。
- 客户端到服务端的单目标源端口容量足够。
- JVM heap 和 direct memory 配置足够。
- 服务端允许足够的 TCP 连接。
- 网络链路、NAT、防火墙和安全组不会主动限制连接数。
- 日志级别不会在高频路径输出大量日志。

建议按阶段推进：

1. 100 终端：确认协议交互正确。
2. 1,000 终端：确认批量启动、监控和停止路径稳定。
3. 10,000 终端：确认客户端资源、服务端资源和位置上报吞吐。
4. 50,000 / 100,000 终端：在完成 OS/JVM/网络准备后再验证。

10k/100k 不是独立的产品入口。它们是容量建设里程碑，启动仍然通过 `/batch/index`，观察和控制仍然通过 `/monitor/list/index`。

## 开发说明

核心代码结构：

```text
src/main/java/cn/org/hentai/simulator
├── app/             # Spring Boot 启动入口
├── domain/          # 实体、模型和枚举
├── infrastructure/  # MyBatis 和通用工具
├── engine/          # 模拟引擎、事件、Netty 连接、运行器和日志
├── service/         # 线路、任务和批量启动服务
└── web/             # MVC 控制器和视图对象
```

主要运行模型：

- `TaskManager` 管理行程任务生命周期和全局运行指标。
- `SimpleDriveTask` 是当前 JT/T 808 终端任务实现。
- `ConnectionPool` 负责 Netty TCP 连接和消息收发。
- `EventDispatcher` 根据事件和 `@Listen` 注解分发回调。
- `RunnerManager` 负责延迟任务和周期任务调度。

### 扩展服务端消息处理

在 `SimpleDriveTask` 或自定义任务类中添加 `@Listen` 方法即可处理事件。

连接成功事件：

```java
@Listen(when = EventEnum.connected)
public void onConnected()
{
    // connected to server
}
```

按消息 ID 处理服务端下行消息，例如 `0x8801`：

```java
@Listen(when = EventEnum.message_received, attachment = "8801")
public void onCameraCaptureCommand(JTT808Message msg)
{
    // handle camera capture command
}
```

### 延迟和周期任务

`AbstractDriveTask` 提供两个调度方法：

```java
public final void executeAfter(Executable executable, int milliseconds);

public final void executeConstantly(Executable executable, int interval);
```

任务终止后，已排队的发送逻辑应显式识别终止状态并停止执行，不能通过吞掉异常或伪造成功来掩盖真实问题。

## 测试

运行全部测试：

```bash
./mvnw test
```

运行单个测试类：

```bash
./mvnw -Dtest=ConnectionPoolTest test
```

新增测试时放在 `src/test/java` 下，按被测包名镜像目录结构组织。优先覆盖协议交互、任务生命周期、批量启动前置检查、监控指标和停止路径。

## 注意事项

- 当前默认位置上报间隔是 5 秒，可在批量启动页面调整。
- 压测失败时应保留失败阶段、失败原因和资源指标，不要为了显示“通过”而隐藏错误。
- 前端静态资源可能被浏览器缓存。修改 `static/` 下的 JS/CSS 后，验证时建议强制刷新页面。
- 调试大量终端时，避免开启过高日志级别。

## 上游与致谢

本项目基于 `glaciall/jtt808-simulator` 继续维护。原始版权信息保留在 `LICENSE` 中。

原 README 中提供的资料：

1. [原作者博客](https://www.hentai.org.cn/)
2. [JT/1078音视频传输协议开发指南](https://www.hentai.org.cn/article?id=8)
3. [JT/1078 RTP消息包在线解析工具](https://www.hentai.org.cn/format/)
