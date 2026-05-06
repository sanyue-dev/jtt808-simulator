package cn.org.hentai.simulator.service.acceptance;

import cn.org.hentai.simulator.domain.model.TaskInfo;
import cn.org.hentai.simulator.domain.model.TerminalIdentity;
import org.junit.jupiter.api.Test;
import org.yzh.protocol.commons.JT808;
import org.yzh.protocol.t808.T0200;

import java.util.concurrent.Delayed;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    void preservesTerminatedStageWhenDisconnectedArrivesLater()
    {
        AcceptanceRun run = new AcceptanceRun(new AcceptanceConfig());
        run.addRecord(new TerminalAcceptanceRecord(new TerminalIdentity("京000001", "A000001", "013800000001"), 1L));
        TaskInfo taskInfo = new TaskInfo().withId(1L);

        run.onTerminated(taskInfo);
        run.onDisconnected(taskInfo);

        AcceptanceRun.AcceptanceSummary summary = run.getSummary();
        TerminalAcceptanceRecord record = run.getRecords().iterator().next();

        assertEquals(1, summary.getTerminated());
        assertEquals(1, summary.getDisconnected());
        assertEquals("terminated", record.getStage());
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

    @Test
    void ignoresTerminationCounterWhenRecordIsMissing()
    {
        AcceptanceRun run = new AcceptanceRun(new AcceptanceConfig());
        run.addRecord(new TerminalAcceptanceRecord(new TerminalIdentity("京000001", "A000001", "013800000001"), 1L));
        run.addRecord(new TerminalAcceptanceRecord(new TerminalIdentity("京000002", "A000002", "013800000002"), 2L));

        assertThrows(IllegalStateException.class, () -> run.onTerminated(new TaskInfo().withId(3L)));
        run.onTerminated(new TaskInfo().withId(1L));

        assertFalse(run.allRecordedTasksTerminated());
    }

    @Test
    void cancelsPendingLaunchFutures()
    {
        AcceptanceRun run = new AcceptanceRun(new AcceptanceConfig());
        TestScheduledFuture pendingLaunch = new TestScheduledFuture(false);
        TestScheduledFuture completedLaunch = new TestScheduledFuture(true);
        assertTrue(run.addLaunchFuture(pendingLaunch));
        assertTrue(run.addLaunchFuture(completedLaunch));

        run.cancelPendingLaunches();

        assertTrue(pendingLaunch.cancelled.get());
        assertFalse(completedLaunch.cancelled.get());
    }

    @Test
    void cancelsNewLaunchFutureAfterFinishingBegins()
    {
        AcceptanceRun run = new AcceptanceRun(new AcceptanceConfig());
        TestScheduledFuture launch = new TestScheduledFuture(false);

        assertTrue(run.beginFinishing());

        assertFalse(run.addLaunchFuture(launch));
        assertTrue(launch.cancelled.get());
    }

    @Test
    void skipsLaunchRecordAfterFinishingBegins()
    {
        AcceptanceRun run = new AcceptanceRun(new AcceptanceConfig());
        AtomicBoolean launched = new AtomicBoolean(false);

        assertTrue(run.beginFinishing());
        boolean accepted = run.launchIfRunning(new TerminalAcceptanceRecord(new TerminalIdentity("京000001", "A000001", "013800000001"), 1L), () -> launched.set(true));

        assertFalse(accepted);
        assertFalse(launched.get());
        assertEquals(0, run.getRecordCount());
    }

    @Test
    void removesLaunchRecordWhenTaskLaunchFails()
    {
        AcceptanceRun run = new AcceptanceRun(new AcceptanceConfig());
        RuntimeException failure = new RuntimeException("boom");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> run.launchIfRunning(
                new TerminalAcceptanceRecord(new TerminalIdentity("京000001", "A000001", "013800000001"), 1L),
                () -> { throw failure; }
        ));

        assertEquals(failure, ex);
        assertEquals(0, run.getRecordCount());
    }

    @Test
    void preservesFinishFailureRecordedAfterFinishingBegins()
    {
        AcceptanceRun run = new AcceptanceRun(new AcceptanceConfig());

        assertTrue(run.beginFinishing());
        run.recordFinishFailure("启动失败: RuntimeException: boom");
        run.finish();

        assertEquals("finish_failed", run.getState());
        assertEquals("启动失败: RuntimeException: boom", run.getFinishFailureReason());
    }

    @Test
    void exposesFinishFailureRecordedAfterFinishedState()
    {
        AcceptanceRun run = new AcceptanceRun(new AcceptanceConfig());

        run.finish();
        run.recordFinishFailure("启动失败: RuntimeException: boom");

        assertEquals("finish_failed", run.getState());
        assertEquals("启动失败: RuntimeException: boom", run.getFinishFailureReason());
    }

    private static class TestScheduledFuture implements ScheduledFuture<Object>
    {
        private final boolean done;
        private final AtomicBoolean cancelled = new AtomicBoolean(false);

        private TestScheduledFuture(boolean done)
        {
            this.done = done;
        }

        @Override
        public long getDelay(TimeUnit unit)
        {
            return 0;
        }

        @Override
        public int compareTo(Delayed other)
        {
            return 0;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning)
        {
            cancelled.set(true);
            return true;
        }

        @Override
        public boolean isCancelled()
        {
            return cancelled.get();
        }

        @Override
        public boolean isDone()
        {
            return done;
        }

        @Override
        public Object get()
        {
            return null;
        }

        @Override
        public Object get(long timeout, TimeUnit unit)
        {
            return null;
        }
    }
}
