package cn.org.hentai.simulator.web.controller;

import cn.org.hentai.simulator.domain.model.TaskInfo;
import cn.org.hentai.simulator.service.RouteManager;
import cn.org.hentai.simulator.service.TaskManager;
import cn.org.hentai.simulator.domain.entity.Route;
import cn.org.hentai.simulator.service.RouteService;
import cn.org.hentai.simulator.service.monitor.TaskRuntimeSummary;
import cn.org.hentai.simulator.service.monitor.TaskStopResult;
import cn.org.hentai.simulator.web.vo.Page;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping("/monitor/list")
public class MonitorController extends BaseController
{
    @Autowired
    RouteService routeService;

    @RequestMapping("/index")
    public String index()
    {
        return "monitor-list-index";
    }

    @RequestMapping("/json")
    @ResponseBody
    public Page<TaskInfo> listJson(@RequestParam(defaultValue = "1") int pageIndex,
                                   @RequestParam(defaultValue = "20") int pageSize,
                                   @RequestParam(required = false) String state,
                                   @RequestParam(required = false) String keyword,
                                   @RequestParam(required = false) String taskGroupId)
    {
        Page<TaskInfo> page = TaskManager.getInstance().find(pageIndex, pageSize, state, keyword, taskGroupId);
        for (TaskInfo task : page.getList())
        {
            Route route = routeService.getById(task.getRouteId());
            if (route != null)
            {
                task.setRouteName(route.getName());
                task.setRouteMileages(route.getMileages());
            }
        }
        return page;
    }

    @RequestMapping("/summary")
    @ResponseBody
    public TaskRuntimeSummary summary()
    {
        return TaskManager.getInstance().getRuntimeSummary();
    }

    @RequestMapping("/terminate")
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void terminate(@RequestParam Long id)
    {
        TaskManager.getInstance().terminate(id);
    }

    @RequestMapping("/terminate-all")
    @ResponseBody
    public TaskStopResult terminateAll()
    {
        return TaskManager.getInstance().terminateActiveTasks();
    }
}
