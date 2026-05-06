package cn.org.hentai.simulator.web.controller;

import cn.org.hentai.simulator.domain.model.TaskInfo;
import cn.org.hentai.simulator.service.RouteManager;
import cn.org.hentai.simulator.service.TaskManager;
import cn.org.hentai.simulator.domain.entity.Route;
import cn.org.hentai.simulator.service.RouteService;
import cn.org.hentai.simulator.web.vo.Page;
import cn.org.hentai.simulator.web.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
    public Result listJson(@RequestParam(defaultValue = "1") int pageIndex,
                           @RequestParam(defaultValue = "20") int pageSize,
                           @RequestParam(required = false) String state,
                           @RequestParam(required = false) String keyword)
    {
        Result result = new Result();
        try
        {
            Page<TaskInfo> page = TaskManager.getInstance().find(pageIndex, pageSize, state, keyword);
            for (TaskInfo task : page.getList())
            {
                Route route = routeService.getById(task.getRouteId());
                if (route != null)
                {
                    task.setRouteName(route.getName());
                    task.setRouteMileages(route.getMileages());
                }
            }
            result.setData(page);
        }
        catch(Exception ex)
        {
            result.setError(ex);
        }
        return result;
    }

    @RequestMapping("/summary")
    @ResponseBody
    public Result summary()
    {
        Result result = new Result();
        try
        {
            result.setData(TaskManager.getInstance().getRuntimeSummary());
        }
        catch(Exception ex)
        {
            result.setError(ex);
        }
        return result;
    }

    @RequestMapping("/terminate")
    @ResponseBody
    public Result terminate(@RequestParam Long id)
    {
        Result result = new Result();
        try
        {
            TaskManager.getInstance().terminate(id);
        }
        catch(Exception ex)
        {
            result.setError(ex);
        }
        return result;
    }

    @RequestMapping("/terminate-all")
    @ResponseBody
    public Result terminateAll()
    {
        Result result = new Result();
        try
        {
            result.setData(TaskManager.getInstance().terminateActiveTasks());
        }
        catch(Exception ex)
        {
            result.setError(ex);
        }
        return result;
    }
}
