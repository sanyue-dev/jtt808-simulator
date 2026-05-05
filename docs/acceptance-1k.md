# 1k 终端阶段验收 Harness

本 harness 用于验证 1,000 个真实 JT/T 808 模拟终端连接指定服务端，完成 TCP 连接、注册、鉴权和周期位置上报。它不提供 mock 成功路径；连接、协议、发送和身份生成失败会进入验收记录。

## 启动命令

先启动模拟器 Web 应用：

```bash
./mvnw spring-boot:run
```

发起 1k 验收：

```bash
curl -X POST 'http://127.0.0.1:18888/acceptance/1k/run' \
  -d terminalCount=1000 \
  -d serverAddress=127.0.0.1 \
  -d serverPort=20021 \
  -d reportIntervalSeconds=5 \
  -d runDurationSeconds=300 \
  --data-urlencode 'vehicleNumberPattern=京%06d' \
  --data-urlencode 'deviceSnPattern=A%06d' \
  --data-urlencode 'simNumberPattern=013800%06d'
```

接口会返回 `runId`、本次配置和初始指标。轮询验收记录：

```bash
curl 'http://127.0.0.1:18888/acceptance/1k/{runId}'
```

如需限制线路，可重复传入 `routeIds`：

```bash
curl -X POST 'http://127.0.0.1:18888/acceptance/1k/run' \
  -d terminalCount=1000 \
  -d routeIds=1 \
  -d routeIds=2
```

## 配置来源

- `terminalCount`：终端数量，1k 阶段固定要求为 `1000`，其他值会显式失败。
- `serverAddress` / `serverPort`：目标 JT/T 808 服务端；未传时使用 `application.yml` 的 `vehicle-server.addr` 和 `vehicle-server.port`。
- `reportIntervalSeconds`：位置上报间隔，传给线路行程计划生成。
- `runDurationSeconds`：验收运行时长，到期后 harness 终止本次创建的任务。
- `vehicleNumberPattern` / `deviceSnPattern` / `simNumberPattern`：身份格式，按 Java `String.format` 生成。
- `routeIds`：可选线路 ID 列表；不传时使用全部线路轮询分配。

身份生成会先完整生成 1,000 组车牌号、终端 ID 和 SIM 卡号，并检查三类身份各自唯一。格式无法生成或生成重复时，请求直接失败。

## 验收指标

`GET /acceptance/1k/{runId}` 返回：

- `connectionSucceeded` / `connectionFailed`
- `registrationSucceeded` / `registrationFailed`
- `authenticationSucceeded` / `authenticationFailed`
- `locationReportSent`
- `disconnected`
- `terminated`
- `sendFailed`
- `protocolExceptions`

每个终端记录包含身份、任务 ID、当前阶段、位置上报发送数和失败原因。失败阶段至少区分：

- `connection_failed`
- `registration_failed`
- `authentication_failed`
- `send_failed`
- `protocol_exception`

## 运行时观察项

验收期间同时观察：

- JVM：堆内存、GC 次数/暂停、线程数、直接内存。
- OS：CPU、RSS、文件句柄数、TCP 连接数、网卡吞吐。
- 服务端：在线数、注册成功数、鉴权成功数、位置上报接收数、服务端拒绝或异常日志。

## 通过/失败判断

通过条件：

- 1,000 个终端均连接成功。
- 注册成功数为 1,000。
- 鉴权成功数为 1,000。
- `locationReportSent` 随运行时间持续增长，服务端能看到对应位置上报。
- `connectionFailed`、`registrationFailed`、`authenticationFailed`、`sendFailed`、`protocolExceptions` 为 0。

任一失败计数大于 0，或终端记录中出现失败阶段，即判定失败。失败时优先查看该终端记录的 `failureReason` 和服务端日志。
