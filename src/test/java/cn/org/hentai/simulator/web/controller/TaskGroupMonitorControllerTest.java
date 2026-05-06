package cn.org.hentai.simulator.web.controller;

import cn.org.hentai.simulator.service.task.TaskGroupMonitorService;
import cn.org.hentai.simulator.service.task.TaskGroupMonitorSnapshot;
import cn.org.hentai.simulator.web.vo.Result;
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

        Result result = controller.snapshot();

        assertEquals(0, result.getError().getCode());
        assertEquals(snapshot, result.getData());
    }

    @Test
    void indexReturnsTaskGroupMonitorTemplate()
    {
        assertEquals("task-group-monitor", new TaskGroupMonitorController().index());
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
}
