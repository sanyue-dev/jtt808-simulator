package cn.org.hentai.simulator.web.controller;

import cn.org.hentai.simulator.domain.model.Point;
import cn.org.hentai.simulator.domain.model.TaskInfo;
import cn.org.hentai.simulator.service.TaskManager;
import cn.org.hentai.simulator.engine.log.Log;
import cn.org.hentai.simulator.domain.entity.Route;
import cn.org.hentai.simulator.service.RouteService;
import cn.org.hentai.simulator.web.exception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@Controller
@RequestMapping("/monitor")
public class MapMonitorController extends BaseController
{
    @Autowired
    RouteService routeService;

    @Value("${map.baidu.key}")
    String baiduMapKey;

    @RequestMapping("/view")
    public String view(@RequestParam Long id, Model model)
    {
        model.addAttribute("id", id);
        model.addAttribute("baiduMapKey", baiduMapKey);
        return "monitor";
    }

    // 基本信息
    @RequestMapping("/info")
    @ResponseBody
    public TaskInfo info(@RequestParam Long id)
    {
        TaskInfo info = TaskManager.getInstance().getById(id);
        if (info == null) throw new ValidationException("任务不存在或已结束: " + id);

        Route route = routeService.getById(info.getRouteId());
        if (route != null)
        {
            info.setRouteName(route.getName());
            info.setRouteMileages(route.getMileages());
        }
        return info;
    }

    // TODO: 轨迹

    // TODO: 当前位置
    @RequestMapping("/position")
    @ResponseBody
    public Point position(@RequestParam Long id, @RequestParam Long time)
    {
        Point point = TaskManager.getInstance().getCurrentPositionById(id);
        if (point != null && point.getReportTime() > time) return point;
        return null;
    }

    // TODO: 日志
    @RequestMapping("/logs")
    @ResponseBody
    public List<Log> logs(@RequestParam Long id, @RequestParam(defaultValue = "0") Long timeAfter)
    {
        return TaskManager.getInstance().getLogsById(id, timeAfter);
    }

    // TODO：终止行程
    @RequestMapping("/terminate")
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void terminate(@RequestParam Long id)
    {
        TaskManager.getInstance().terminate(id);
    }

    // TODO：状态设置
    @RequestMapping("/bit/set")
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setBit(@RequestParam Long id, @RequestParam String type, @RequestParam int bitIndex, @RequestParam Boolean on)
    {
        if ("warning-flags".equals(type))
        {
            TaskManager.getInstance().setWarningFlagById(id, bitIndex, on);
        }
        if ("state-flags".equals(type))
        {
            TaskManager.getInstance().setStateFlagById(id, bitIndex, on);
        }
    }
}
