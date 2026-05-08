package cn.org.hentai.simulator.web.controller;

import cn.org.hentai.simulator.domain.entity.Route;
import cn.org.hentai.simulator.service.RouteService;
import cn.org.hentai.simulator.service.task.SingleTaskLaunchRequest;
import cn.org.hentai.simulator.service.task.SingleTaskLaunchService;
import cn.org.hentai.simulator.service.task.TaskCreationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Created by houcheng when 2018/11/25.
 * 线路计划任务控制器
 */
@Controller
@RequestMapping("/task")
public class TaskController
{
    private String mode;

    @Autowired
    RouteService routeService;

    @Autowired
    SingleTaskLaunchService singleTaskLaunchService;

    @Value("${vehicle-server.addr}")
    String vehicleServerAddr;

    @Value("${vehicle-server.port}")
    String  vehicleServerPort;

    @RequestMapping("/index")
    public String index(Model model)
    {
        List<Route> routes = routeService.list();
        model.addAttribute("routes", routes);
        model.addAttribute("vehicleServerAddr", vehicleServerAddr);
        model.addAttribute("vehicleServerPort", vehicleServerPort);

        return "/task-create";
    }

    @RequestMapping("/run")
    @ResponseBody
    public TaskCreationResult run(@RequestParam Long routeId,
                                  @RequestParam(required = false) String vehicleNumber,
                                  @RequestParam(required = false) String deviceSn,
                                  @RequestParam(required = false) String simNumber,
                                  @RequestParam(required = false) String mileages,
                                  @RequestParam(required = false) String serverAddress,
                                  @RequestParam(required = false) String serverPort)
    {
        SingleTaskLaunchRequest request = new SingleTaskLaunchRequest();
        request.setRouteId(routeId);
        request.setVehicleNumber(vehicleNumber);
        request.setDeviceSn(deviceSn);
        request.setSimNumber(simNumber);
        request.setMileages(mileages);
        request.setServerAddress(serverAddress);
        request.setServerPort(serverPort);
        request.setMode(mode);
        return singleTaskLaunchService.launch(request);
    }

    @Value("${simulator.mode}")
    public void setMode(String mode) {
        this.mode = mode;
    }
}
