package cn.org.hentai.simulator.service.task;

public class BatchTaskLaunchResult
{
    private final int scheduledTasks;
    private final int rampUpWindowCount;
    private final boolean autoStopScheduled;

    public BatchTaskLaunchResult(int scheduledTasks, int rampUpWindowCount, boolean autoStopScheduled)
    {
        this.scheduledTasks = scheduledTasks;
        this.rampUpWindowCount = rampUpWindowCount;
        this.autoStopScheduled = autoStopScheduled;
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
}
