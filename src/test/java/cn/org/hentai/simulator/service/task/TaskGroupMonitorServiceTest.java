package cn.org.hentai.simulator.service.task;

import cn.org.hentai.simulator.domain.model.TaskInfo;
import cn.org.hentai.simulator.domain.model.TaskLifecycleObserver;
import cn.org.hentai.simulator.service.monitor.TaskRuntimeSummary;
import cn.org.hentai.simulator.service.monitor.TaskStopResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskGroupMonitorServiceTest
{
    @Test
    void singleTaskGroupAppearsInMonitorSnapshotAndRemainsAfterCompletion()
    {
        TaskGroupMonitorService service = service();

        TaskCreationResult creation = service.createGroup(TaskGroupSource.SINGLE, 1);
        service.recordTaskStarted(creation.getTaskGroupId(), 101L);

        TaskGroupMonitorSnapshot snapshot = service.snapshot();

        assertEquals(7L, snapshot.getRuntimeSummary().getTotalTasks());
        assertEquals(1, snapshot.getTaskGroups().size());
        TaskGroupSummary group = snapshot.getTaskGroups().get(0);
        assertEquals(creation.getTaskGroupId(), group.getTaskGroupId());
        assertEquals("single", group.getSource());
        assertEquals(1, group.getTargetTasks());
        assertEquals(1, group.getStartedTasks());
        assertEquals(1, group.getActiveTasks());
        assertEquals(0, group.getTerminatedTasks());
        assertEquals("running", group.getState());

        TaskLifecycleObserver observer = service.observer(creation.getTaskGroupId());
        observer.onTerminated(new TaskInfo().withId(101L));

        group = service.snapshot().getTaskGroups().get(0);
        assertEquals(0, group.getActiveTasks());
        assertEquals(1, group.getTerminatedTasks());
        assertEquals("completed", group.getState());
    }

    @Test
    void taskGroupHandlesTerminationBeforeStartRecord()
    {
        TaskGroupMonitorService service = service();
        TaskCreationResult creation = service.createGroup(TaskGroupSource.SINGLE, 1);

        TaskLifecycleObserver observer = service.observer(creation.getTaskGroupId());
        observer.onTerminated(new TaskInfo().withId(101L));
        service.recordTaskStarted(creation.getTaskGroupId(), 101L);

        TaskGroupSummary group = service.snapshot().getTaskGroups().get(0);
        assertEquals(1, group.getStartedTasks());
        assertEquals(0, group.getActiveTasks());
        assertEquals(1, group.getTerminatedTasks());
        assertEquals("completed", group.getState());
    }

    @Test
    void recordingStartedTaskAssignsTaskGroupIdentity()
    {
        RecordingTaskGroupAssigner assigner = new RecordingTaskGroupAssigner();
        TaskGroupMonitorService service = new TaskGroupMonitorService(new FixedRuntimeSummaryProvider(), taskIds -> new TaskStopResult(), assigner);

        TaskCreationResult creation = service.createGroup(TaskGroupSource.BATCH, 2);
        service.recordTaskStarted(creation.getTaskGroupId(), 101L);

        assertEquals(101L, assigner.taskId);
        assertEquals(creation.getTaskGroupId(), assigner.taskGroupId);
        assertEquals(creation.getTaskGroupDisplayName(), assigner.taskGroupDisplayName);
    }

    @Test
    void stoppingTaskGroupStopsOnlyActiveTasksAndSkipsAlreadyTerminatedTasks()
    {
        RecordingTaskStopper stopper = new RecordingTaskStopper();
        TaskGroupMonitorService service = service(stopper);
        TaskCreationResult creation = service.createGroup(TaskGroupSource.BATCH, 3);
        service.recordTaskStarted(creation.getTaskGroupId(), 101L);
        service.recordTaskStarted(creation.getTaskGroupId(), 102L);
        service.recordTaskStarted(creation.getTaskGroupId(), 103L);
        service.observer(creation.getTaskGroupId()).onTerminated(new TaskInfo().withId(102L));

        TaskStopResult result = service.stopTaskGroup(creation.getTaskGroupId());

        assertEquals(List.of(101L, 103L), stopper.requestedTaskIds);
        assertEquals(2L, result.getSucceeded());
        assertEquals(0L, result.getFailed());
    }

    @Test
    void stoppingTaskGroupExposesPartialFailureInSummary()
    {
        RecordingTaskStopper stopper = new RecordingTaskStopper();
        stopper.failedTaskId = 103L;
        TaskGroupMonitorService service = service(stopper);
        TaskCreationResult creation = service.createGroup(TaskGroupSource.BATCH, 2);
        service.recordTaskStarted(creation.getTaskGroupId(), 101L);
        service.recordTaskStarted(creation.getTaskGroupId(), 103L);

        TaskStopResult result = service.stopTaskGroup(creation.getTaskGroupId());

        assertEquals(1L, result.getSucceeded());
        assertEquals(1L, result.getFailed());
        assertEquals(103L, result.getFailures().get(0).getTaskId());
        TaskGroupSummary group = service.snapshot().getTaskGroups().get(0);
        assertEquals("failed", group.getState());
        assertEquals(1L, group.getStopSucceeded());
        assertEquals(1L, group.getStopFailed());
        assertEquals(103L, group.getStopFailures().get(0).getTaskId());
    }

    @Test
    void stoppingTaskGroupCompletesAfterAllStoppedTasksTerminate()
    {
        RecordingTaskStopper stopper = new RecordingTaskStopper();
        TaskGroupMonitorService service = service(stopper);
        TaskCreationResult creation = service.createGroup(TaskGroupSource.BATCH, 2);
        service.recordTaskStarted(creation.getTaskGroupId(), 101L);
        service.recordTaskStarted(creation.getTaskGroupId(), 102L);

        service.stopTaskGroup(creation.getTaskGroupId());

        TaskGroupSummary group = service.snapshot().getTaskGroups().get(0);
        assertEquals("stopping", group.getState());

        TaskLifecycleObserver observer = service.observer(creation.getTaskGroupId());
        observer.onTerminated(new TaskInfo().withId(101L));
        observer.onTerminated(new TaskInfo().withId(102L));

        group = service.snapshot().getTaskGroups().get(0);
        assertEquals("completed", group.getState());
        assertEquals(0, group.getActiveTasks());
        assertEquals(2, group.getTerminatedTasks());
    }

    private static class FixedRuntimeSummaryProvider implements TaskGroupMonitorService.RuntimeSummaryProvider
    {
        @Override
        public TaskRuntimeSummary getRuntimeSummary()
        {
            return new TaskRuntimeSummary(7L, 5L, 0L, 2L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0D, 0L, 0L, 0L, 0L, null, null);
        }
    }

    private static TaskGroupMonitorService service()
    {
        return service(taskIds -> new TaskStopResult());
    }

    private static TaskGroupMonitorService service(TaskGroupMonitorService.TaskStopper stopper)
    {
        return new TaskGroupMonitorService(new FixedRuntimeSummaryProvider(), stopper, (taskId, taskGroupId, taskGroupDisplayName) -> {});
    }

    private static class RecordingTaskStopper implements TaskGroupMonitorService.TaskStopper
    {
        private final List<Long> requestedTaskIds = new ArrayList<>();
        private Long failedTaskId = null;

        @Override
        public TaskStopResult stopTasks(Collection<Long> taskIds)
        {
            requestedTaskIds.addAll(taskIds);
            TaskStopResult result = new TaskStopResult();
            taskIds.forEach(taskId -> {
                if (taskId.equals(failedTaskId)) result.recordFailure(taskId, "stop failed");
                else result.recordSuccess();
            });
            return result;
        }
    }

    private static class RecordingTaskGroupAssigner implements TaskGroupMonitorService.TaskGroupAssigner
    {
        private long taskId;
        private String taskGroupId;
        private String taskGroupDisplayName;

        @Override
        public void assignTaskGroup(long taskId, String taskGroupId, String taskGroupDisplayName)
        {
            this.taskId = taskId;
            this.taskGroupId = taskGroupId;
            this.taskGroupDisplayName = taskGroupDisplayName;
        }
    }
}
