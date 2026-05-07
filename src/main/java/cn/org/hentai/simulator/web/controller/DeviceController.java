package cn.org.hentai.simulator.web.controller;

import cn.org.hentai.simulator.domain.entity.DeviceProfile;
import cn.org.hentai.simulator.service.DeviceProfileService;
import cn.org.hentai.simulator.service.RouteService;
import cn.org.hentai.simulator.web.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
    public Result list(@RequestParam(defaultValue = "1") int pageIndex,
                       @RequestParam(defaultValue = "20") int pageSize,
                       @RequestParam(required = false) String keyword)
    {
        Result result = new Result();
        try
        {
            result.setData(deviceProfileService.find(keyword, pageIndex, pageSize));
        }
        catch(Exception ex)
        {
            result.setError(ex);
        }
        return result;
    }

    @RequestMapping("/save")
    @ResponseBody
    public Result save(@RequestParam(required = false) Long id,
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
        Result result = new Result();
        try
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
            profile.setServerPort(Integer.parseInt(serverPort));
            profile.setInitialMileage(initialMileage == null || initialMileage.isEmpty() ? 0 : Integer.parseInt(initialMileage));
            profile.setRemark(remark);
            profile.setEnabled(1);
            result.setData(deviceProfileService.save(profile));
        }
        catch(Exception ex)
        {
            result.setError(ex);
        }
        return result;
    }

    @RequestMapping("/remove")
    @ResponseBody
    public Result remove(@RequestParam Long id)
    {
        Result result = new Result();
        try
        {
            deviceProfileService.removeById(id);
        }
        catch(Exception ex)
        {
            result.setError(ex);
        }
        return result;
    }

    @RequestMapping("/status")
    @ResponseBody
    public Result status(@RequestParam Long id, @RequestParam int enabled)
    {
        Result result = new Result();
        try
        {
            deviceProfileService.updateEnabled(id, enabled);
        }
        catch(Exception ex)
        {
            result.setError(ex);
        }
        return result;
    }
}
