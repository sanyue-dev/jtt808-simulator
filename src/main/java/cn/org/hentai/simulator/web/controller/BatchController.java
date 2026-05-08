package cn.org.hentai.simulator.web.controller;

import cn.org.hentai.simulator.domain.entity.Route;
import cn.org.hentai.simulator.service.RouteService;
import cn.org.hentai.simulator.service.task.BatchTaskLaunchProgress;
import cn.org.hentai.simulator.service.task.BatchTaskLaunchRequest;
import cn.org.hentai.simulator.service.task.BatchTaskLaunchResult;
import cn.org.hentai.simulator.service.task.TaskBatchLaunchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/batch")
public class BatchController extends BaseController
{
    @Autowired
    RouteService routeService;

    @Autowired
    TaskBatchLaunchService taskBatchLaunchService;

    @Value("${vehicle-server.addr}")
    String vehicleServerAddr;

    @Value("${vehicle-server.port}")
    String  vehicleServerPort;

    private String mode;

    // 批量创建任务入口页面
    @RequestMapping("/index")
    public String index(Model model)
    {
        List<Route> routes = routeService.list();
        model.addAttribute("routes", routes);
        model.addAttribute("vehicleServerAddr", vehicleServerAddr);
        model.addAttribute("vehicleServerPort", vehicleServerPort);

        return "task-batch-create";
    }

    // 批量创建
    @RequestMapping("/run")
    @ResponseBody
    public BatchTaskLaunchResult run(@RequestParam int vehicleCount,
                                     @RequestParam(name = "routeIdList[]", required = false) Long[] routeIdList,
                                     @RequestParam String vehicleNumberPattern,
                                     @RequestParam String deviceSnPattern,
                                     @RequestParam String simNumberPattern,
                                     @RequestParam String serverAddress,
                                     @RequestParam String serverPort,
                                     @RequestParam(defaultValue = "5") int reportIntervalSeconds,
                                     @RequestParam(defaultValue = "0") int runDurationSeconds,
                                     @RequestParam(defaultValue = "0") int rampUpBatchSize,
                                     @RequestParam(defaultValue = "1") int rampUpIntervalMillis)
    {
        List<Long> routeIds = new ArrayList<>();
        if (routeIdList != null)
        {
            for (Long id : routeIdList)
            {
                if (id == null || id == 0L)
                {
                    routeIds.clear();
                    break;
                }
                routeIds.add(id);
            }
        }

        BatchTaskLaunchRequest request = new BatchTaskLaunchRequest();
        request.setTerminalCount(vehicleCount);
        request.setRouteIds(routeIds);
        request.setVehicleNumberPattern(vehicleNumberPattern);
        request.setDeviceSnPattern(deviceSnPattern);
        request.setSimNumberPattern(simNumberPattern);
        request.setServerAddress(serverAddress);
        request.setServerPort(parseServerPort(serverPort));
        request.setMode(mode);
        request.setReportIntervalSeconds(reportIntervalSeconds);
        request.setRunDurationSeconds(runDurationSeconds);
        request.setRampUpBatchSize(rampUpBatchSize);
        request.setRampUpIntervalMillis(rampUpIntervalMillis);

        return taskBatchLaunchService.launch(request);
    }

    @RequestMapping("/progress")
    @ResponseBody
    public BatchTaskLaunchProgress progress()
    {
        return taskBatchLaunchService.currentProgress();
    }

    private int parseServerPort(String serverPort)
    {
        try
        {
            return Integer.parseInt(serverPort);
        }
        catch(Exception ex)
        {
            throw new IllegalArgumentException("目标服务端端口非法: " + serverPort, ex);
        }
    }

    @Value("${simulator.mode}")
    public void setMode(String mode) {
        this.mode = mode;
    }
}
