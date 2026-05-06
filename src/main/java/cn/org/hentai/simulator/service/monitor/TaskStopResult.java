package cn.org.hentai.simulator.service.monitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TaskStopResult
{
    private long succeeded;
    private long failed;
    private final List<TaskStopFailure> failures = new ArrayList<>();

    public void recordSuccess()
    {
        succeeded++;
    }

    public void recordFailure(long taskId, String reason)
    {
        failed++;
        failures.add(new TaskStopFailure(taskId, reason));
    }

    public long getSucceeded()
    {
        return succeeded;
    }

    public long getFailed()
    {
        return failed;
    }

    public List<TaskStopFailure> getFailures()
    {
        return Collections.unmodifiableList(failures);
    }

    public static class TaskStopFailure
    {
        private final long taskId;
        private final String reason;

        public TaskStopFailure(long taskId, String reason)
        {
            this.taskId = taskId;
            this.reason = reason;
        }

        public long getTaskId()
        {
            return taskId;
        }

        public String getReason()
        {
            return reason;
        }
    }
}
