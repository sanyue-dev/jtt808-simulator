package cn.org.hentai.simulator.service.monitor;

import cn.org.hentai.simulator.domain.model.TaskInfo;
import org.junit.jupiter.api.Test;
import org.yzh.protocol.commons.JT808;
import org.yzh.protocol.t808.T0100;
import org.yzh.protocol.t808.T0200;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskRuntimeMetricsTest
{
    @Test
    void summarizesTaskLifecycleEvents()
    {
        TaskRuntimeMetrics metrics = new TaskRuntimeMetrics();
        TaskInfo taskInfo = new TaskInfo().withId(1L);

        metrics.onConnected(taskInfo);
        metrics.onConnectionFailed(taskInfo, new RuntimeException("connect refused"));
        metrics.onRegistrationSucceeded(taskInfo);
        metrics.onRegistrationFailed(taskInfo, "duplicate terminal");
        metrics.onAuthenticationSucceeded(taskInfo);
        metrics.onAuthenticationFailed(taskInfo, "bad token");
        T0200 locationReport = new T0200();
        locationReport.setMessageId(JT808.位置信息汇报);
        metrics.onLocationReported(taskInfo, locationReport);
        metrics.onDisconnected(taskInfo);
        metrics.onTerminated(taskInfo);
        metrics.onSendFailed(taskInfo, null, new RuntimeException("write failed"));
        metrics.onProtocolException(taskInfo, new RuntimeException("decode failed"));

        TaskRuntimeSummary summary = metrics.summary(3L, 2L, 0L, 1L);

        assertEquals(3L, summary.getTotalTasks());
        assertEquals(2L, summary.getActiveTasks());
        assertEquals(0L, summary.getParkingTasks());
        assertEquals(1L, summary.getTerminatedTasks());
        assertEquals(1L, summary.getConnectionSucceeded());
        assertEquals(1L, summary.getConnectionFailed());
        assertEquals(1L, summary.getRegistrationSucceeded());
        assertEquals(1L, summary.getRegistrationFailed());
        assertEquals(1L, summary.getAuthenticationSucceeded());
        assertEquals(1L, summary.getAuthenticationFailed());
        assertEquals(1L, summary.getLocationReportSent());
        assertEquals(1L, summary.getDisconnected());
        assertEquals(1L, summary.getTerminated());
        assertEquals(1L, summary.getSendFailed());
        assertEquals(1L, summary.getProtocolExceptions());
    }

    @Test
    void countsOnlyT0200AsLocationReport()
    {
        TaskRuntimeMetrics metrics = new TaskRuntimeMetrics();
        TaskInfo taskInfo = new TaskInfo().withId(1L);
        T0100 registration = new T0100();
        registration.setMessageId(JT808.终端注册);
        T0200 locationReport = new T0200();
        locationReport.setMessageId(JT808.位置信息汇报);

        metrics.onLocationReported(taskInfo, registration);
        metrics.onLocationReported(taskInfo, locationReport);

        assertEquals(1L, metrics.summary(0L, 0L, 0L, 0L).getLocationReportSent());
    }

    @Test
    void exposesFailureStageAndReasonOnTaskInfo()
    {
        TaskRuntimeMetrics metrics = new TaskRuntimeMetrics();
        TaskInfo taskInfo = new TaskInfo().withId(7L);

        metrics.onConnectionFailed(taskInfo, new RuntimeException("connection refused"));
        metrics.applyFailureInfo(taskInfo);

        assertEquals("connection_failed", taskInfo.getFailureStage());
        assertEquals("RuntimeException: connection refused", taskInfo.getFailureReason());
    }
}
