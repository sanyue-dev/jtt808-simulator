package cn.org.hentai.simulator.service.task;

import cn.org.hentai.simulator.domain.model.TaskInfo;
import cn.org.hentai.simulator.domain.model.TaskLifecycleObserver;
import cn.org.hentai.simulator.service.TaskManager;
import cn.org.hentai.simulator.service.monitor.TaskRuntimeSummary;
import cn.org.hentai.simulator.service.monitor.TaskStopResult;
import org.springframework.stereotype.Service;
import org.yzh.protocol.basics.JTMessage;
import org.yzh.protocol.commons.JT808;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongSupplier;

@Service
public class TaskGroupMonitorService
{
    private final RuntimeSummaryProvider runtimeSummaryProvider;
    private final TaskStopper taskStopper;
    private final TaskGroupAssigner taskGroupAssigner;
    private final LongSupplier clock;
    private final AtomicLong sequence = new AtomicLong();
    private final Map<String, TaskGroup> groups = new LinkedHashMap<>();
    private final Map<String, LaunchStopper> launchStoppers = new ConcurrentHashMap<>();

    public TaskGroupMonitorService()
    {
        this(() -> TaskManager.getInstance().getRuntimeSummary(),
                taskIds -> TaskManager.getInstance().terminateTasks(taskIds),
                (taskId, taskGroupId, taskGroupDisplayName) -> TaskManager.getInstance().assignTaskGroup(taskId, taskGroupId, taskGroupDisplayName));
    }

    public TaskGroupMonitorService(RuntimeSummaryProvider runtimeSummaryProvider)
    {
        this(runtimeSummaryProvider,
                taskIds -> TaskManager.getInstance().terminateTasks(taskIds),
                (taskId, taskGroupId, taskGroupDisplayName) -> TaskManager.getInstance().assignTaskGroup(taskId, taskGroupId, taskGroupDisplayName));
    }

    public TaskGroupMonitorService(RuntimeSummaryProvider runtimeSummaryProvider, TaskStopper taskStopper)
    {
        this(runtimeSummaryProvider, taskStopper, (taskId, taskGroupId, taskGroupDisplayName) -> TaskManager.getInstance().assignTaskGroup(taskId, taskGroupId, taskGroupDisplayName));
    }

    public TaskGroupMonitorService(RuntimeSummaryProvider runtimeSummaryProvider, TaskStopper taskStopper, TaskGroupAssigner taskGroupAssigner)
    {
        this(runtimeSummaryProvider, taskStopper, taskGroupAssigner, System::currentTimeMillis);
    }

    public TaskGroupMonitorService(RuntimeSummaryProvider runtimeSummaryProvider, TaskStopper taskStopper, TaskGroupAssigner taskGroupAssigner, LongSupplier clock)
    {
        this.runtimeSummaryProvider = runtimeSummaryProvider;
        this.taskStopper = taskStopper;
        this.taskGroupAssigner = taskGroupAssigner;
        this.clock = clock;
    }

    public synchronized TaskCreationResult createGroup(TaskGroupSource source, int targetTasks)
    {
        return createGroup(source, targetTasks, 0);
    }

    public synchronized TaskCreationResult createGroup(TaskGroupSource source, int targetTasks, int rampUpWindowCount)
    {
        long index = sequence.incrementAndGet();
        String id = "TG-" + index;
        TaskGroup group = new TaskGroup(id, displayName(source, targetTasks, index), source, targetTasks, rampUpWindowCount, clock);
        groups.put(id, group);
        return new TaskCreationResult(id, group.displayName, source.getValue(), targetTasks);
    }

    public void recordTaskStarted(String taskGroupId, long taskId)
    {
        TaskGroup group = group(taskGroupId);
        taskGroupAssigner.assignTaskGroup(taskId, group.id, group.displayName);
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

    public void recordLaunchStopped(String taskGroupId)
    {
        group(taskGroupId).recordLaunchStopped();
    }

    public void registerLaunchStopper(String taskGroupId, LaunchStopper launchStopper)
    {
        group(taskGroupId);
        launchStoppers.put(taskGroupId, launchStopper);
    }

    public TaskStopResult stopTaskGroup(String taskGroupId)
    {
        TaskGroup group = group(taskGroupId);
        List<Long> taskIds = group.beginStop();
        LaunchStopper launchStopper = launchStoppers.get(taskGroupId);
        TaskStopResult result = launchStopper == null ? taskStopper.stopTasks(taskIds) : launchStopper.stopLaunching(taskIds);
        group.recordStopResult(result);
        return result;
    }

    public TaskLifecycleObserver observer(String taskGroupId)
    {
        return new TaskLifecycleObserver()
        {
            @Override
            public void onConnected(TaskInfo taskInfo)
            {
                group(taskGroupId).recordConnected();
            }

            @Override
            public void onConnectionFailed(TaskInfo taskInfo, Throwable cause)
            {
                group(taskGroupId).recordConnectionFailed();
            }

            @Override
            public void onRegistrationSucceeded(TaskInfo taskInfo)
            {
                group(taskGroupId).recordRegistrationSucceeded();
            }

            @Override
            public void onRegistrationFailed(TaskInfo taskInfo, String reason)
            {
                group(taskGroupId).recordRegistrationFailed();
            }

            @Override
            public void onAuthenticationSucceeded(TaskInfo taskInfo)
            {
                group(taskGroupId).recordAuthenticationSucceeded();
            }

            @Override
            public void onAuthenticationFailed(TaskInfo taskInfo, String reason)
            {
                group(taskGroupId).recordAuthenticationFailed();
            }

            @Override
            public void onLocationReported(TaskInfo taskInfo, JTMessage message)
            {
                if (message == null || (message.getMessageId() & 0xffff) != JT808.位置信息汇报) return;
                group(taskGroupId).recordLocationReported();
            }

            @Override
            public void onDisconnected(TaskInfo taskInfo)
            {
                group(taskGroupId).recordDisconnected();
            }

            @Override
            public void onSendFailed(TaskInfo taskInfo, JTMessage message, Throwable cause)
            {
                group(taskGroupId).recordSendFailed();
            }

            @Override
            public void onProtocolException(TaskInfo taskInfo, Throwable cause)
            {
                group(taskGroupId).recordProtocolException();
            }

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

    public interface TaskStopper
    {
        TaskStopResult stopTasks(Collection<Long> taskIds);
    }

    public interface TaskGroupAssigner
    {
        void assignTaskGroup(long taskId, String taskGroupId, String taskGroupDisplayName);
    }

    public interface LaunchStopper
    {
        TaskStopResult stopLaunching(Collection<Long> taskIds);
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
        private final AtomicLong connectionSucceeded = new AtomicLong();
        private final AtomicLong connectionFailed = new AtomicLong();
        private final AtomicLong registrationSucceeded = new AtomicLong();
        private final AtomicLong registrationFailed = new AtomicLong();
        private final AtomicLong authenticationSucceeded = new AtomicLong();
        private final AtomicLong authenticationFailed = new AtomicLong();
        private final AtomicLong locationReportSent = new AtomicLong();
        private final AtomicLong disconnected = new AtomicLong();
        private final AtomicLong sendFailed = new AtomicLong();
        private final AtomicLong protocolExceptions = new AtomicLong();
        private final LongSupplier clock;
        private final long startedAtMillis;
        private long stopSucceeded;
        private long stopFailed;
        private List<TaskStopResult.TaskStopFailure> stopFailures = Collections.emptyList();

        private TaskGroup(String id, String displayName, TaskGroupSource source, int targetTasks, int rampUpWindowCount, LongSupplier clock)
        {
            this.id = id;
            this.displayName = displayName;
            this.source = source;
            this.targetTasks = targetTasks;
            this.rampUpWindowCount = rampUpWindowCount;
            this.clock = clock;
            this.startedAtMillis = clock.getAsLong();
        }

        private synchronized void recordTaskStarted(long taskId)
        {
            if (startedTaskIds.add(taskId) == false) return;
            if (terminatedTaskIds.contains(taskId) == false) activeTaskIds.add(taskId);
            int started = startedTasks.incrementAndGet();
            if (started >= targetTasks) state.compareAndSet("creating", "running");
            completeIfAllStartedTasksTerminated();
        }

        private synchronized void recordLaunchWindowExecuted()
        {
            executedWindowCount.incrementAndGet();
        }

        private synchronized void recordLaunchFailure(RuntimeException ex)
        {
            failureReason.compareAndSet(null, ex.getMessage());
            state.set("failed");
        }

        private synchronized void recordLaunchStopped()
        {
            if ("failed".equals(state.get())) return;
            state.set(activeTaskIds.isEmpty() ? "completed" : "stopping");
        }

        private synchronized List<Long> beginStop()
        {
            if ("failed".equals(state.get()) == false && activeTaskIds.isEmpty() == false) state.set("stopping");
            ArrayList<Long> taskIds = new ArrayList<>(activeTaskIds);
            taskIds.sort(Long::compareTo);
            return taskIds;
        }

        private synchronized void recordStopResult(TaskStopResult result)
        {
            stopSucceeded = result.getSucceeded();
            stopFailed = result.getFailed();
            stopFailures = result.getFailures();
            if (result.getFailed() > 0)
            {
                failureReason.compareAndSet(null, "任务组停止存在失败");
                state.set("failed");
                return;
            }
            completeIfAllStartedTasksTerminated();
        }

        private synchronized void recordTaskTerminated(long taskId)
        {
            activeTaskIds.remove(taskId);
            if (terminatedTaskIds.add(taskId)) terminatedTasks.incrementAndGet();
            completeIfAllStartedTasksTerminated();
        }

        private void recordConnected() { connectionSucceeded.incrementAndGet(); }
        private void recordConnectionFailed() { connectionFailed.incrementAndGet(); }
        private void recordRegistrationSucceeded() { registrationSucceeded.incrementAndGet(); }
        private void recordRegistrationFailed() { registrationFailed.incrementAndGet(); }
        private void recordAuthenticationSucceeded() { authenticationSucceeded.incrementAndGet(); }
        private void recordAuthenticationFailed() { authenticationFailed.incrementAndGet(); }
        private void recordLocationReported() { locationReportSent.incrementAndGet(); }
        private void recordDisconnected() { disconnected.incrementAndGet(); }
        private void recordSendFailed() { sendFailed.incrementAndGet(); }
        private void recordProtocolException() { protocolExceptions.incrementAndGet(); }

        private void completeIfAllStartedTasksTerminated()
        {
            if (activeTaskIds.isEmpty() && "stopping".equals(state.get())) state.set("completed");
            if (startedTasks.get() >= targetTasks && activeTaskIds.isEmpty() && "failed".equals(state.get()) == false) state.set("completed");
        }

        private synchronized TaskGroupSummary summary()
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
                    failureReason.get(),
                    stopSucceeded,
                    stopFailed,
                    stopFailures,
                    connectionSucceeded.get(),
                    connectionFailed.get(),
                    registrationSucceeded.get(),
                    registrationFailed.get(),
                    authenticationSucceeded.get(),
                    authenticationFailed.get(),
                    locationReportSent.get(),
                    locationReportRate(),
                    disconnected.get(),
                    sendFailed.get(),
                    protocolExceptions.get()
            );
        }

        private double locationReportRate()
        {
            long elapsedMillis = Math.max(clock.getAsLong() - startedAtMillis, 1L);
            return locationReportSent.get() * 1000D / elapsedMillis;
        }
    }
}
