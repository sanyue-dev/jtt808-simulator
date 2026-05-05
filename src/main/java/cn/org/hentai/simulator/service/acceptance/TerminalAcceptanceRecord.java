package cn.org.hentai.simulator.service.acceptance;

import cn.org.hentai.simulator.domain.model.TerminalIdentity;

import java.util.concurrent.atomic.AtomicLong;

public class TerminalAcceptanceRecord
{
    private final TerminalIdentity identity;
    private final long taskId;
    private volatile String stage = "created";
    private volatile String failureReason;
    private final AtomicLong locationReportSent = new AtomicLong(0);

    public TerminalAcceptanceRecord(TerminalIdentity identity, long taskId)
    {
        this.identity = identity;
        this.taskId = taskId;
    }

    public TerminalIdentity getIdentity()
    {
        return identity;
    }

    public long getTaskId()
    {
        return taskId;
    }

    public String getStage()
    {
        return stage;
    }

    public void setStage(String stage)
    {
        if (hasFailure()) return;
        this.stage = stage;
    }

    public String getFailureReason()
    {
        return failureReason;
    }

    public void fail(String stage, String reason)
    {
        this.stage = stage;
        this.failureReason = reason;
    }

    public boolean hasFailure()
    {
        return failureReason != null;
    }

    public long getLocationReportSent()
    {
        return locationReportSent.get();
    }

    public void incrementLocationReportSent()
    {
        this.locationReportSent.incrementAndGet();
    }
}
