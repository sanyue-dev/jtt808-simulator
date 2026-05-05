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
import java.util.concurrent.TimeUnit;

@Service
public class AcceptanceHarnessService
{
    private static final Logger logger = LoggerFactory.getLogger(AcceptanceHarnessService.class);

    private final RouteService routeService;
    private final IdentityBatchGenerator identityBatchGenerator = new IdentityBatchGenerator();
    private final Map<String, AcceptanceRun> runs = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

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
            throw new RuntimeException("1k 验收启动失败，runId=" + run.getId() + "，已请求终止已启动任务", ex);
        }

        scheduler.schedule(() -> requestFinish(run, null), config.getRunDurationSeconds(), TimeUnit.SECONDS);
        return run;
    }

    private void launchTasks(AcceptanceConfig config, List<Route> routes, List<TerminalIdentity> identities, AcceptanceRun run)
    {
        TaskManager taskManager = TaskManager.getInstance();
        for (int i = 0; i < identities.size(); i++)
        {
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
            run.addRecord(record);
            try
            {
                taskManager.run(taskId, params, route.getId(), config.getReportIntervalSeconds(), run);
            }
            catch(RuntimeException ex)
            {
                run.removeRecord(taskId);
                throw ex;
            }
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
        run.finishing();
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
            failureReason = failureReason == null ? reason : failureReason + "; 终止失败: " + reason;
        }
        awaitFinish(run, failureReason);
    }

    private void awaitFinish(AcceptanceRun run, String failureReason)
    {
        if (run.allRecordedTasksTerminated())
        {
            if (failureReason == null) run.finish();
            else run.finishFailed(failureReason);
            return;
        }
        scheduler.schedule(() -> awaitFinish(run, failureReason), 100, TimeUnit.MILLISECONDS);
    }

    private void validate(AcceptanceConfig config)
    {
        if (config.getTerminalCount() != 1000) throw new IllegalArgumentException("1k 阶段验收必须启动 1000 个终端");
        if (config.getReportIntervalSeconds() < 1) throw new IllegalArgumentException("位置上报间隔必须大于 0 秒");
        if (config.getRunDurationSeconds() < 1) throw new IllegalArgumentException("运行时长必须大于 0 秒");
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
}
