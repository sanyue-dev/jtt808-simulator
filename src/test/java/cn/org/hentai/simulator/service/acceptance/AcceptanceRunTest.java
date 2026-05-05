package cn.org.hentai.simulator.service.acceptance;

import cn.org.hentai.simulator.domain.model.TaskInfo;
import cn.org.hentai.simulator.domain.model.TerminalIdentity;
import org.junit.jupiter.api.Test;
import org.yzh.protocol.commons.JT808;
import org.yzh.protocol.t808.T0200;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AcceptanceRunTest
{
    @Test
    void aggregatesLifecycleMetricsAndTerminalRecord()
    {
        AcceptanceConfig config = new AcceptanceConfig();
        AcceptanceRun run = new AcceptanceRun(config);
        run.addRecord(new TerminalAcceptanceRecord(new TerminalIdentity("京000001", "A000001", "013800000001"), 1L));
        TaskInfo taskInfo = new TaskInfo().withId(1L);

        run.onConnected(taskInfo);
        run.onRegistrationSucceeded(taskInfo);
        run.onAuthenticationSucceeded(taskInfo);

        T0200 report = new T0200();
        report.setMessageId(JT808.位置信息汇报);
        run.onLocationReported(taskInfo, report);
        run.onDisconnected(taskInfo);
        run.onTerminated(taskInfo);

        AcceptanceRun.AcceptanceSummary summary = run.getSummary();
        TerminalAcceptanceRecord record = run.getRecords().iterator().next();

        assertEquals(1, summary.getConnectionSucceeded());
        assertEquals(1, summary.getRegistrationSucceeded());
        assertEquals(1, summary.getAuthenticationSucceeded());
        assertEquals(1, summary.getLocationReportSent());
        assertEquals(1, summary.getDisconnected());
        assertEquals(1, summary.getTerminated());
        assertEquals(1, record.getLocationReportSent());
        assertEquals("terminated", record.getStage());
    }

    @Test
    void recordsExplicitFailureStageAndReason()
    {
        AcceptanceRun run = new AcceptanceRun(new AcceptanceConfig());
        run.addRecord(new TerminalAcceptanceRecord(new TerminalIdentity("京000001", "A000001", "013800000001"), 1L));

        run.onAuthenticationFailed(new TaskInfo().withId(1L), "resultCode=1");
        run.onTerminated(new TaskInfo().withId(1L));

        AcceptanceRun.AcceptanceSummary summary = run.getSummary();
        TerminalAcceptanceRecord record = run.getRecords().iterator().next();

        assertEquals(1, summary.getAuthenticationFailed());
        assertEquals(1, summary.getTerminated());
        assertEquals("authentication_failed", record.getStage());
        assertEquals("resultCode=1", record.getFailureReason());
    }

    @Test
    void reportsAllRecordedTasksTerminatedOnlyAfterEveryRecordTerminates()
    {
        AcceptanceRun run = new AcceptanceRun(new AcceptanceConfig());
        run.addRecord(new TerminalAcceptanceRecord(new TerminalIdentity("京000001", "A000001", "013800000001"), 1L));
        run.addRecord(new TerminalAcceptanceRecord(new TerminalIdentity("京000002", "A000002", "013800000002"), 2L));

        run.finishing();
        run.onTerminated(new TaskInfo().withId(1L));

        assertEquals("finishing", run.getState());
        assertFalse(run.allRecordedTasksTerminated());

        run.onTerminated(new TaskInfo().withId(2L));

        assertTrue(run.allRecordedTasksTerminated());
    }

    @Test
    void removingUnstartedRecordKeepsTerminationWaitBoundedToStartedTasks()
    {
        AcceptanceRun run = new AcceptanceRun(new AcceptanceConfig());
        run.addRecord(new TerminalAcceptanceRecord(new TerminalIdentity("京000001", "A000001", "013800000001"), 1L));
        run.addRecord(new TerminalAcceptanceRecord(new TerminalIdentity("京000002", "A000002", "013800000002"), 2L));

        run.removeRecord(2L);
        run.onTerminated(new TaskInfo().withId(1L));

        assertEquals(1, run.getRecordCount());
        assertTrue(run.allRecordedTasksTerminated());
    }
}
