package cn.org.hentai.simulator.web.controller;

import cn.org.hentai.simulator.domain.entity.DeviceProfile;
import cn.org.hentai.simulator.service.DeviceProfileService;
import cn.org.hentai.simulator.service.RouteService;
import cn.org.hentai.simulator.web.exception.ValidationException;
import cn.org.hentai.simulator.web.vo.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping("/device")
public class DeviceController
{
    @Autowired
    DeviceProfileService deviceProfileService;

    @Autowired
    RouteService routeService;

    @RequestMapping("/index")
    public String index(Model model)
    {
        model.addAttribute("routes", routeService.list());
        return "devices";
    }

    @RequestMapping("/list")
    @ResponseBody
    public Page<DeviceProfile> list(@RequestParam(defaultValue = "1") int pageIndex,
                                    @RequestParam(defaultValue = "20") int pageSize,
                                    @RequestParam(required = false) String keyword)
    {
        return deviceProfileService.find(keyword, pageIndex, pageSize);
    }

    @RequestMapping("/save")
    @ResponseBody
    public DeviceProfile save(@RequestParam(required = false) Long id,
                              @RequestParam String name,
                              @RequestParam String vehicleNumber,
                              @RequestParam String deviceSn,
                              @RequestParam String simNumber,
                              @RequestParam(required = false) String authToken,
                              @RequestParam String authMode,
                              @RequestParam(required = false) Long defaultRouteId,
                              @RequestParam String serverAddress,
                              @RequestParam String serverPort,
                              @RequestParam(required = false) String initialMileage,
                              @RequestParam(required = false) String remark)
    {
        DeviceProfile profile = new DeviceProfile();
        profile.setId(id);
        profile.setName(name);
        profile.setVehicleNumber(vehicleNumber);
        profile.setDeviceSn(deviceSn);
        profile.setSimNumber(simNumber);
        profile.setAuthToken(authToken);
        profile.setAuthMode(authMode);
        profile.setDefaultRouteId(defaultRouteId);
        profile.setServerAddress(serverAddress);
        profile.setServerPort(parsePositiveInt(serverPort, "服务端端口"));
        profile.setInitialMileage(initialMileage == null || initialMileage.isEmpty() ? 0 : parseNonNegativeInt(initialMileage, "初始里程"));
        profile.setRemark(remark);
        profile.setEnabled(1);
        return deviceProfileService.save(profile);
    }

    private int parsePositiveInt(String value, String fieldName)
    {
        int parsed = parseInt(value, fieldName);
        if (parsed < 1) throw new ValidationException(fieldName + "必须大于 0");
        return parsed;
    }

    private int parseNonNegativeInt(String value, String fieldName)
    {
        int parsed = parseInt(value, fieldName);
        if (parsed < 0) throw new ValidationException(fieldName + "不能小于 0");
        return parsed;
    }

    private int parseInt(String value, String fieldName)
    {
        try
        {
            return Integer.parseInt(value);
        }
        catch(NumberFormatException ex)
        {
            throw new ValidationException(fieldName + "必须是整数", ex);
        }
    }

    @RequestMapping("/remove")
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@RequestParam Long id)
    {
        deviceProfileService.removeById(id);
    }

    @RequestMapping("/status")
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void status(@RequestParam Long id, @RequestParam int enabled)
    {
        deviceProfileService.updateEnabled(id, enabled);
    }
}
