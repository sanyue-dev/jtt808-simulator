package cn.org.hentai.simulator.web.controller;

import cn.org.hentai.simulator.service.monitor.TaskStopResult;
import cn.org.hentai.simulator.service.task.TaskGroupMonitorService;
import cn.org.hentai.simulator.service.task.TaskGroupMonitorSnapshot;
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
    public TaskGroupMonitorSnapshot snapshot()
    {
        return taskGroupMonitorService.snapshot();
    }

    @RequestMapping("/stop")
    @ResponseBody
    public TaskStopResult stop(String taskGroupId)
    {
        return taskGroupMonitorService.stopTaskGroup(taskGroupId);
    }
}
