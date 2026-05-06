package cn.org.hentai.simulator.service.task;

import cn.org.hentai.simulator.domain.model.TaskInfo;
import cn.org.hentai.simulator.domain.model.TaskLifecycleObserver;
import cn.org.hentai.simulator.service.TaskManager;
import cn.org.hentai.simulator.service.monitor.TaskRuntimeSummary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class TaskGroupMonitorService
{
    private final RuntimeSummaryProvider runtimeSummaryProvider;
    private final AtomicLong sequence = new AtomicLong();
    private final Map<String, TaskGroup> groups = new LinkedHashMap<>();

    public TaskGroupMonitorService()
    {
        this(() -> TaskManager.getInstance().getRuntimeSummary());
    }

    public TaskGroupMonitorService(RuntimeSummaryProvider runtimeSummaryProvider)
    {
        this.runtimeSummaryProvider = runtimeSummaryProvider;
    }

    public synchronized TaskCreationResult createGroup(TaskGroupSource source, int targetTasks)
    {
        return createGroup(source, targetTasks, 0);
    }

    public synchronized TaskCreationResult createGroup(TaskGroupSource source, int targetTasks, int rampUpWindowCount)
    {
        long index = sequence.incrementAndGet();
        String id = "TG-" + index;
        TaskGroup group = new TaskGroup(id, displayName(source, targetTasks, index), source, targetTasks, rampUpWindowCount);
        groups.put(id, group);
        return new TaskCreationResult(id, group.displayName, source.getValue(), targetTasks);
    }

    public void recordTaskStarted(String taskGroupId, long taskId)
    {
        TaskGroup group = group(taskGroupId);
        group.recordTaskStarted(taskId);
    }

    public void recordLaunchWindowExecuted(String taskGroupId)
    {
        group(taskGroupId).recordLaunchWindowExecuted();
    }

    public void recordLaunchFailure(String taskGroupId, RuntimeException ex)
    {
        group(taskGroupId).recordLaunchFailure(ex);
    }

    public TaskLifecycleObserver observer(String taskGroupId)
    {
        return new TaskLifecycleObserver()
        {
            @Override
            public void onTerminated(TaskInfo taskInfo)
            {
                if (taskInfo == null) return;
                group(taskGroupId).recordTaskTerminated(taskInfo.getId());
            }
        };
    }

    public synchronized TaskGroupMonitorSnapshot snapshot()
    {
        ArrayList<TaskGroupSummary> summaries = new ArrayList<>();
        for (TaskGroup group : groups.values()) summaries.add(group.summary());
        return new TaskGroupMonitorSnapshot(runtimeSummaryProvider.getRuntimeSummary(), summaries);
    }

    private TaskGroup group(String taskGroupId)
    {
        synchronized(this)
        {
            TaskGroup group = groups.get(taskGroupId);
            if (group == null) throw new IllegalArgumentException("任务组不存在: " + taskGroupId);
            return group;
        }
    }

    private String displayName(TaskGroupSource source, int targetTasks, long index)
    {
        if (source == TaskGroupSource.SINGLE) return "单车创建 #" + index;
        return "批量创建 " + targetTasks + " 台 #" + index;
    }

    public interface RuntimeSummaryProvider
    {
        TaskRuntimeSummary getRuntimeSummary();
    }

    private static class TaskGroup
    {
        private final String id;
        private final String displayName;
        private final TaskGroupSource source;
        private final int targetTasks;
        private final int rampUpWindowCount;
        private final AtomicInteger executedWindowCount = new AtomicInteger();
        private final AtomicInteger startedTasks = new AtomicInteger();
        private final AtomicInteger terminatedTasks = new AtomicInteger();
        private final Set<Long> startedTaskIds = ConcurrentHashMap.newKeySet();
        private final Set<Long> activeTaskIds = ConcurrentHashMap.newKeySet();
        private final Set<Long> terminatedTaskIds = ConcurrentHashMap.newKeySet();
        private final AtomicReference<String> state = new AtomicReference<>("creating");
        private final AtomicReference<String> failureReason = new AtomicReference<>();

        private TaskGroup(String id, String displayName, TaskGroupSource source, int targetTasks, int rampUpWindowCount)
        {
            this.id = id;
            this.displayName = displayName;
            this.source = source;
            this.targetTasks = targetTasks;
            this.rampUpWindowCount = rampUpWindowCount;
        }

        private void recordTaskStarted(long taskId)
        {
            if (startedTaskIds.add(taskId) == false) return;
            if (terminatedTaskIds.contains(taskId) == false) activeTaskIds.add(taskId);
            int started = startedTasks.incrementAndGet();
            if (started >= targetTasks) state.compareAndSet("creating", "running");
            completeIfAllStartedTasksTerminated();
        }

        private void recordLaunchWindowExecuted()
        {
            executedWindowCount.incrementAndGet();
        }

        private void recordLaunchFailure(RuntimeException ex)
        {
            failureReason.compareAndSet(null, ex.getMessage());
            state.set("failed");
        }

        private void recordTaskTerminated(long taskId)
        {
            activeTaskIds.remove(taskId);
            if (terminatedTaskIds.add(taskId)) terminatedTasks.incrementAndGet();
            completeIfAllStartedTasksTerminated();
        }

        private void completeIfAllStartedTasksTerminated()
        {
            if (startedTasks.get() >= targetTasks && activeTaskIds.isEmpty() && "failed".equals(state.get()) == false) state.set("completed");
        }

        private TaskGroupSummary summary()
        {
            return new TaskGroupSummary(
                    id,
                    displayName,
                    source.getValue(),
                    source.getDisplayText(),
                    state.get(),
                    targetTasks,
                    startedTasks.get(),
                    activeTaskIds.size(),
                    terminatedTasks.get(),
                    rampUpWindowCount,
                    executedWindowCount.get(),
                    failureReason.get()
            );
        }
    }
}
