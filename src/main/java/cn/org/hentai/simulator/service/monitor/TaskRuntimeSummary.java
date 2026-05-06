package cn.org.hentai.simulator.service.monitor;

import cn.org.hentai.simulator.engine.runner.SchedulerDelaySummary;

public class TaskRuntimeSummary
{
    private final long totalTasks;
    private final long activeTasks;
    private final long parkingTasks;
    private final long terminatedTasks;
    private final long connectionSucceeded;
    private final long connectionFailed;
    private final long registrationSucceeded;
    private final long registrationFailed;
    private final long authenticationSucceeded;
    private final long authenticationFailed;
    private final long locationReportSent;
    private final double locationReportRate;
    private final long disconnected;
    private final long terminated;
    private final long sendFailed;
    private final long protocolExceptions;
    private final RuntimeResourceSnapshot runtimeResources;
    private final SchedulerDelaySummary schedulerDelay;

    public TaskRuntimeSummary(long totalTasks,
                              long activeTasks,
                              long parkingTasks,
                              long terminatedTasks,
                              long connectionSucceeded,
                              long connectionFailed,
                              long registrationSucceeded,
                              long registrationFailed,
                              long authenticationSucceeded,
                              long authenticationFailed,
                              long locationReportSent,
                              double locationReportRate,
                              long disconnected,
                              long terminated,
                              long sendFailed,
                              long protocolExceptions,
                              RuntimeResourceSnapshot runtimeResources,
                              SchedulerDelaySummary schedulerDelay)
    {
        this.totalTasks = totalTasks;
        this.activeTasks = activeTasks;
        this.parkingTasks = parkingTasks;
        this.terminatedTasks = terminatedTasks;
        this.connectionSucceeded = connectionSucceeded;
        this.connectionFailed = connectionFailed;
        this.registrationSucceeded = registrationSucceeded;
        this.registrationFailed = registrationFailed;
        this.authenticationSucceeded = authenticationSucceeded;
        this.authenticationFailed = authenticationFailed;
        this.locationReportSent = locationReportSent;
        this.locationReportRate = locationReportRate;
        this.disconnected = disconnected;
        this.terminated = terminated;
        this.sendFailed = sendFailed;
        this.protocolExceptions = protocolExceptions;
        this.runtimeResources = runtimeResources;
        this.schedulerDelay = schedulerDelay;
    }

    public long getTotalTasks() { return totalTasks; }
    public long getActiveTasks() { return activeTasks; }
    public long getParkingTasks() { return parkingTasks; }
    public long getTerminatedTasks() { return terminatedTasks; }
    public long getConnectionSucceeded() { return connectionSucceeded; }
    public long getConnectionFailed() { return connectionFailed; }
    public long getRegistrationSucceeded() { return registrationSucceeded; }
    public long getRegistrationFailed() { return registrationFailed; }
    public long getAuthenticationSucceeded() { return authenticationSucceeded; }
    public long getAuthenticationFailed() { return authenticationFailed; }
    public long getLocationReportSent() { return locationReportSent; }
    public double getLocationReportRate() { return locationReportRate; }
    public long getDisconnected() { return disconnected; }
    public long getTerminated() { return terminated; }
    public long getSendFailed() { return sendFailed; }
    public long getProtocolExceptions() { return protocolExceptions; }
    public RuntimeResourceSnapshot getRuntimeResources() { return runtimeResources; }
    public SchedulerDelaySummary getSchedulerDelay() { return schedulerDelay; }
}
