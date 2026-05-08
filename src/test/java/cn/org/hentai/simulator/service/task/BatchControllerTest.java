package cn.org.hentai.simulator.service.task;

import cn.org.hentai.simulator.web.controller.BatchController;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

        PreflightCheckException ex = assertThrows(PreflightCheckException.class, () ->
            controller.run(1000, null, "京%06d", "A%06d", "0138%08d", "127.0.0.1", "20021", 5, 0, 1000, 1));

        assertEquals("本机文件描述符余量不足: available=400, required=1064, open=100, max=500", ex.getMessage());
        assertEquals(preflight, ex.getPreflight());
    }

    @Test
    void progressReturnsCurrentBatchLaunchProgress()
    {
        BatchTaskLaunchProgress progress = BatchTaskLaunchProgress.of("launching", 1000, 10, 3, 300, true, null, 0L, 0L);

        BatchController controller = new BatchController();
        ReflectionTestUtils.setField(controller, "taskBatchLaunchService", new ProgressTaskBatchLaunchService(progress));

        BatchTaskLaunchProgress result = controller.progress();

        assertEquals(progress, result);
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

    private static class ProgressTaskBatchLaunchService extends TaskBatchLaunchService
    {
        private final BatchTaskLaunchProgress progress;

        ProgressTaskBatchLaunchService(BatchTaskLaunchProgress progress)
        {
            super(null, null, null, null, null);
            this.progress = progress;
        }

        @Override
        public BatchTaskLaunchProgress currentProgress()
        {
            return progress;
        }
    }
}
