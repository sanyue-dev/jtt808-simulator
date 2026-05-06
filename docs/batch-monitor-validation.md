# 批量创建与全局监控验证

`/acceptance/1k` 和 `/acceptance/10k` 已不再是产品主路径。容量验证统一通过批量创建行程任务和全局行程监控完成：

- `/batch/index`：批量创建行程任务，车辆数量就是终端数量。
- `/monitor/list/index`：观察所有行程任务的总体运行指标、失败阶段、失败原因，并停止单个或全部未终止任务。
- `/task/index`：仍用于单独创建行程任务，创建后的任务同样进入全局任务池。

## 启动批量任务

先启动模拟器 Web 应用：

```bash
./mvnw spring-boot:run
```

通过 UI 打开：

```text
http://127.0.0.1:18888/batch/index
```

也可以直接调用接口：

```bash
curl -X POST 'http://127.0.0.1:18888/batch/run' \
  -d vehicleCount=10000 \
  -d serverAddress=127.0.0.1 \
  -d serverPort=20021 \
  -d reportIntervalSeconds=5 \
  -d runDurationSeconds=300 \
  -d rampUpBatchSize=100 \
  -d rampUpIntervalMillis=1000 \
  --data-urlencode 'vehicleNumberPattern=京%06d' \
  --data-urlencode 'deviceSnPattern=A%06d' \
  --data-urlencode 'simNumberPattern=013800%06d'
```

## 参数说明

- `vehicleCount`：终端数量，范围为 `1..100000`。
- `serverAddress` / `serverPort`：目标 JT/T 808 服务端。
- `reportIntervalSeconds`：位置上报间隔，必须大于 `0`。
- `runDurationSeconds`：运行时长；`0` 表示不自动停止，大于 `0` 时到期停止本次批量创建已启动的任务。
- `rampUpBatchSize`：每批启动数量；`0` 表示按本次 `vehicleCount` 一次性提交。
- `rampUpIntervalMillis`：ramp-up 批次间隔，必须大于 `0`。
- `vehicleNumberPattern` / `deviceSnPattern` / `simNumberPattern`：身份格式，按 Java `String.format` 生成，并在启动前检查唯一性和格式。
- `routeIdList[]`：可选线路 ID；不传或传 `0` 时使用全部线路轮询分配。

## 观察与停止

全局观察页面：

```text
http://127.0.0.1:18888/monitor/list/index
```

接口：

```bash
curl 'http://127.0.0.1:18888/monitor/list/summary'
curl 'http://127.0.0.1:18888/monitor/list/json?pageIndex=1&pageSize=20'
curl -X POST 'http://127.0.0.1:18888/monitor/list/terminate-all'
```

监控指标来自统一任务生命周期统计，包括连接、注册、鉴权、位置上报、断连、终止、发送失败和协议异常。失败不会被 mock 或静默吞掉，会进入全局指标以及任务明细的失败阶段和失败原因。

## 前置检查

`POST /batch/run` 会返回 `preflight`，用于记录启动前容量风险：

- `failures`：硬失败。存在时本次批量启动会被拒绝，例如身份规则重复、服务端端口非法、文件描述符余量不足。
- `warnings`：风险提示。不会阻止启动，但 10k 验证记录必须保留，例如无法读取本机 fd 限制、无法确认临时端口容量，或单源 IP 连接同一服务端地址端口接近本机临时端口容量。
- `openFileDescriptorCount` / `maxFileDescriptorCount`：当前进程 fd 使用量和上限；`-1` 表示当前平台无法读取。
- `requiredFileDescriptorCount`：本次启动按终端数量估算的最低 fd 需求。
- `singleDestinationEphemeralPortCapacity`：单源 IP 连接同一服务端地址端口的临时端口估算容量；超过时应使用更小规模、分源 IP、多客户端或调整系统端口范围后再验证。

前置检查不是成功保证。它只拒绝明显不可执行的配置，并把会影响 10k 的本机容量风险提前暴露出来。

## 运行观测字段

`GET /monitor/list/summary` 除任务生命周期指标外，还包含：

- `runtimeResources.heapUsedBytes` / `heapCommittedBytes` / `heapMaxBytes`：JVM heap 使用、已提交和上限。
- `runtimeResources.threadCount` / `peakThreadCount` / `daemonThreadCount`：当前线程、峰值线程和 daemon 线程数量。
- `runtimeResources.availableProcessors` / `systemLoadAverage` / `processCpuLoad`：CPU 可用核数、系统 load 和进程 CPU load；无法读取时可能为负值。
- `runtimeResources.openFileDescriptorCount` / `maxFileDescriptorCount`：当前进程 fd 使用量和上限；非 Unix 平台可能为 `-1`。
- `schedulerDelay.samples`：全局调度器已记录的 planned-vs-actual 样本数。
- `schedulerDelay.averageDelayMillis` / `maxDelayMillis`：任务调度平均延迟和最大延迟。10k 验证中如果位置上报速率下降且调度延迟升高，优先排查客户端调度线程、CPU 和 GC。

## 10k 验证记录

10k live 容量验证应记录：

- 启动参数：终端数量、目标服务端、上报间隔、ramp-up、运行时长、身份规则和线路选择。
- `/batch/run` 返回的 `preflight.failures`、`preflight.warnings`、fd 和临时端口容量字段。
- `/monitor/list/summary` 的连接、注册、鉴权、位置上报、失败和终止指标。
- `/monitor/list/summary` 的 JVM heap、线程数、fd、CPU load、位置上报速率和调度延迟。
- `/monitor/list/index` 明细中的失败阶段和失败原因样本。
- 停止方式：自动停止、单任务停止或停止全部未终止任务。
- 验证时间、Git commit、目标服务端版本和机器资源。
