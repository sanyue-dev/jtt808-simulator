package cn.org.hentai.simulator.service.task;

import cn.org.hentai.simulator.service.monitor.TaskRuntimeSummary;

import java.util.List;

public class TaskGroupMonitorSnapshot
{
    private final TaskRuntimeSummary runtimeSummary;
    private final List<TaskGroupSummary> taskGroups;

    public TaskGroupMonitorSnapshot(TaskRuntimeSummary runtimeSummary, List<TaskGroupSummary> taskGroups)
    {
        this.runtimeSummary = runtimeSummary;
        this.taskGroups = taskGroups;
    }

    public TaskRuntimeSummary getRuntimeSummary()
    {
        return runtimeSummary;
    }

    public List<TaskGroupSummary> getTaskGroups()
    {
        return taskGroups;
    }
}
