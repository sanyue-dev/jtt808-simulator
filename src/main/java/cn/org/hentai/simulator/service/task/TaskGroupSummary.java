package cn.org.hentai.simulator.service.task;

import cn.org.hentai.simulator.service.monitor.TaskStopResult;

import java.util.List;

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
    private final long stopSucceeded;
    private final long stopFailed;
    private final List<TaskStopResult.TaskStopFailure> stopFailures;
    private final long connectionSucceeded;
    private final long connectionFailed;
    private final long registrationSucceeded;
    private final long registrationFailed;
    private final long authenticationSucceeded;
    private final long authenticationFailed;
    private final long locationReportSent;
    private final double locationReportRate;
    private final long disconnected;
    private final long sendFailed;
    private final long protocolExceptions;

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
                     String failureReason,
                     long stopSucceeded,
                     long stopFailed,
                     List<TaskStopResult.TaskStopFailure> stopFailures,
                     long connectionSucceeded,
                     long connectionFailed,
                     long registrationSucceeded,
                     long registrationFailed,
                     long authenticationSucceeded,
                     long authenticationFailed,
                     long locationReportSent,
                     double locationReportRate,
                     long disconnected,
                     long sendFailed,
                     long protocolExceptions)
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
        this.stopSucceeded = stopSucceeded;
        this.stopFailed = stopFailed;
        this.stopFailures = stopFailures;
        this.connectionSucceeded = connectionSucceeded;
        this.connectionFailed = connectionFailed;
        this.registrationSucceeded = registrationSucceeded;
        this.registrationFailed = registrationFailed;
        this.authenticationSucceeded = authenticationSucceeded;
        this.authenticationFailed = authenticationFailed;
        this.locationReportSent = locationReportSent;
        this.locationReportRate = locationReportRate;
        this.disconnected = disconnected;
        this.sendFailed = sendFailed;
        this.protocolExceptions = protocolExceptions;
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
    public long getStopSucceeded() { return stopSucceeded; }
    public long getStopFailed() { return stopFailed; }
    public List<TaskStopResult.TaskStopFailure> getStopFailures() { return stopFailures; }
    public long getConnectionSucceeded() { return connectionSucceeded; }
    public long getConnectionFailed() { return connectionFailed; }
    public long getRegistrationSucceeded() { return registrationSucceeded; }
    public long getRegistrationFailed() { return registrationFailed; }
    public long getAuthenticationSucceeded() { return authenticationSucceeded; }
    public long getAuthenticationFailed() { return authenticationFailed; }
    public long getLocationReportSent() { return locationReportSent; }
    public double getLocationReportRate() { return locationReportRate; }
    public long getDisconnected() { return disconnected; }
    public long getSendFailed() { return sendFailed; }
    public long getProtocolExceptions() { return protocolExceptions; }
}
