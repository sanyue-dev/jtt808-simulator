package cn.org.hentai.simulator.service.acceptance;

import cn.org.hentai.simulator.domain.model.TaskInfo;
import cn.org.hentai.simulator.domain.model.TaskLifecycleObserver;
import org.yzh.protocol.basics.JTMessage;
import org.yzh.protocol.commons.JT808;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class AcceptanceRun implements TaskLifecycleObserver
{
    private final String id = UUID.randomUUID().toString();
    private final AcceptanceConfig config;
    private final Instant startedAt = Instant.now();
    private volatile Instant finishedAt;
    private volatile String state = "running";
    private volatile String finishFailureReason;
    private final Map<Long, TerminalAcceptanceRecord> records = new ConcurrentHashMap<>();
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

    public AcceptanceRun(AcceptanceConfig config)
    {
        this.config = config;
    }

    public String getId()
    {
        return id;
    }

    public AcceptanceConfig getConfig()
    {
        return config;
    }

    public Instant getStartedAt()
    {
        return startedAt;
    }

    public Instant getFinishedAt()
    {
        return finishedAt;
    }

    public String getState()
    {
        return state;
    }

    public String getFinishFailureReason()
    {
        return finishFailureReason;
    }

    public void addRecord(TerminalAcceptanceRecord record)
    {
        records.put(record.getTaskId(), record);
    }

    public int getRecordCount()
    {
        return records.size();
    }

    public Collection<TerminalAcceptanceRecord> getRecords()
    {
        return records.values();
    }

    public AcceptanceSummary getSummary()
    {
        return new AcceptanceSummary(
                config.getTerminalCount(),
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
                protocolExceptions.get()
        );
    }

    public void finish()
    {
        state = "finished";
        finishedAt = Instant.now();
    }

    public void finishing()
    {
        state = "finishing";
    }

    public void finishFailed(String reason)
    {
        state = "finish_failed";
        finishFailureReason = reason;
        finishedAt = Instant.now();
    }

    public boolean allRecordedTasksTerminated()
    {
        return terminated.get() >= records.size();
    }

    @Override
    public void onConnected(TaskInfo taskInfo)
    {
        connectionSucceeded.incrementAndGet();
        record(taskInfo).setStage("connected");
    }

    @Override
    public void onConnectionFailed(TaskInfo taskInfo, Throwable cause)
    {
        connectionFailed.incrementAndGet();
        record(taskInfo).fail("connection_failed", reason(cause));
    }

    @Override
    public void onRegistrationSucceeded(TaskInfo taskInfo)
    {
        registrationSucceeded.incrementAndGet();
        record(taskInfo).setStage("registration_succeeded");
    }

    @Override
    public void onRegistrationFailed(TaskInfo taskInfo, String reason)
    {
        registrationFailed.incrementAndGet();
        record(taskInfo).fail("registration_failed", reason);
    }

    @Override
    public void onAuthenticationSucceeded(TaskInfo taskInfo)
    {
        authenticationSucceeded.incrementAndGet();
        record(taskInfo).setStage("authentication_succeeded");
    }

    @Override
    public void onAuthenticationFailed(TaskInfo taskInfo, String reason)
    {
        authenticationFailed.incrementAndGet();
        record(taskInfo).fail("authentication_failed", reason);
    }

    @Override
    public void onLocationReported(TaskInfo taskInfo, JTMessage message)
    {
        if ((message.getMessageId() & 0xffff) != JT808.位置信息汇报) return;
        locationReportSent.incrementAndGet();
        TerminalAcceptanceRecord record = record(taskInfo);
        record.setStage("location_reporting");
        record.incrementLocationReportSent();
    }

    @Override
    public void onDisconnected(TaskInfo taskInfo)
    {
        disconnected.incrementAndGet();
        record(taskInfo).setStage("disconnected");
    }

    @Override
    public void onTerminated(TaskInfo taskInfo)
    {
        terminated.incrementAndGet();
        TerminalAcceptanceRecord record = record(taskInfo);
        if (record.hasFailure() == false) record.setStage("terminated");
    }

    @Override
    public void onSendFailed(TaskInfo taskInfo, JTMessage message, Throwable cause)
    {
        sendFailed.incrementAndGet();
        String messageId = message == null ? "unknown" : String.format("%04x", message.getMessageId() & 0xffff);
        record(taskInfo).fail("send_failed", "messageId=" + messageId + ", reason=" + reason(cause));
    }

    @Override
    public void onProtocolException(TaskInfo taskInfo, Throwable cause)
    {
        protocolExceptions.incrementAndGet();
        record(taskInfo).fail("protocol_exception", reason(cause));
    }

    private TerminalAcceptanceRecord record(TaskInfo taskInfo)
    {
        TerminalAcceptanceRecord record = records.get(taskInfo.getId());
        if (record == null) throw new IllegalStateException("验收任务记录不存在: taskId=" + taskInfo.getId());
        return record;
    }

    private String reason(Throwable cause)
    {
        if (cause == null) return "unknown";
        return cause.getClass().getSimpleName() + ": " + cause.getMessage();
    }

    public static class AcceptanceSummary
    {
        private final int terminalCount;
        private final long connectionSucceeded;
        private final long connectionFailed;
        private final long registrationSucceeded;
        private final long registrationFailed;
        private final long authenticationSucceeded;
        private final long authenticationFailed;
        private final long locationReportSent;
        private final long disconnected;
        private final long terminated;
        private final long sendFailed;
        private final long protocolExceptions;

        public AcceptanceSummary(int terminalCount, long connectionSucceeded, long connectionFailed, long registrationSucceeded, long registrationFailed, long authenticationSucceeded, long authenticationFailed, long locationReportSent, long disconnected, long terminated, long sendFailed, long protocolExceptions)
        {
            this.terminalCount = terminalCount;
            this.connectionSucceeded = connectionSucceeded;
            this.connectionFailed = connectionFailed;
            this.registrationSucceeded = registrationSucceeded;
            this.registrationFailed = registrationFailed;
            this.authenticationSucceeded = authenticationSucceeded;
            this.authenticationFailed = authenticationFailed;
            this.locationReportSent = locationReportSent;
            this.disconnected = disconnected;
            this.terminated = terminated;
            this.sendFailed = sendFailed;
            this.protocolExceptions = protocolExceptions;
        }

        public int getTerminalCount() { return terminalCount; }
        public long getConnectionSucceeded() { return connectionSucceeded; }
        public long getConnectionFailed() { return connectionFailed; }
        public long getRegistrationSucceeded() { return registrationSucceeded; }
        public long getRegistrationFailed() { return registrationFailed; }
        public long getAuthenticationSucceeded() { return authenticationSucceeded; }
        public long getAuthenticationFailed() { return authenticationFailed; }
        public long getLocationReportSent() { return locationReportSent; }
        public long getDisconnected() { return disconnected; }
        public long getTerminated() { return terminated; }
        public long getSendFailed() { return sendFailed; }
        public long getProtocolExceptions() { return protocolExceptions; }
    }
}
