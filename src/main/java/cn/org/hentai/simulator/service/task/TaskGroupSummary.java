package cn.org.hentai.simulator.service.task;

public class TaskGroupSummary
{
    private final String taskGroupId;
    private final String displayName;
    private final String source;
    private final String sourceText;
    private final String state;
    private final int targetTasks;
    private final int startedTasks;
    private final int activeTasks;
    private final int terminatedTasks;
    private final int rampUpWindowCount;
    private final int executedWindowCount;
    private final String failureReason;

    TaskGroupSummary(String taskGroupId,
                     String displayName,
                     String source,
                     String sourceText,
                     String state,
                     int targetTasks,
                     int startedTasks,
                     int activeTasks,
                     int terminatedTasks,
                     int rampUpWindowCount,
                     int executedWindowCount,
                     String failureReason)
    {
        this.taskGroupId = taskGroupId;
        this.displayName = displayName;
        this.source = source;
        this.sourceText = sourceText;
        this.state = state;
        this.targetTasks = targetTasks;
        this.startedTasks = startedTasks;
        this.activeTasks = activeTasks;
        this.terminatedTasks = terminatedTasks;
        this.rampUpWindowCount = rampUpWindowCount;
        this.executedWindowCount = executedWindowCount;
        this.failureReason = failureReason;
    }

    public String getTaskGroupId() { return taskGroupId; }
    public String getDisplayName() { return displayName; }
    public String getSource() { return source; }
    public String getSourceText() { return sourceText; }
    public String getState() { return state; }
    public int getTargetTasks() { return targetTasks; }
    public int getStartedTasks() { return startedTasks; }
    public int getActiveTasks() { return activeTasks; }
    public int getTerminatedTasks() { return terminatedTasks; }
    public int getRampUpWindowCount() { return rampUpWindowCount; }
    public int getExecutedWindowCount() { return executedWindowCount; }
    public String getFailureReason() { return failureReason; }
}
