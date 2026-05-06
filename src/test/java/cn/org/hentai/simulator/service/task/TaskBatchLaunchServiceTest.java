package cn.org.hentai.simulator.service.task;

import cn.org.hentai.simulator.domain.entity.Route;
import cn.org.hentai.simulator.service.RouteService;
import cn.org.hentai.simulator.service.monitor.TaskStopResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskBatchLaunchServiceTest
{
    private final RecordingTaskGateway taskGateway = new RecordingTaskGateway();
    private final RecordingScheduler launchScheduler = new RecordingScheduler();
    private final RecordingScheduler stopScheduler = new RecordingScheduler();
    private final RecordingCapacityProbe capacityProbe = new RecordingCapacityProbe();
    private final TaskBatchLaunchService service = new TaskBatchLaunchService(new FakeRouteService(), taskGateway, launchScheduler, stopScheduler, capacityProbe);

    @Test
    void validatesGeneralBatchLaunchConfig()
    {
        BatchTaskLaunchRequest request = validRequest();
        request.setTerminalCount(5000);

        assertDoesNotThrow(() -> service.validate(request));
    }

    @Test
    void rejectsUnsupportedBatchLaunchConfig()
    {
        BatchTaskLaunchRequest request = validRequest();
        request.setTerminalCount(100001);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.validate(request));

        assertEquals("终端数量必须在 1 到 100000 之间", ex.getMessage());
    }

    @Test
    void rejectsZeroTerminalCountWithoutDividingByZero()
    {
        BatchTaskLaunchRequest request = validRequest();
        request.setTerminalCount(0);
        request.setRunDurationSeconds(300);
        request.setRampUpBatchSize(0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.validate(request));

        assertEquals("终端数量必须在 1 到 100000 之间; ramp-up 批次大小必须大于 0", ex.getMessage());
    }

    @Test
    void rejectsRampUpWindowThatOutlivesRunDuration()
    {
        BatchTaskLaunchRequest request = validRequest();
        request.setTerminalCount(10000);
        request.setRunDurationSeconds(300);
        request.setRampUpBatchSize(100);
        request.setRampUpIntervalMillis(10000);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.validate(request));

        assertEquals("ramp-up 最后启动窗口必须早于运行时长: lastLaunchDelayMillis=990000, runDurationMillis=300000", ex.getMessage());
    }

    @Test
    void failsPreflightWhenFileDescriptorCapacityIsInsufficient()
    {
        BatchTaskLaunchRequest request = validRequest();
        request.setTerminalCount(1000);
        capacityProbe.openFileDescriptorCount = 100L;
        capacityProbe.maxFileDescriptorCount = 500L;

        PreflightCheckResult result = service.preflight(request);

        assertEquals(true, result.hasFailures());
        assertEquals("本机文件描述符余量不足: available=400, required=1064, open=100, max=500", result.getFailures().get(0));
        assertEquals(100L, result.getOpenFileDescriptorCount());
        assertEquals(500L, result.getMaxFileDescriptorCount());
        assertEquals(1064L, result.getRequiredFileDescriptorCount());
    }

    @Test
    void preflightLaunchFailuresCarryDiagnosticResult()
    {
        BatchTaskLaunchRequest request = validRequest();
        request.setTerminalCount(1000);
        capacityProbe.openFileDescriptorCount = 100L;
        capacityProbe.maxFileDescriptorCount = 500L;

        PreflightCheckException ex = assertThrows(PreflightCheckException.class, () -> service.launch(request));

        assertEquals("本机文件描述符余量不足: available=400, required=1064, open=100, max=500", ex.getMessage());
        assertEquals(ex.getPreflight().getFailures().get(0), ex.getMessage());
        assertEquals(100L, ex.getPreflight().getOpenFileDescriptorCount());
        assertEquals(500L, ex.getPreflight().getMaxFileDescriptorCount());
        assertEquals(1064L, ex.getPreflight().getRequiredFileDescriptorCount());
    }

    @Test
    void warnsWhenSingleDestinationEphemeralPortCapacityIsRisky()
    {
        BatchTaskLaunchRequest request = validRequest();
        request.setTerminalCount(30000);

        PreflightCheckResult result = service.preflight(request);

        assertEquals(false, result.hasFailures());
        assertEquals("单源 IP 到同一服务端地址端口的临时端口容量存在风险: requested=30000, estimatedCapacity=28000", result.getWarnings().get(0));
        assertEquals(28000L, result.getSingleDestinationEphemeralPortCapacity());
    }

    @Test
    void buildsRampUpLaunchWindows()
    {
        List<TaskLaunchWindow> windows = service.buildLaunchWindows(10000, 100, 1000);

        assertEquals(100, windows.size());
        assertEquals(0, windows.get(0).getStartIndex());
        assertEquals(100, windows.get(0).getEndIndex());
        assertEquals(0L, windows.get(0).getDelayMillis());
        assertEquals(9900, windows.get(99).getStartIndex());
        assertEquals(10000, windows.get(99).getEndIndex());
        assertEquals(99000L, windows.get(99).getDelayMillis());
    }

    @Test
    void schedulesBatchTasksWithGeneratedIdentitiesAndAutoStop()
    {
        BatchTaskLaunchRequest request = validRequest();
        request.setTerminalCount(3);
        request.setRunDurationSeconds(30);
        request.setRampUpBatchSize(2);
        request.setRampUpIntervalMillis(1000);

        BatchTaskLaunchResult result = service.launch(request);

        assertEquals(3, result.getScheduledTasks());
        assertEquals(2, result.getRampUpWindowCount());
        assertEquals(true, result.isAutoStopScheduled());
        assertEquals(false, result.getPreflight().hasFailures());
        assertEquals(List.of(0L, 1000L), launchScheduler.delays);
        assertEquals(List.of(30000L), stopScheduler.delays);

        launchScheduler.runAll();

        assertEquals(3, taskGateway.started.size());
        assertEquals("京000001", taskGateway.started.get(0).params.get("vehicle.number"));
        assertEquals("A000001", taskGateway.started.get(0).params.get("device.sn"));
        assertEquals("013800000001", taskGateway.started.get(0).params.get("device.sim"));
        assertEquals(10L, taskGateway.started.get(0).routeId);
        assertEquals(11L, taskGateway.started.get(1).routeId);
        assertEquals(10L, taskGateway.started.get(2).routeId);

        stopScheduler.runAll();

        assertEquals(Set.of(1L, 2L, 3L), taskGateway.terminatedTaskIds);
    }

    @Test
    void exposesCurrentBatchLaunchProgressWhileRampUpWindowsRun()
    {
        BatchTaskLaunchRequest request = validRequest();
        request.setTerminalCount(3);
        request.setRampUpBatchSize(2);
        request.setRampUpIntervalMillis(1000);

        service.launch(request);

        BatchTaskLaunchProgress progress = service.currentProgress();
        assertEquals(false, progress.isEmpty());
        assertEquals("launching", progress.getState());
        assertEquals(3, progress.getTargetTasks());
        assertEquals(2, progress.getRampUpWindowCount());
        assertEquals(0, progress.getExecutedWindowCount());
        assertEquals(0, progress.getStartedTasks());

        launchScheduler.runNext();

        progress = service.currentProgress();
        assertEquals("launching", progress.getState());
        assertEquals(1, progress.getExecutedWindowCount());
        assertEquals(2, progress.getStartedTasks());

        launchScheduler.runNext();

        progress = service.currentProgress();
        assertEquals("running", progress.getState());
        assertEquals(2, progress.getExecutedWindowCount());
        assertEquals(3, progress.getStartedTasks());
    }

    @Test
    void recordsStopResultInCurrentBatchLaunchProgress()
    {
        BatchTaskLaunchRequest request = validRequest();
        request.setTerminalCount(3);
        request.setRunDurationSeconds(30);
        request.setRampUpBatchSize(3);

        service.launch(request);
        launchScheduler.runAll();

        stopScheduler.runAll();

        BatchTaskLaunchProgress progress = service.currentProgress();
        assertEquals("completed", progress.getState());
        assertEquals(3, progress.getStartedTasks());
        assertEquals(3L, progress.getStopSucceeded());
        assertEquals(0L, progress.getStopFailed());
    }

    @Test
    void recordsTaskStoppedAfterAutoStopSnapshot()
    {
        BatchTaskLaunchRequest request = validRequest();
        request.setTerminalCount(1);
        request.setRunDurationSeconds(30);
        taskGateway.afterRun = stopScheduler::runAll;

        service.launch(request);
        launchScheduler.runNext();

        BatchTaskLaunchProgress progress = service.currentProgress();
        assertEquals("completed", progress.getState());
        assertEquals(1, progress.getStartedTasks());
        assertEquals(1L, progress.getStopSucceeded());
        assertEquals(Set.of(1L), taskGateway.terminatedTaskIds);
    }

    @Test
    void recordsLaunchFailureInCurrentBatchLaunchProgress()
    {
        BatchTaskLaunchRequest request = validRequest();
        request.setTerminalCount(3);
        request.setRampUpBatchSize(3);
        taskGateway.failOnRun = true;

        service.launch(request);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> launchScheduler.runAll());
        assertEquals("route start failed", ex.getMessage());

        BatchTaskLaunchProgress progress = service.currentProgress();
        assertEquals("failed", progress.getState());
        assertEquals(0, progress.getStartedTasks());
        assertEquals("route start failed", progress.getFailureReason());
    }

    @Test
    void returnsEmptyProgressBeforeAnyBatchLaunch()
    {
        BatchTaskLaunchProgress progress = service.currentProgress();

        assertEquals(true, progress.isEmpty());
        assertEquals("empty", progress.getState());
    }

    private BatchTaskLaunchRequest validRequest()
    {
        BatchTaskLaunchRequest request = new BatchTaskLaunchRequest();
        request.setTerminalCount(1000);
        request.setReportIntervalSeconds(5);
        request.setRunDurationSeconds(0);
        request.setRampUpBatchSize(1000);
        request.setRampUpIntervalMillis(1);
        request.setServerAddress("127.0.0.1");
        request.setServerPort(20021);
        return request;
    }

    private static class FakeRouteService extends RouteService
    {
        @Override
        public List<Route> list()
        {
            return List.of(new Route().withId(10L), new Route().withId(11L));
        }

        @Override
        public Route getById(Long id)
        {
            return list().stream().filter(route -> route.getId().equals(id)).findFirst().orElse(null);
        }
    }

    private static class RecordingTaskGateway implements TaskBatchLaunchService.TaskGateway
    {
        private long nextTaskId = 1L;
        private final List<StartedTask> started = new ArrayList<>();
        private final Set<Long> terminatedTaskIds = new HashSet<>();
        private boolean failOnRun = false;
        private Runnable afterRun = null;

        @Override
        public long reserveIndexes(int count)
        {
            return 1L;
        }

        @Override
        public long nextTaskId()
        {
            return nextTaskId++;
        }

        @Override
        public void run(long taskId, Map<String, String> params, Long routeId, int reportIntervalSeconds)
        {
            if (failOnRun) throw new RuntimeException("route start failed");
            started.add(new StartedTask(taskId, params, routeId, reportIntervalSeconds));
            if (afterRun != null) afterRun.run();
        }

        @Override
        public TaskStopResult terminateTasks(Collection<Long> taskIds)
        {
            terminatedTaskIds.addAll(taskIds);
            TaskStopResult result = new TaskStopResult();
            taskIds.forEach(id -> result.recordSuccess());
            return result;
        }
    }

    private record StartedTask(long taskId, Map<String, String> params, Long routeId, int reportIntervalSeconds)
    {
    }

    private static class RecordingScheduler implements TaskBatchLaunchService.TaskScheduler
    {
        private final List<Long> delays = new ArrayList<>();
        private final List<Runnable> tasks = new ArrayList<>();

        @Override
        public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit)
        {
            delays.add(unit.toMillis(delay));
            tasks.add(task);
            return new CompletedFuture();
        }

        void runAll()
        {
            tasks.forEach(Runnable::run);
        }

        void runNext()
        {
            tasks.remove(0).run();
        }
    }

    private static class RecordingCapacityProbe implements TaskBatchLaunchService.CapacityProbe
    {
        private long openFileDescriptorCount = 10L;
        private long maxFileDescriptorCount = 200000L;
        private long singleDestinationEphemeralPortCapacity = 28000L;

        @Override
        public long openFileDescriptorCount()
        {
            return openFileDescriptorCount;
        }

        @Override
        public long maxFileDescriptorCount()
        {
            return maxFileDescriptorCount;
        }

        @Override
        public long singleDestinationEphemeralPortCapacity()
        {
            return singleDestinationEphemeralPortCapacity;
        }
    }

    private static class CompletedFuture implements ScheduledFuture<Object>
    {
        @Override public long getDelay(TimeUnit unit) { return 0; }
        @Override public int compareTo(java.util.concurrent.Delayed o) { return 0; }
        @Override public boolean cancel(boolean mayInterruptIfRunning) { return false; }
        @Override public boolean isCancelled() { return false; }
        @Override public boolean isDone() { return true; }
        @Override public Object get() { return null; }
        @Override public Object get(long timeout, TimeUnit unit) { return null; }
    }
}
