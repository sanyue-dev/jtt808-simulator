package cn.org.hentai.simulator.service.task;

import cn.org.hentai.simulator.domain.model.TaskInfo;
import cn.org.hentai.simulator.domain.model.TaskLifecycleObserver;
import cn.org.hentai.simulator.service.monitor.TaskRuntimeSummary;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskGroupMonitorServiceTest
{
    @Test
    void singleTaskGroupAppearsInMonitorSnapshotAndRemainsAfterCompletion()
    {
        TaskGroupMonitorService service = new TaskGroupMonitorService(new FixedRuntimeSummaryProvider());

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

    private static class FixedRuntimeSummaryProvider implements TaskGroupMonitorService.RuntimeSummaryProvider
    {
        @Override
        public TaskRuntimeSummary getRuntimeSummary()
        {
            return new TaskRuntimeSummary(7L, 5L, 0L, 2L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0D, 0L, 0L, 0L, 0L, null, null);
        }
    }
}
