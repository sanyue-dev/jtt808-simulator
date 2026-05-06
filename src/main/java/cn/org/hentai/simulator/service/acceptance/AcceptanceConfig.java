package cn.org.hentai.simulator.service.acceptance;

import java.util.ArrayList;
import java.util.List;

public class AcceptanceConfig
{
    private int terminalCount = 1000;
    private int reportIntervalSeconds = 5;
    private int runDurationSeconds = 300;
    private int rampUpBatchSize = 100;
    private int rampUpIntervalMillis = 1000;
    private String serverAddress;
    private int serverPort;
    private String vehicleNumberPattern = "京%06d";
    private String deviceSnPattern = "A%06d";
    private String simNumberPattern = "013800%06d";
    private List<Long> routeIds = new ArrayList<>();

    public int getTerminalCount()
    {
        return terminalCount;
    }

    public void setTerminalCount(int terminalCount)
    {
        this.terminalCount = terminalCount;
    }

    public int getReportIntervalSeconds()
    {
        return reportIntervalSeconds;
    }

    public void setReportIntervalSeconds(int reportIntervalSeconds)
    {
        this.reportIntervalSeconds = reportIntervalSeconds;
    }

    public int getRunDurationSeconds()
    {
        return runDurationSeconds;
    }

    public void setRunDurationSeconds(int runDurationSeconds)
    {
        this.runDurationSeconds = runDurationSeconds;
    }

    public int getRampUpBatchSize()
    {
        return rampUpBatchSize;
    }

    public void setRampUpBatchSize(int rampUpBatchSize)
    {
        this.rampUpBatchSize = rampUpBatchSize;
    }

    public int getRampUpIntervalMillis()
    {
        return rampUpIntervalMillis;
    }

    public void setRampUpIntervalMillis(int rampUpIntervalMillis)
    {
        this.rampUpIntervalMillis = rampUpIntervalMillis;
    }

    public String getServerAddress()
    {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress)
    {
        this.serverAddress = serverAddress;
    }

    public int getServerPort()
    {
        return serverPort;
    }

    public void setServerPort(int serverPort)
    {
        this.serverPort = serverPort;
    }

    public String getVehicleNumberPattern()
    {
        return vehicleNumberPattern;
    }

    public void setVehicleNumberPattern(String vehicleNumberPattern)
    {
        this.vehicleNumberPattern = vehicleNumberPattern;
    }

    public String getDeviceSnPattern()
    {
        return deviceSnPattern;
    }

    public void setDeviceSnPattern(String deviceSnPattern)
    {
        this.deviceSnPattern = deviceSnPattern;
    }

    public String getSimNumberPattern()
    {
        return simNumberPattern;
    }

    public void setSimNumberPattern(String simNumberPattern)
    {
        this.simNumberPattern = simNumberPattern;
    }

    public List<Long> getRouteIds()
    {
        return routeIds;
    }

    public void setRouteIds(List<Long> routeIds)
    {
        this.routeIds = routeIds == null ? new ArrayList<>() : new ArrayList<>(routeIds);
    }
}
