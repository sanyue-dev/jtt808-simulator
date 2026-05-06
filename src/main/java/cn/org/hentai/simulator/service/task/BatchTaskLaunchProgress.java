package cn.org.hentai.simulator.service.task;

public class BatchTaskLaunchProgress
{
    private final boolean empty;
    private final String state;
    private final int targetTasks;
    private final int rampUpWindowCount;
    private final int executedWindowCount;
    private final int startedTasks;
    private final boolean autoStopScheduled;
    private final String failureReason;
    private final long stopSucceeded;
    private final long stopFailed;

    private BatchTaskLaunchProgress(boolean empty,
                                    String state,
                                    int targetTasks,
                                    int rampUpWindowCount,
                                    int executedWindowCount,
                                    int startedTasks,
                                    boolean autoStopScheduled,
                                    String failureReason,
                                    long stopSucceeded,
                                    long stopFailed)
    {
        this.empty = empty;
        this.state = state;
        this.targetTasks = targetTasks;
        this.rampUpWindowCount = rampUpWindowCount;
        this.executedWindowCount = executedWindowCount;
        this.startedTasks = startedTasks;
        this.autoStopScheduled = autoStopScheduled;
        this.failureReason = failureReason;
        this.stopSucceeded = stopSucceeded;
        this.stopFailed = stopFailed;
    }

    public static BatchTaskLaunchProgress empty()
    {
        return new BatchTaskLaunchProgress(true, "empty", 0, 0, 0, 0, false, null, 0L, 0L);
    }

    static BatchTaskLaunchProgress of(String state,
                                      int targetTasks,
                                      int rampUpWindowCount,
                                      int executedWindowCount,
                                      int startedTasks,
                                      boolean autoStopScheduled,
                                      String failureReason,
                                      long stopSucceeded,
                                      long stopFailed)
    {
        return new BatchTaskLaunchProgress(false, state, targetTasks, rampUpWindowCount, executedWindowCount, startedTasks, autoStopScheduled, failureReason, stopSucceeded, stopFailed);
    }

    public boolean isEmpty() { return empty; }
    public String getState() { return state; }
    public int getTargetTasks() { return targetTasks; }
    public int getRampUpWindowCount() { return rampUpWindowCount; }
    public int getExecutedWindowCount() { return executedWindowCount; }
    public int getStartedTasks() { return startedTasks; }
    public boolean isAutoStopScheduled() { return autoStopScheduled; }
    public String getFailureReason() { return failureReason; }
    public long getStopSucceeded() { return stopSucceeded; }
    public long getStopFailed() { return stopFailed; }
}
