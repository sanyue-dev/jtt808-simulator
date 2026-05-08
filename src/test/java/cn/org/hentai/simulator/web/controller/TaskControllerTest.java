package cn.org.hentai.simulator.web.controller;

import cn.org.hentai.simulator.service.task.SingleTaskLaunchRequest;
import cn.org.hentai.simulator.service.task.SingleTaskLaunchService;
import cn.org.hentai.simulator.service.task.TaskCreationResult;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskControllerTest
{
    @Test
    void runReturnsCreationResultForSingleTaskGroup()
    {
        TaskController controller = new TaskController();
        ReflectionTestUtils.setField(controller, "singleTaskLaunchService", new RecordingSingleTaskLaunchService());

        TaskCreationResult data = controller.run(10L, "京A00001", "DEVICE001", "13800000001", "0", "127.0.0.1", "20021");

        assertEquals("TG-1", data.getTaskGroupId());
        assertEquals("单车创建 #1", data.getTaskGroupDisplayName());
        assertEquals("single", data.getTaskGroupSource());
        assertEquals(1, data.getTargetTasks());
    }

    private static class RecordingSingleTaskLaunchService extends SingleTaskLaunchService
    {
        RecordingSingleTaskLaunchService()
        {
            super(null);
        }

        @Override
        public TaskCreationResult launch(SingleTaskLaunchRequest request)
        {
            assertEquals(10L, request.getRouteId());
            assertEquals("京A00001", request.getVehicleNumber());
            assertEquals("DEVICE001", request.getDeviceSn());
            assertEquals("13800000001", request.getSimNumber());
            return new TaskCreationResult("TG-1", "单车创建 #1", "single", 1);
        }
    }
}
