---
paths:
  - src/main/resources/templates/route-create.ftlh
  - src/main/resources/templates/monitor.ftlh
  - src/main/resources/static/js/LeafletAutoCar.js
  - src/main/java/cn/org/hentai/simulator/web/controller/RouteController.java
  - src/main/java/cn/org/hentai/simulator/web/controller/MapMonitorController.java
---

# Frontend Map Pages

- 地图使用 Leaflet + CartoDB Voyager 瓦片，车辆动画库为 `static/js/LeafletAutoCar.js`（`BMapLib.AutoCar.js` 为旧版百度地图，未使用）
- `route-create.ftlh` 与 `monitor.ftlh` 现已统一使用 Leaflet + CartoDB Voyager 底图；不要再按线路页 OSM 标准底图的旧状态判断
- 车辆图标（`static/img/vehicle.png`）车头朝上，后端 `LBSUtils.caculateAngle` 返回正北为 0° 的罗盘角，前端直接 `rotate(direction)`
- 后端在 `SimpleDriveTask.reportLocation()` 中通过 `Point.setDirection()` 写入方向角，前端经 `/monitor/position` 获取
- 当前位置更新为直接跳变（无平滑动画），如需恢复需在 `LeafletAutoCar.moveTo` 中重新实现
- `/route/create` 与 `/route/edit?id=...` 共用 `route-create.ftlh`，改该模板会同时影响创建页和编辑页
- 线路创建最小闭环：填写名称/速度 → 添加并选中至少 2 个站点候选 → 点击“线路规划”生成 `route.points` → 保存；只填站点文本不点候选不会形成有效轨迹
- 线路页保存时若未设置停留点/问题路段，会依次触发两次 `confirmDialog` 确认后才真正提交
