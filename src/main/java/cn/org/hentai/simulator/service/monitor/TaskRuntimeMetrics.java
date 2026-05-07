package cn.org.hentai.simulator.service.monitor;

import cn.org.hentai.simulator.domain.model.TaskInfo;
import cn.org.hentai.simulator.domain.model.TaskLifecycleObserver;
import cn.org.hentai.simulator.engine.runner.RunnerManager;
import org.yzh.protocol.basics.JTMessage;
import org.yzh.protocol.commons.JT808;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TaskRuntimeMetrics implements TaskLifecycleObserver
{
    private final RuntimeResourceProbe runtimeResourceProbe;
    private final AtomicLong connectionSucceeded = new AtomicLong();
    private final AtomicLong connectionFailed = new AtomicLong();
    private final AtomicLong registrationSucceeded = new AtomicLong();
    private final AtomicLong registrationFailed = new AtomicLong();
    private final AtomicLong authenticationSucceeded = new AtomicLong();
    private final AtomicLong authenticationFailed = new AtomicLong();
    private final AtomicLong locationReportSent = new AtomicLong();
    private final AtomicLong disconnected = new AtomicLong();
    private final AtomicLong terminated = new AtomicLong();
    private final AtomicLong sendFailed = new AtomicLong();
    private final AtomicLong protocolExceptions = new AtomicLong();
    private final ConcurrentMap<Long, FailureInfo> failures = new ConcurrentHashMap<>();

    public TaskRuntimeMetrics()
    {
        this.runtimeResourceProbe = new RuntimeResourceProbe();
    }

    @Override
    public void onConnected(TaskInfo taskInfo)
    {
        connectionSucceeded.incrementAndGet();
    }

    @Override
    public void onConnectionFailed(TaskInfo taskInfo, Throwable cause)
    {
        connectionFailed.incrementAndGet();
        recordFailure(taskInfo, "connection_failed", reason(cause));
    }

    @Override
    public void onRegistrationSucceeded(TaskInfo taskInfo)
    {
        registrationSucceeded.incrementAndGet();
    }

    @Override
    public void onRegistrationFailed(TaskInfo taskInfo, String reason)
    {
        registrationFailed.incrementAndGet();
        recordFailure(taskInfo, "registration_failed", reason);
    }

    @Override
    public void onAuthenticationSucceeded(TaskInfo taskInfo)
    {
        authenticationSucceeded.incrementAndGet();
    }

    @Override
    public void onAuthenticationFailed(TaskInfo taskInfo, String reason)
    {
        authenticationFailed.incrementAndGet();
        recordFailure(taskInfo, "authentication_failed", reason);
    }

    @Override
    public void onLocationReported(TaskInfo taskInfo, JTMessage message)
    {
        if (message == null || (message.getMessageId() & 0xffff) != JT808.位置信息汇报) return;
        locationReportSent.incrementAndGet();
    }

    @Override
    public void onDisconnected(TaskInfo taskInfo)
    {
        disconnected.incrementAndGet();
    }

    @Override
    public void onTerminated(TaskInfo taskInfo)
    {
        terminated.incrementAndGet();
    }

    @Override
    public void onSendFailed(TaskInfo taskInfo, JTMessage message, Throwable cause)
    {
        sendFailed.incrementAndGet();
        String messageId = message == null ? "unknown" : String.format("%04x", message.getMessageId() & 0xffff);
        recordFailure(taskInfo, "send_failed", "messageId=" + messageId + ", reason=" + reason(cause));
    }

    @Override
    public void onProtocolException(TaskInfo taskInfo, Throwable cause)
    {
        protocolExceptions.incrementAndGet();
        recordFailure(taskInfo, "protocol_exception", reason(cause));
    }

    public void applyFailureInfo(TaskInfo taskInfo)
    {
        FailureInfo failureInfo = failures.get(taskInfo.getId());
        if (failureInfo == null) return;
        taskInfo.setFailureStage(failureInfo.stage);
        taskInfo.setFailureReason(failureInfo.reason);
    }

    public TaskRuntimeSummary summary(long totalTasks, long activeTasks, long parkingTasks, long terminatedTasks)
    {
        return new TaskRuntimeSummary(
                totalTasks,
                activeTasks,
                parkingTasks,
                terminatedTasks,
                connectionSucceeded.get(),
                connectionFailed.get(),
                registrationSucceeded.get(),
                registrationFailed.get(),
                authenticationSucceeded.get(),
                authenticationFailed.get(),
                locationReportSent.get(),
                disconnected.get(),
                terminated.get(),
                sendFailed.get(),
                protocolExceptions.get(),
                runtimeResourceProbe.snapshot(),
                RunnerManager.getInstance().getSchedulerDelaySummary()
        );
    }

    private void recordFailure(TaskInfo taskInfo, String stage, String reason)
    {
        if (taskInfo == null) return;
        failures.put(taskInfo.getId(), new FailureInfo(stage, reason));
    }

    private String reason(Throwable cause)
    {
        if (cause == null) return "unknown";
        return cause.getClass().getSimpleName() + ": " + cause.getMessage();
    }

    private static class FailureInfo
    {
        private final String stage;
        private final String reason;

        private FailureInfo(String stage, String reason)
        {
            this.stage = stage;
            this.reason = reason;
        }
    }
}
