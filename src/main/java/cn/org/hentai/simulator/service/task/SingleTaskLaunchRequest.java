package cn.org.hentai.simulator.service.task;

public class SingleTaskLaunchRequest
{
    private Long routeId;
    private String vehicleNumber;
    private String deviceSn;
    private String simNumber;
    private String mileages;
    private String serverAddress;
    private String serverPort;
    private String mode;

    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }
    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }
    public String getDeviceSn() { return deviceSn; }
    public void setDeviceSn(String deviceSn) { this.deviceSn = deviceSn; }
    public String getSimNumber() { return simNumber; }
    public void setSimNumber(String simNumber) { this.simNumber = simNumber; }
    public String getMileages() { return mileages; }
    public void setMileages(String mileages) { this.mileages = mileages; }
    public String getServerAddress() { return serverAddress; }
    public void setServerAddress(String serverAddress) { this.serverAddress = serverAddress; }
    public String getServerPort() { return serverPort; }
    public void setServerPort(String serverPort) { this.serverPort = serverPort; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
}
