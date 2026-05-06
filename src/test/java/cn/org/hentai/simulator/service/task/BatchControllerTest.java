package cn.org.hentai.simulator.service.task;

import cn.org.hentai.simulator.web.controller.BatchController;
import cn.org.hentai.simulator.web.vo.Result;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BatchControllerTest
{
    @Test
    void runReturnsPreflightDetailsWhenLaunchIsRejected()
    {
        PreflightCheckResult preflight = new PreflightCheckResult();
        preflight.fail("本机文件描述符余量不足: available=400, required=1064, open=100, max=500");
        preflight.setOpenFileDescriptorCount(100L);
        preflight.setMaxFileDescriptorCount(500L);
        preflight.setRequiredFileDescriptorCount(1064L);

        BatchController controller = new BatchController();
        ReflectionTestUtils.setField(controller, "taskBatchLaunchService", new RejectingTaskBatchLaunchService(preflight));

        Result result = controller.run(1000, null, "京%06d", "A%06d", "0138%08d", "127.0.0.1", "20021", 5, 0, 1000, 1);

        assertEquals(1, result.getError().getCode());
        assertEquals("本机文件描述符余量不足: available=400, required=1064, open=100, max=500", result.getError().getReason());

        BatchTaskLaunchResult data = (BatchTaskLaunchResult) result.getData();
        assertEquals(0, data.getScheduledTasks());
        assertEquals(0, data.getRampUpWindowCount());
        assertEquals(false, data.isAutoStopScheduled());
        assertEquals(preflight, data.getPreflight());
    }

    private static class RejectingTaskBatchLaunchService extends TaskBatchLaunchService
    {
        private final PreflightCheckResult preflight;

        RejectingTaskBatchLaunchService(PreflightCheckResult preflight)
        {
            super(null, null, null, null, null);
            this.preflight = preflight;
        }

        @Override
        public BatchTaskLaunchResult launch(BatchTaskLaunchRequest request)
        {
            throw new PreflightCheckException(preflight);
        }
    }
}
