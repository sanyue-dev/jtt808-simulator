package cn.org.hentai.simulator.service.task;

public class BatchTaskLaunchResult
{
    private final int scheduledTasks;
    private final int rampUpWindowCount;
    private final boolean autoStopScheduled;
    private final PreflightCheckResult preflight;

    public BatchTaskLaunchResult(int scheduledTasks, int rampUpWindowCount, boolean autoStopScheduled, PreflightCheckResult preflight)
    {
        this.scheduledTasks = scheduledTasks;
        this.rampUpWindowCount = rampUpWindowCount;
        this.autoStopScheduled = autoStopScheduled;
        this.preflight = preflight;
    }

    public int getScheduledTasks()
    {
        return scheduledTasks;
    }

    public int getRampUpWindowCount()
    {
        return rampUpWindowCount;
    }

    public boolean isAutoStopScheduled()
    {
        return autoStopScheduled;
    }

    public PreflightCheckResult getPreflight()
    {
        return preflight;
    }
}
