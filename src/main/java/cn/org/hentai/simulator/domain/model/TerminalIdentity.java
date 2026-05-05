package cn.org.hentai.simulator.domain.model;

public class TerminalIdentity
{
    private final String vehicleNumber;
    private final String deviceSn;
    private final String simNumber;

    public TerminalIdentity(String vehicleNumber, String deviceSn, String simNumber)
    {
        this.vehicleNumber = vehicleNumber;
        this.deviceSn = deviceSn;
        this.simNumber = simNumber;
    }

    public String getVehicleNumber()
    {
        return vehicleNumber;
    }

    public String getDeviceSn()
    {
        return deviceSn;
    }

    public String getSimNumber()
    {
        return simNumber;
    }
}
