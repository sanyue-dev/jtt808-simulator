package cn.org.hentai.simulator.service.task;

public class TaskCreationResult
{
    private final String taskGroupId;
    private final String taskGroupDisplayName;
    private final String taskGroupSource;
    private final int targetTasks;

    public TaskCreationResult(String taskGroupId, String taskGroupDisplayName, String taskGroupSource, int targetTasks)
    {
        this.taskGroupId = taskGroupId;
        this.taskGroupDisplayName = taskGroupDisplayName;
        this.taskGroupSource = taskGroupSource;
        this.targetTasks = targetTasks;
    }

    public String getTaskGroupId()
    {
        return taskGroupId;
    }

    public String getTaskGroupDisplayName()
    {
        return taskGroupDisplayName;
    }

    public String getTaskGroupSource()
    {
        return taskGroupSource;
    }

    public int getTargetTasks()
    {
        return targetTasks;
    }
}
