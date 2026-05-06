package cn.org.hentai.simulator.web.controller;

import cn.org.hentai.simulator.service.task.TaskGroupMonitorService;
import cn.org.hentai.simulator.web.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/task-groups")
public class TaskGroupMonitorController
{
    @Autowired
    TaskGroupMonitorService taskGroupMonitorService;

    @RequestMapping("/monitor")
    public String index()
    {
        return "task-group-monitor";
    }

    @RequestMapping("/snapshot")
    @ResponseBody
    public Result snapshot()
    {
        Result result = new Result();
        try
        {
            result.setData(taskGroupMonitorService.snapshot());
        }
        catch(Exception ex)
        {
            result.setError(ex);
        }
        return result;
    }
}
