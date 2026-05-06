package cn.org.hentai.simulator.service.task;

public class BatchTaskLaunchResult
{
    private final int scheduledTasks;
    private final int rampUpWindowCount;
    private final boolean autoStopScheduled;
    private final PreflightCheckResult preflight;
    private final String taskGroupId;
    private final String taskGroupDisplayName;

    public BatchTaskLaunchResult(int scheduledTasks, int rampUpWindowCount, boolean autoStopScheduled, PreflightCheckResult preflight)
    {
        this(scheduledTasks, rampUpWindowCount, autoStopScheduled, preflight, null, null);
    }

    public BatchTaskLaunchResult(int scheduledTasks,
                                 int rampUpWindowCount,
                                 boolean autoStopScheduled,
                                 PreflightCheckResult preflight,
                                 String taskGroupId,
                                 String taskGroupDisplayName)
    {
        this.scheduledTasks = scheduledTasks;
        this.rampUpWindowCount = rampUpWindowCount;
        this.autoStopScheduled = autoStopScheduled;
        this.preflight = preflight;
        this.taskGroupId = taskGroupId;
        this.taskGroupDisplayName = taskGroupDisplayName;
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

    public String getTaskGroupId()
    {
        return taskGroupId;
    }

    public String getTaskGroupDisplayName()
    {
        return taskGroupDisplayName;
    }
}
