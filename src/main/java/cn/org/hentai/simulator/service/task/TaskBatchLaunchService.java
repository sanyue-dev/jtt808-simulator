package cn.org.hentai.simulator.service.task;

import cn.org.hentai.simulator.domain.entity.Route;
import cn.org.hentai.simulator.domain.model.TaskLifecycleObserver;
import cn.org.hentai.simulator.domain.model.TerminalIdentity;
import cn.org.hentai.simulator.service.RouteService;
import cn.org.hentai.simulator.service.TaskManager;
import cn.org.hentai.simulator.service.monitor.TaskStopResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TaskBatchLaunchService
{
    private static final Logger logger = LoggerFactory.getLogger(TaskBatchLaunchService.class);

    private final RouteService routeService;
    private final TaskGateway taskGateway;
    private final TaskScheduler launchScheduler;
    private final TaskScheduler stopScheduler;
    private final CapacityProbe capacityProbe;
    private final TaskIdentityBatchGenerator identityBatchGenerator;
    private final TaskGroupMonitorService taskGroupMonitorService;
    private final AtomicReference<LaunchSession> currentSession = new AtomicReference<>();

    @Autowired
    public TaskBatchLaunchService(RouteService routeService, TaskGroupMonitorService taskGroupMonitorService)
    {
        this(routeService, new TaskManagerGateway(), new ExecutorTaskScheduler(newLaunchScheduler()), new ExecutorTaskScheduler(Executors.newSingleThreadScheduledExecutor()), new SystemCapacityProbe(), taskGroupMonitorService);
    }

    TaskBatchLaunchService(RouteService routeService, TaskGateway taskGateway, TaskScheduler launchScheduler, TaskScheduler stopScheduler)
    {
        this(routeService, taskGateway, launchScheduler, stopScheduler, new SystemCapacityProbe(), new TaskGroupMonitorService());
    }

    TaskBatchLaunchService(RouteService routeService, TaskGateway taskGateway, TaskScheduler launchScheduler, TaskScheduler stopScheduler, CapacityProbe capacityProbe)
    {
        this(routeService, taskGateway, launchScheduler, stopScheduler, capacityProbe, new TaskGroupMonitorService());
    }

    TaskBatchLaunchService(RouteService routeService, TaskGateway taskGateway, TaskScheduler launchScheduler, TaskScheduler stopScheduler, CapacityProbe capacityProbe, TaskGroupMonitorService taskGroupMonitorService)
    {
        this.routeService = routeService;
        this.taskGateway = taskGateway;
        this.launchScheduler = launchScheduler;
        this.stopScheduler = stopScheduler;
        this.capacityProbe = capacityProbe;
        this.taskGroupMonitorService = taskGroupMonitorService;
        this.identityBatchGenerator = new TaskIdentityBatchGenerator();
    }

    private static ScheduledThreadPoolExecutor newLaunchScheduler()
    {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        executor.setRemoveOnCancelPolicy(true);
        return executor;
    }

    public BatchTaskLaunchResult launch(BatchTaskLaunchRequest request)
    {
        PreflightCheckResult preflight = preflight(request);
        throwIfPreflightFailed(preflight);
        List<Route> routes = resolveRoutes(request.getRouteIds());
        int batchSize = effectiveRampUpBatchSize(request);
        List<TaskLaunchWindow> windows = buildLaunchWindows(request.getTerminalCount(), batchSize, request.getRampUpIntervalMillis());
        List<TerminalIdentity> identities = identityBatchGenerator.generate(request.getTerminalCount(), taskGateway.reserveIndexes(request.getTerminalCount()), request.getVehicleNumberPattern(), request.getDeviceSnPattern(), request.getSimNumberPattern());
        TaskCreationResult creation = taskGroupMonitorService.createGroup(TaskGroupSource.BATCH, request.getTerminalCount(), windows.size());
        LaunchSession session = new LaunchSession(creation.getTaskGroupId(), request.getTerminalCount(), windows.size(), request.getRunDurationSeconds() > 0);
        taskGroupMonitorService.registerLaunchStopper(creation.getTaskGroupId(), taskIds -> stopLaunching(session, taskIds));
        currentSession.set(session);

        try
        {
            for (TaskLaunchWindow window : windows)
            {
                ScheduledFuture<?> future = launchScheduler.schedule(() -> launchWindow(request, routes, identities, window, session), window.getDelayMillis(), TimeUnit.MILLISECONDS);
                session.launchFutures.add(future);
            }
            if (request.getRunDurationSeconds() > 0)
            {
                stopScheduler.schedule(() -> stopSession(session), request.getRunDurationSeconds(), TimeUnit.SECONDS);
            }
            return new BatchTaskLaunchResult(request.getTerminalCount(), windows.size(), request.getRunDurationSeconds() > 0, preflight, creation.getTaskGroupId(), creation.getTaskGroupDisplayName());
        }
        catch(RuntimeException ex)
        {
            session.fail(ex);
            taskGroupMonitorService.recordLaunchFailure(creation.getTaskGroupId(), ex);
            stopSession(session);
            throw new RuntimeException("批量任务启动失败，已请求终止已启动任务", ex);
        }
    }

    public BatchTaskLaunchProgress currentProgress()
    {
        LaunchSession session = currentSession.get();
        if (session == null) return BatchTaskLaunchProgress.empty();
        return session.progress();
    }

    public void validate(BatchTaskLaunchRequest request)
    {
        throwIfPreflightFailed(preflight(request));
    }

    public PreflightCheckResult preflight(BatchTaskLaunchRequest request)
    {
        PreflightCheckResult result = new PreflightCheckResult();
        validateConfig(request, result);
        validateIdentityGeneration(request, result);
        checkFileDescriptors(request, result);
        checkEphemeralPorts(request, result);
        return result;
    }

    private void throwIfPreflightFailed(PreflightCheckResult preflight)
    {
        if (preflight.hasFailures()) throw new PreflightCheckException(preflight);
    }

    private void validateConfig(BatchTaskLaunchRequest request, PreflightCheckResult result)
    {
        if (request.getTerminalCount() < 1 || request.getTerminalCount() > 100000)
            result.fail("终端数量必须在 1 到 100000 之间");
        if (request.getReportIntervalSeconds() < 1) result.fail("位置上报间隔必须大于 0 秒");
        if (request.getRunDurationSeconds() < 0) result.fail("运行时长不能小于 0 秒");

        int batchSize = effectiveRampUpBatchSize(request);
        if (batchSize < 1) result.fail("ramp-up 批次大小必须大于 0");
        if (request.getRampUpIntervalMillis() < 1) result.fail("ramp-up 间隔必须大于 0 毫秒");
        if (request.getRunDurationSeconds() > 0 && request.getTerminalCount() > 0 && batchSize > 0)
        {
            long launchWindowCount = (request.getTerminalCount() + (long) batchSize - 1L) / batchSize;
            long lastLaunchDelayMillis = (launchWindowCount - 1L) * request.getRampUpIntervalMillis();
            long runDurationMillis = request.getRunDurationSeconds() * 1000L;
            if (lastLaunchDelayMillis >= runDurationMillis)
                result.fail("ramp-up 最后启动窗口必须早于运行时长: lastLaunchDelayMillis=" + lastLaunchDelayMillis + ", runDurationMillis=" + runDurationMillis);
        }
        if (request.getServerAddress() == null || request.getServerAddress().isBlank()) result.fail("目标服务端地址不能为空");
        if (request.getServerPort() < 1 || request.getServerPort() > 65535) result.fail("目标服务端端口非法: " + request.getServerPort());
    }

    private void validateIdentityGeneration(BatchTaskLaunchRequest request, PreflightCheckResult result)
    {
        if (request.getTerminalCount() < 1 || request.getTerminalCount() > 100000) return;
        try
        {
            identityBatchGenerator.generate(request.getTerminalCount(), 1L, request.getVehicleNumberPattern(), request.getDeviceSnPattern(), request.getSimNumberPattern());
        }
        catch(RuntimeException ex)
        {
            result.fail(ex.getMessage());
        }
    }

    private void checkFileDescriptors(BatchTaskLaunchRequest request, PreflightCheckResult result)
    {
        long open = capacityProbe.openFileDescriptorCount();
        long max = capacityProbe.maxFileDescriptorCount();
        long required = request.getTerminalCount() + 64L;
        result.setOpenFileDescriptorCount(open);
        result.setMaxFileDescriptorCount(max);
        result.setRequiredFileDescriptorCount(required);
        if (max < 0)
        {
            result.warn("无法读取本机文件描述符限制，10k 验证前请人工确认 ulimit -n 足够支撑目标连接数");
            return;
        }
        long available = max - Math.max(open, 0L);
        if (available < required)
        {
            result.fail("本机文件描述符余量不足: available=" + available + ", required=" + required + ", open=" + open + ", max=" + max);
        }
    }

    private void checkEphemeralPorts(BatchTaskLaunchRequest request, PreflightCheckResult result)
    {
        long capacity = capacityProbe.singleDestinationEphemeralPortCapacity();
        result.setSingleDestinationEphemeralPortCapacity(capacity);
        if (capacity < 0)
        {
            result.warn("无法读取单源 IP 连接单服务端的临时端口容量，10k 验证前请人工确认本地端口范围");
            return;
        }
        if (request.getTerminalCount() > capacity)
        {
            result.warn("单源 IP 到同一服务端地址端口的临时端口容量存在风险: requested=" + request.getTerminalCount() + ", estimatedCapacity=" + capacity);
        }
    }

    public List<TaskLaunchWindow> buildLaunchWindows(int terminalCount, int batchSize, long intervalMillis)
    {
        List<TaskLaunchWindow> windows = new ArrayList<>();
        for (int start = 0, batchIndex = 0; start < terminalCount; start += batchSize, batchIndex++)
        {
            int end = Math.min(start + batchSize, terminalCount);
            windows.add(new TaskLaunchWindow(start, end, batchIndex * intervalMillis));
        }
        return windows;
    }

    private int effectiveRampUpBatchSize(BatchTaskLaunchRequest request)
    {
        return request.getRampUpBatchSize() > 0 ? request.getRampUpBatchSize() : request.getTerminalCount();
    }

    private List<Route> resolveRoutes(List<Long> routeIds)
    {
        List<Route> routes = new ArrayList<>();
        if (routeIds == null || routeIds.isEmpty())
        {
            routes.addAll(routeService.list());
        }
        else
        {
            for (Long routeId : routeIds)
            {
                Route route = routeService.getById(routeId);
                if (route == null) throw new IllegalArgumentException("线路不存在: " + routeId);
                routes.add(route);
            }
        }
        if (routes.isEmpty()) throw new IllegalArgumentException("没有可用于批量创建的线路");
        return routes;
    }

    private void launchWindow(BatchTaskLaunchRequest request, List<Route> routes, List<TerminalIdentity> identities, TaskLaunchWindow window, LaunchSession session)
    {
        session.recordWindowStarted();
        try
        {
            for (int i = window.getStartIndex(); i < window.getEndIndex(); i++)
            {
                if (session.stopping.get()) return;
                TerminalIdentity identity = identities.get(i);
                Route route = routes.get(i % routes.size());
                long taskId = taskGateway.nextTaskId();
                taskGateway.run(taskId, params(request, identity), route.getId(), request.getReportIntervalSeconds(), taskGroupMonitorService.observer(session.taskGroupId));
                session.taskIds.add(taskId);
                session.startedTasks.incrementAndGet();
                taskGroupMonitorService.recordTaskStarted(session.taskGroupId, taskId);
                if (session.stopping.get())
                {
                    TaskStopResult result = taskGateway.terminateTasks(List.of(taskId));
                    session.recordStopResult(List.of(taskId), result);
                }
            }
            session.recordWindowExecuted();
            taskGroupMonitorService.recordLaunchWindowExecuted(session.taskGroupId);
        }
        catch(RuntimeException ex)
        {
            logger.error("批量任务启动窗口失败: startIndex={}, endIndex={}, delayMillis={}", window.getStartIndex(), window.getEndIndex(), window.getDelayMillis(), ex);
            session.fail(ex);
            taskGroupMonitorService.recordLaunchFailure(session.taskGroupId, ex);
            stopSession(session);
            throw ex;
        }
        finally
        {
            session.recordWindowFinished();
        }
    }

    private Map<String, String> params(BatchTaskLaunchRequest request, TerminalIdentity identity)
    {
        Map<String, String> params = new HashMap<>();
        params.put("server.address", request.getServerAddress());
        params.put("server.port", String.valueOf(request.getServerPort()));
        params.put("mode", request.getMode());
        params.put("vehicle.number", identity.getVehicleNumber());
        params.put("device.sn", identity.getDeviceSn());
        params.put("device.sim", identity.getSimNumber());
        params.put("mileages", "0");
        return params;
    }

    private void stopSession(LaunchSession session)
    {
        if (session.stopping.compareAndSet(false, true) == false) return;
        cancelPendingLaunches(session);
        List<Long> taskIds = new ArrayList<>(session.taskIds);
        TaskStopResult result = taskGateway.terminateTasks(taskIds);
        session.recordStopResult(taskIds, result);
        taskGroupMonitorService.recordLaunchStopped(session.taskGroupId);
        if (result.getFailed() > 0)
        {
            logger.error("批量任务自动停止存在失败: succeeded={}, failed={}, failures={}", result.getSucceeded(), result.getFailed(), result.getFailures());
        }
    }

    private TaskStopResult stopLaunching(LaunchSession session, Collection<Long> taskIds)
    {
        if (session.stopping.compareAndSet(false, true))
        {
            cancelPendingLaunches(session);
        }
        TaskStopResult result = taskGateway.terminateTasks(taskIds);
        session.recordStopResult(taskIds, result);
        taskGroupMonitorService.recordLaunchStopped(session.taskGroupId);
        return result;
    }

    private void cancelPendingLaunches(LaunchSession session)
    {
        session.launchFutures.forEach(future -> {
            if (future.isDone() == false) future.cancel(false);
        });
    }

    interface TaskGateway
    {
        long reserveIndexes(int count);

        long nextTaskId();

        void run(long taskId, Map<String, String> params, Long routeId, int reportIntervalSeconds, TaskLifecycleObserver lifecycleObserver);

        TaskStopResult terminateTasks(Collection<Long> taskIds);
    }

    interface TaskScheduler
    {
        ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit);
    }

    interface CapacityProbe
    {
        long openFileDescriptorCount();

        long maxFileDescriptorCount();

        long singleDestinationEphemeralPortCapacity();
    }

    private static class TaskManagerGateway implements TaskGateway
    {
        @Override
        public long reserveIndexes(int count)
        {
            return TaskManager.getInstance().reserveIndexes(count);
        }

        @Override
        public long nextTaskId()
        {
            return TaskManager.getInstance().nextTaskId();
        }

        @Override
        public void run(long taskId, Map<String, String> params, Long routeId, int reportIntervalSeconds, TaskLifecycleObserver lifecycleObserver)
        {
            TaskManager.getInstance().run(taskId, params, routeId, reportIntervalSeconds, lifecycleObserver);
        }

        @Override
        public TaskStopResult terminateTasks(Collection<Long> taskIds)
        {
            return TaskManager.getInstance().terminateTasks(taskIds);
        }
    }

    private static class ExecutorTaskScheduler implements TaskScheduler
    {
        private final java.util.concurrent.ScheduledExecutorService executor;

        private ExecutorTaskScheduler(java.util.concurrent.ScheduledExecutorService executor)
        {
            this.executor = executor;
        }

        @Override
        public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit)
        {
            return executor.schedule(task, delay, unit);
        }
    }

    private static class SystemCapacityProbe implements CapacityProbe
    {
        @Override
        public long openFileDescriptorCount()
        {
            java.lang.management.OperatingSystemMXBean bean = java.lang.management.ManagementFactory.getOperatingSystemMXBean();
            if (bean instanceof com.sun.management.UnixOperatingSystemMXBean unixBean) return unixBean.getOpenFileDescriptorCount();
            return -1L;
        }

        @Override
        public long maxFileDescriptorCount()
        {
            java.lang.management.OperatingSystemMXBean bean = java.lang.management.ManagementFactory.getOperatingSystemMXBean();
            if (bean instanceof com.sun.management.UnixOperatingSystemMXBean unixBean) return unixBean.getMaxFileDescriptorCount();
            return -1L;
        }

        @Override
        public long singleDestinationEphemeralPortCapacity()
        {
            return 28000L;
        }
    }

    private static class LaunchSession
    {
        private final int targetTasks;
        private final String taskGroupId;
        private final int rampUpWindowCount;
        private final boolean autoStopScheduled;
        private final AtomicBoolean stopping = new AtomicBoolean(false);
        private final AtomicBoolean stopRequested = new AtomicBoolean(false);
        private final AtomicInteger runningWindowCount = new AtomicInteger();
        private final AtomicInteger executedWindowCount = new AtomicInteger();
        private final AtomicInteger startedTasks = new AtomicInteger();
        private final Queue<Long> taskIds = new ConcurrentLinkedQueue<>();
        private final Set<Long> stoppedTaskIds = ConcurrentHashMap.newKeySet();
        private final CopyOnWriteArrayList<ScheduledFuture<?>> launchFutures = new CopyOnWriteArrayList<>();
        private final AtomicReference<String> state = new AtomicReference<>("launching");
        private final AtomicReference<String> failureReason = new AtomicReference<>();
        private final AtomicLong stopSucceeded = new AtomicLong();
        private final AtomicLong stopFailed = new AtomicLong();

        private LaunchSession(String taskGroupId, int targetTasks, int rampUpWindowCount, boolean autoStopScheduled)
        {
            this.taskGroupId = taskGroupId;
            this.targetTasks = targetTasks;
            this.rampUpWindowCount = rampUpWindowCount;
            this.autoStopScheduled = autoStopScheduled;
        }

        private void recordWindowExecuted()
        {
            int executed = executedWindowCount.incrementAndGet();
            if (executed >= rampUpWindowCount) state.compareAndSet("launching", "running");
        }

        private void recordWindowStarted()
        {
            runningWindowCount.incrementAndGet();
        }

        private void recordWindowFinished()
        {
            runningWindowCount.decrementAndGet();
            completeIfStopped();
        }

        private void fail(RuntimeException ex)
        {
            failureReason.compareAndSet(null, ex.getMessage());
            state.set("failed");
        }

        private void recordStopResult(Collection<Long> taskIds, TaskStopResult result)
        {
            stopRequested.set(true);
            Set<Long> failedTaskIds = new java.util.HashSet<>();
            result.getFailures().forEach(failure -> failedTaskIds.add(failure.getTaskId()));
            for (Long taskId : taskIds)
            {
                if (stoppedTaskIds.add(taskId) == false) continue;
                if (failedTaskIds.contains(taskId)) stopFailed.incrementAndGet();
                else stopSucceeded.incrementAndGet();
            }
            if ("failed".equals(state.get())) return;
            completeIfStopped();
        }

        private void completeIfStopped()
        {
            if (stopRequested.get() && runningWindowCount.get() == 0 && "failed".equals(state.get()) == false) state.set("completed");
        }

        private BatchTaskLaunchProgress progress()
        {
            String currentState = stopping.get() && ("launching".equals(state.get()) || runningWindowCount.get() > 0) ? "stopping" : state.get();
            return BatchTaskLaunchProgress.of(
                    currentState,
                    targetTasks,
                    rampUpWindowCount,
                    executedWindowCount.get(),
                    startedTasks.get(),
                    autoStopScheduled,
                    failureReason.get(),
                    stopSucceeded.get(),
                    stopFailed.get()
            );
        }
    }
}
