package cn.org.hentai.simulator.web.controller;

import cn.org.hentai.simulator.service.task.TaskGroupMonitorService;
import cn.org.hentai.simulator.service.task.TaskGroupMonitorSnapshot;
import cn.org.hentai.simulator.service.monitor.TaskStopResult;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskGroupMonitorControllerTest
{
    @Test
    void snapshotReturnsTaskGroupMonitorSnapshot()
    {
        TaskGroupMonitorController controller = new TaskGroupMonitorController();
        TaskGroupMonitorSnapshot snapshot = new TaskGroupMonitorSnapshot(null, List.of());
        ReflectionTestUtils.setField(controller, "taskGroupMonitorService", new SnapshotTaskGroupMonitorService(snapshot));

        TaskGroupMonitorSnapshot result = controller.snapshot();

        assertEquals(snapshot, result);
    }

    @Test
    void indexReturnsTaskGroupMonitorTemplate()
    {
        assertEquals("task-group-monitor", new TaskGroupMonitorController().index());
    }

    @Test
    void stopReturnsTaskGroupStopResult()
    {
        TaskGroupMonitorController controller = new TaskGroupMonitorController();
        TaskStopResult stopResult = new TaskStopResult();
        stopResult.recordSuccess();
        StopTaskGroupMonitorService service = new StopTaskGroupMonitorService(stopResult);
        ReflectionTestUtils.setField(controller, "taskGroupMonitorService", service);

        TaskStopResult result = controller.stop("TG-1");

        assertEquals("TG-1", service.taskGroupId);
        assertEquals(stopResult, result);
    }

    private static class SnapshotTaskGroupMonitorService extends TaskGroupMonitorService
    {
        private final TaskGroupMonitorSnapshot snapshot;

        SnapshotTaskGroupMonitorService(TaskGroupMonitorSnapshot snapshot)
        {
            super(() -> null);
            this.snapshot = snapshot;
        }

        @Override
        public TaskGroupMonitorSnapshot snapshot()
        {
            return snapshot;
        }
    }

    private static class StopTaskGroupMonitorService extends TaskGroupMonitorService
    {
        private final TaskStopResult stopResult;
        private String taskGroupId;

        StopTaskGroupMonitorService(TaskStopResult stopResult)
        {
            super(() -> null);
            this.stopResult = stopResult;
        }

        @Override
        public TaskStopResult stopTaskGroup(String taskGroupId)
        {
            this.taskGroupId = taskGroupId;
            return stopResult;
        }
    }
}
