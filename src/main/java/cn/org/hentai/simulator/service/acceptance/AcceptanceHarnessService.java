package cn.org.hentai.simulator.service.acceptance;

import cn.org.hentai.simulator.domain.entity.Route;
import cn.org.hentai.simulator.domain.model.TerminalIdentity;
import cn.org.hentai.simulator.service.RouteService;
import cn.org.hentai.simulator.service.TaskManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class AcceptanceHarnessService
{
    private static final Logger logger = LoggerFactory.getLogger(AcceptanceHarnessService.class);

    private final RouteService routeService;
    private final IdentityBatchGenerator identityBatchGenerator = new IdentityBatchGenerator();
    private final Map<String, AcceptanceRun> runs = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledThreadPoolExecutor launchScheduler = newLaunchScheduler();

    @Autowired
    public AcceptanceHarnessService(RouteService routeService)
    {
        this.routeService = routeService;
    }

    public AcceptanceRun start(AcceptanceConfig config)
    {
        validate(config);
        List<Route> routes = resolveRoutes(config.getRouteIds());
        List<TerminalIdentity> identities = identityBatchGenerator.generate(config.getTerminalCount(), TaskManager.getInstance().reserveIndexes(config.getTerminalCount()), config.getVehicleNumberPattern(), config.getDeviceSnPattern(), config.getSimNumberPattern());
        AcceptanceRun run = new AcceptanceRun(config);
        runs.put(run.getId(), run);

        try
        {
            launchTasks(config, routes, identities, run);
        }
        catch(RuntimeException ex)
        {
            requestFinish(run, "启动失败: " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
            throw new RuntimeException("验收启动失败，runId=" + run.getId() + "，已请求终止已启动任务", ex);
        }

        scheduler.schedule(() -> requestFinish(run, null), config.getRunDurationSeconds(), TimeUnit.SECONDS);
        return run;
    }

    private ScheduledThreadPoolExecutor newLaunchScheduler()
    {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        executor.setRemoveOnCancelPolicy(true);
        return executor;
    }

    private void launchTasks(AcceptanceConfig config, List<Route> routes, List<TerminalIdentity> identities, AcceptanceRun run)
    {
        TaskManager taskManager = TaskManager.getInstance();
        for (LaunchWindow window : buildLaunchWindows(config.getTerminalCount(), config.getRampUpBatchSize(), config.getRampUpIntervalMillis()))
        {
            if (run.canLaunch() == false) return;
            ScheduledFuture<?> launchFuture = launchScheduler.schedule(() -> {
                if (run.canLaunch() == false) return;
                try
                {
                    launchWindow(config, routes, identities, run, taskManager, window);
                }
                catch(RuntimeException ex)
                {
                    logger.error("验收启动批次失败: runId={}, startIndex={}, endIndex={}, delayMillis={}",
                            run.getId(), window.getStartIndex(), window.getEndIndex(), window.getDelayMillis(), ex);
                    requestFinish(run, "启动失败: " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
                }
            }, window.getDelayMillis(), TimeUnit.MILLISECONDS);
            if (run.addLaunchFuture(launchFuture) == false) return;
        }
    }

    void launchWindow(AcceptanceConfig config, List<Route> routes, List<TerminalIdentity> identities, AcceptanceRun run, TaskManager taskManager, LaunchWindow window)
    {
        for (int i = window.getStartIndex(); i < window.getEndIndex(); i++)
        {
            if (run.canLaunch() == false) return;

            TerminalIdentity identity = identities.get(i);
            Route route = routes.get(i % routes.size());
            Map<String, String> params = new HashMap<>();
            params.put("server.address", config.getServerAddress());
            params.put("server.port", String.valueOf(config.getServerPort()));
            params.put("mode", "stress");
            params.put("vehicle.number", identity.getVehicleNumber());
            params.put("device.sn", identity.getDeviceSn());
            params.put("device.sim", identity.getSimNumber());
            params.put("mileages", "0");

            long taskId = taskManager.nextTaskId();
            TerminalAcceptanceRecord record = new TerminalAcceptanceRecord(identity, taskId);
            boolean launched = run.launchIfRunning(record, () -> taskManager.run(taskId, params, route.getId(), config.getReportIntervalSeconds(), run));
            if (launched == false) return;
        }
    }

    public AcceptanceRun get(String runId)
    {
        AcceptanceRun run = runs.get(runId);
        if (run == null) throw new IllegalArgumentException("验收运行不存在: " + runId);
        return run;
    }

    private void requestFinish(AcceptanceRun run, String failureReason)
    {
        run.recordFinishFailure(failureReason);
        if (run.beginFinishing() == false) return;
        run.cancelPendingLaunches();
        RuntimeException failure = null;
        for (TerminalAcceptanceRecord record : run.getRecords())
        {
            try
            {
                TaskManager.getInstance().terminate(record.getTaskId());
            }
            catch(RuntimeException ex)
            {
                logger.error("验收任务终止失败: runId={}, taskId={}", run.getId(), record.getTaskId(), ex);
                if (failure == null) failure = ex;
            }
        }
        if (failure != null)
        {
            String reason = failure.getClass().getSimpleName() + ": " + failure.getMessage();
            run.recordFinishFailure("终止失败: " + reason);
        }
        awaitFinish(run);
    }

    private void awaitFinish(AcceptanceRun run)
    {
        if (run.allRecordedTasksTerminated())
        {
            run.finish();
            return;
        }
        scheduler.schedule(() -> awaitFinish(run), 100, TimeUnit.MILLISECONDS);
    }

    void validate(AcceptanceConfig config)
    {
        if (config.getTerminalCount() != 1000 && config.getTerminalCount() != 10000)
            throw new IllegalArgumentException("验收阶段仅支持 1000 或 10000 个终端");
        if (config.getReportIntervalSeconds() < 1) throw new IllegalArgumentException("位置上报间隔必须大于 0 秒");
        if (config.getRunDurationSeconds() < 1) throw new IllegalArgumentException("运行时长必须大于 0 秒");
        if (config.getRampUpBatchSize() < 1) throw new IllegalArgumentException("ramp-up 批次大小必须大于 0");
        if (config.getRampUpIntervalMillis() < 1) throw new IllegalArgumentException("ramp-up 间隔必须大于 0 毫秒");
        long launchWindowCount = (config.getTerminalCount() + (long) config.getRampUpBatchSize() - 1L) / config.getRampUpBatchSize();
        long lastLaunchDelayMillis = (launchWindowCount - 1L) * config.getRampUpIntervalMillis();
        long runDurationMillis = config.getRunDurationSeconds() * 1000L;
        if (lastLaunchDelayMillis >= runDurationMillis)
            throw new IllegalArgumentException("ramp-up 最后启动窗口必须早于运行时长: lastLaunchDelayMillis=" + lastLaunchDelayMillis + ", runDurationMillis=" + runDurationMillis);
        if (config.getServerAddress() == null || config.getServerAddress().isBlank()) throw new IllegalArgumentException("目标服务端地址不能为空");
        if (config.getServerPort() < 1 || config.getServerPort() > 65535) throw new IllegalArgumentException("目标服务端端口非法: " + config.getServerPort());
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
        if (routes.isEmpty()) throw new IllegalArgumentException("没有可用于验收的线路");
        return routes;
    }

    List<LaunchWindow> buildLaunchWindows(int terminalCount, int batchSize, long intervalMillis)
    {
        List<LaunchWindow> windows = new ArrayList<>();
        for (int start = 0, batchIndex = 0; start < terminalCount; start += batchSize, batchIndex++)
        {
            int end = Math.min(start + batchSize, terminalCount);
            windows.add(new LaunchWindow(start, end, batchIndex * intervalMillis));
        }
        return windows;
    }

    static final class LaunchWindow
    {
        private final int startIndex;
        private final int endIndex;
        private final long delayMillis;

        LaunchWindow(int startIndex, int endIndex, long delayMillis)
        {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.delayMillis = delayMillis;
        }

        int getStartIndex()
        {
            return startIndex;
        }

        int getEndIndex()
        {
            return endIndex;
        }

        long getDelayMillis()
        {
            return delayMillis;
        }
    }
}
