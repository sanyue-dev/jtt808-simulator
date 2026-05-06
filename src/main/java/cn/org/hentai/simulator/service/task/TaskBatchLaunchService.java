package cn.org.hentai.simulator.service.task;

import cn.org.hentai.simulator.domain.entity.Route;
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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private final TaskIdentityBatchGenerator identityBatchGenerator;

    @Autowired
    public TaskBatchLaunchService(RouteService routeService)
    {
        this(routeService, new TaskManagerGateway(), new ExecutorTaskScheduler(newLaunchScheduler()), new ExecutorTaskScheduler(Executors.newSingleThreadScheduledExecutor()));
    }

    TaskBatchLaunchService(RouteService routeService, TaskGateway taskGateway, TaskScheduler launchScheduler, TaskScheduler stopScheduler)
    {
        this.routeService = routeService;
        this.taskGateway = taskGateway;
        this.launchScheduler = launchScheduler;
        this.stopScheduler = stopScheduler;
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
        validate(request);
        List<Route> routes = resolveRoutes(request.getRouteIds());
        int batchSize = effectiveRampUpBatchSize(request);
        List<TaskLaunchWindow> windows = buildLaunchWindows(request.getTerminalCount(), batchSize, request.getRampUpIntervalMillis());
        List<TerminalIdentity> identities = identityBatchGenerator.generate(request.getTerminalCount(), taskGateway.reserveIndexes(request.getTerminalCount()), request.getVehicleNumberPattern(), request.getDeviceSnPattern(), request.getSimNumberPattern());
        LaunchSession session = new LaunchSession();

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
            return new BatchTaskLaunchResult(request.getTerminalCount(), windows.size(), request.getRunDurationSeconds() > 0);
        }
        catch(RuntimeException ex)
        {
            stopSession(session);
            throw new RuntimeException("批量任务启动失败，已请求终止已启动任务", ex);
        }
    }

    public void validate(BatchTaskLaunchRequest request)
    {
        if (request.getTerminalCount() < 1 || request.getTerminalCount() > 100000)
            throw new IllegalArgumentException("终端数量必须在 1 到 100000 之间");
        if (request.getReportIntervalSeconds() < 1) throw new IllegalArgumentException("位置上报间隔必须大于 0 秒");
        if (request.getRunDurationSeconds() < 0) throw new IllegalArgumentException("运行时长不能小于 0 秒");

        int batchSize = effectiveRampUpBatchSize(request);
        if (batchSize < 1) throw new IllegalArgumentException("ramp-up 批次大小必须大于 0");
        if (request.getRampUpIntervalMillis() < 1) throw new IllegalArgumentException("ramp-up 间隔必须大于 0 毫秒");
        if (request.getRunDurationSeconds() > 0)
        {
            long launchWindowCount = (request.getTerminalCount() + (long) batchSize - 1L) / batchSize;
            long lastLaunchDelayMillis = (launchWindowCount - 1L) * request.getRampUpIntervalMillis();
            long runDurationMillis = request.getRunDurationSeconds() * 1000L;
            if (lastLaunchDelayMillis >= runDurationMillis)
                throw new IllegalArgumentException("ramp-up 最后启动窗口必须早于运行时长: lastLaunchDelayMillis=" + lastLaunchDelayMillis + ", runDurationMillis=" + runDurationMillis);
        }
        if (request.getServerAddress() == null || request.getServerAddress().isBlank()) throw new IllegalArgumentException("目标服务端地址不能为空");
        if (request.getServerPort() < 1 || request.getServerPort() > 65535) throw new IllegalArgumentException("目标服务端端口非法: " + request.getServerPort());
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
        try
        {
            for (int i = window.getStartIndex(); i < window.getEndIndex(); i++)
            {
                if (session.stopping.get()) return;
                TerminalIdentity identity = identities.get(i);
                Route route = routes.get(i % routes.size());
                long taskId = taskGateway.nextTaskId();
                taskGateway.run(taskId, params(request, identity), route.getId(), request.getReportIntervalSeconds());
                session.taskIds.add(taskId);
                if (session.stopping.get()) taskGateway.terminateTasks(List.of(taskId));
            }
        }
        catch(RuntimeException ex)
        {
            logger.error("批量任务启动窗口失败: startIndex={}, endIndex={}, delayMillis={}", window.getStartIndex(), window.getEndIndex(), window.getDelayMillis(), ex);
            stopSession(session);
            throw ex;
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
        session.launchFutures.forEach(future -> {
            if (future.isDone() == false) future.cancel(false);
        });
        TaskStopResult result = taskGateway.terminateTasks(session.taskIds);
        if (result.getFailed() > 0)
        {
            logger.error("批量任务自动停止存在失败: succeeded={}, failed={}, failures={}", result.getSucceeded(), result.getFailed(), result.getFailures());
        }
    }

    interface TaskGateway
    {
        long reserveIndexes(int count);

        long nextTaskId();

        void run(long taskId, Map<String, String> params, Long routeId, int reportIntervalSeconds);

        TaskStopResult terminateTasks(Collection<Long> taskIds);
    }

    interface TaskScheduler
    {
        ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit);
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
        public void run(long taskId, Map<String, String> params, Long routeId, int reportIntervalSeconds)
        {
            TaskManager.getInstance().run(taskId, params, routeId, reportIntervalSeconds, null);
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

    private static class LaunchSession
    {
        private final AtomicBoolean stopping = new AtomicBoolean(false);
        private final Queue<Long> taskIds = new ConcurrentLinkedQueue<>();
        private final CopyOnWriteArrayList<ScheduledFuture<?>> launchFutures = new CopyOnWriteArrayList<>();
    }
}
