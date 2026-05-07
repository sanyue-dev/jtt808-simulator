package cn.org.hentai.simulator.domain.entity;

import java.io.Serializable;

public class DeviceProfile implements Serializable
{
    private Long id;
    private String name;
    private String vehicleNumber;
    private String deviceSn;
    private String simNumber;
    private String authToken;
    private String authMode;
    private Long defaultRouteId;
    private String serverAddress;
    private Integer serverPort;
    private Integer initialMileage;
    private String remark;
    private Integer enabled;

    private static final long serialVersionUID = 1L;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }
    public String getDeviceSn() { return deviceSn; }
    public void setDeviceSn(String deviceSn) { this.deviceSn = deviceSn; }
    public String getSimNumber() { return simNumber; }
    public void setSimNumber(String simNumber) { this.simNumber = simNumber; }
    public String getAuthToken() { return authToken; }
    public void setAuthToken(String authToken) { this.authToken = authToken; }
    public String getAuthMode() { return authMode; }
    public void setAuthMode(String authMode) { this.authMode = authMode; }
    public Long getDefaultRouteId() { return defaultRouteId; }
    public void setDefaultRouteId(Long defaultRouteId) { this.defaultRouteId = defaultRouteId; }
    public String getServerAddress() { return serverAddress; }
    public void setServerAddress(String serverAddress) { this.serverAddress = serverAddress; }
    public Integer getServerPort() { return serverPort; }
    public void setServerPort(Integer serverPort) { this.serverPort = serverPort; }
    public Integer getInitialMileage() { return initialMileage; }
    public void setInitialMileage(Integer initialMileage) { this.initialMileage = initialMileage; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Integer getEnabled() { return enabled; }
    public void setEnabled(Integer enabled) { this.enabled = enabled; }
}
