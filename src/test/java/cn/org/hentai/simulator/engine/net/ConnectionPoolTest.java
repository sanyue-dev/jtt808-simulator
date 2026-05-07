package cn.org.hentai.simulator.engine.net;

import cn.org.hentai.simulator.domain.enums.TaskState;
import cn.org.hentai.simulator.domain.model.DrivePlan;
import cn.org.hentai.simulator.domain.model.TaskInfo;
import cn.org.hentai.simulator.domain.model.TaskLifecycleObserver;
import cn.org.hentai.simulator.engine.core.SimpleDriveTask;
import cn.org.hentai.simulator.engine.event.EventDispatcher;
import cn.org.hentai.simulator.engine.event.EventEnum;
import cn.org.hentai.simulator.engine.event.Listen;
import cn.org.hentai.simulator.service.monitor.TaskRuntimeSummary;
import cn.org.hentai.simulator.service.monitor.TaskStopResult;
import cn.org.hentai.simulator.service.task.TaskCreationResult;
import cn.org.hentai.simulator.service.task.TaskGroupMonitorService;
import cn.org.hentai.simulator.service.task.TaskGroupSource;
import cn.org.hentai.simulator.service.task.TaskGroupSummary;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(OutputCaptureExtension.class)
class ConnectionPoolTest
{
    @Test
    void intentionalCloseDoesNotReportMissingChannelWhenInactiveEventArrives(CapturedOutput output)
    {
        ConnectionPool pool = ConnectionPool.getInstance();
        EmbeddedChannel channel = new EmbeddedChannel();
        String channelId = channel.id().asLongText();

        pool.connections.put(channelId, new ConnectionPool.Connection(channel, null));

        try
        {
            pool.close(channelId);
            pool.notify("disconnected", channelId, null, null);

            assertFalse(output.getOut().contains("no channel found for: " + channelId));
            assertFalse(output.getErr().contains("no channel found for: " + channelId));
        }
        finally
        {
            pool.connections.remove(channelId);
            channel.close();
        }
    }

    @Test
    void intentionalCloseDoesNotReportMissingChannelWhenLateInboundMessageArrives(CapturedOutput output)
    {
        ConnectionPool pool = ConnectionPool.getInstance();
        EmbeddedChannel channel = new EmbeddedChannel();
        String channelId = channel.id().asLongText();

        pool.connections.put(channelId, new ConnectionPool.Connection(channel, null));

        try
        {
            pool.close(channelId);
            pool.notify("message_received", channelId, "8300", null);
            pool.notify("disconnected", channelId, null, null);

            assertFalse(output.getOut().contains("no channel found for: " + channelId));
            assertFalse(output.getErr().contains("no channel found for: " + channelId));
        }
        finally
        {
            pool.connections.remove(channelId);
            pool.intentionallyClosedChannels.remove(channelId);
            channel.close();
        }
    }

    @Test
    void unknownChannelStillReportsMissingChannel(CapturedOutput output)
    {
        String channelId = "unknown-channel-id";

        ConnectionPool.getInstance().notify("disconnected", channelId, null, null);

        assertTrue(output.getOut().contains("no channel found for: " + channelId)
                || output.getErr().contains("no channel found for: " + channelId));
    }

    @Test
    void closeDoesNotRememberAlreadyInactiveChannel()
    {
        ConnectionPool pool = ConnectionPool.getInstance();
        EmbeddedChannel channel = new EmbeddedChannel();
        String channelId = channel.id().asLongText();

        pool.connections.put(channelId, new ConnectionPool.Connection(channel, null));

        try
        {
            channel.close();
            pool.close(channelId);

            assertFalse(pool.intentionallyClosedChannels.contains(channelId));
        }
        finally
        {
            pool.connections.remove(channelId);
            pool.intentionallyClosedChannels.remove(channelId);
            channel.close();
        }
    }

    @Test
    void protocolExceptionClosesTaskLifecycleImmediately()
    {
        ConnectionPool pool = ConnectionPool.getInstance();
        EmbeddedChannel channel = new EmbeddedChannel(new ConnectionPool.SimpleNettyHandler());
        String channelId = channel.id().asLongText();
        CountingLifecycleObserver observer = new CountingLifecycleObserver();
        SimpleDriveTask task = new SimpleDriveTask(1L, 1L);
        task.init(new HashMap<>(), new DrivePlan(), observer);
        task.onConnectSuccess(channel);
        EventDispatcher.register(task);
        pool.connections.put(channelId, new ConnectionPool.Connection(channel, task));

        try
        {
            channel.pipeline().fireExceptionCaught(new IOException("Operation timed out"));

            assertEquals(TaskState.terminated, task.getState());
            assertEquals(1, observer.protocolExceptions.get());
            assertEquals(1, observer.disconnected.get());
            assertEquals(1, observer.terminated.get());
        }
        finally
        {
            pool.connections.remove(channelId);
            pool.intentionallyClosedChannels.remove(channelId);
            channel.close();
        }
    }

    @Test
    void protocolExceptionUpdatesTaskGroupSummaryImmediately()
    {
        ConnectionPool pool = ConnectionPool.getInstance();
        EmbeddedChannel channel = new EmbeddedChannel(new ConnectionPool.SimpleNettyHandler());
        String channelId = channel.id().asLongText();
        TaskGroupMonitorService monitorService = new TaskGroupMonitorService(new EmptyRuntimeSummaryProvider(),
                taskIds -> new TaskStopResult(),
                (taskId, taskGroupId, taskGroupDisplayName) -> {});
        TaskCreationResult creation = monitorService.createGroup(TaskGroupSource.BATCH, 1);
        SimpleDriveTask task = new SimpleDriveTask(1L, 1L);
        task.init(new HashMap<>(), new DrivePlan(), monitorService.observer(creation.getTaskGroupId()));
        task.onConnectSuccess(channel);
        EventDispatcher.register(task);
        pool.connections.put(channelId, new ConnectionPool.Connection(channel, task));
        monitorService.recordTaskStarted(creation.getTaskGroupId(), task.getId());

        try
        {
            channel.pipeline().fireExceptionCaught(new IOException("Operation timed out"));

            TaskGroupSummary group = monitorService.snapshot().getTaskGroups().get(0);
            assertEquals("completed", group.getState());
            assertEquals(0, group.getActiveTasks());
            assertEquals(1, group.getTerminatedTasks());
            assertEquals(1L, group.getDisconnected());
            assertEquals(1L, group.getProtocolExceptions());
        }
        finally
        {
            pool.connections.remove(channelId);
            pool.intentionallyClosedChannels.remove(channelId);
            channel.close();
        }
    }

    @Test
    void protocolExceptionUsesDisconnectedEventDispatch()
    {
        ConnectionPool pool = ConnectionPool.getInstance();
        EmbeddedChannel channel = new EmbeddedChannel(new ConnectionPool.SimpleNettyHandler());
        String channelId = channel.id().asLongText();
        CustomDisconnectedTask task = new CustomDisconnectedTask();
        task.init(new HashMap<>(), new DrivePlan());
        task.onConnectSuccess(channel);
        EventDispatcher.register(task);
        pool.connections.put(channelId, new ConnectionPool.Connection(channel, task));

        try
        {
            channel.pipeline().fireExceptionCaught(new IOException("Operation timed out"));

            assertEquals(TaskState.terminated, task.getState());
            assertEquals(1, task.customDisconnectedCount.get());
        }
        finally
        {
            pool.connections.remove(channelId);
            pool.intentionallyClosedChannels.remove(channelId);
            channel.close();
        }
    }

    private static class CountingLifecycleObserver implements TaskLifecycleObserver
    {
        private final AtomicInteger protocolExceptions = new AtomicInteger();
        private final AtomicInteger disconnected = new AtomicInteger();
        private final AtomicInteger terminated = new AtomicInteger();

        @Override
        public void onProtocolException(TaskInfo taskInfo, Throwable cause)
        {
            protocolExceptions.incrementAndGet();
        }

        @Override
        public void onDisconnected(TaskInfo taskInfo)
        {
            disconnected.incrementAndGet();
        }

        @Override
        public void onTerminated(TaskInfo taskInfo)
        {
            terminated.incrementAndGet();
        }
    }

    private static class EmptyRuntimeSummaryProvider implements TaskGroupMonitorService.RuntimeSummaryProvider
    {
        @Override
        public TaskRuntimeSummary getRuntimeSummary()
        {
            return new TaskRuntimeSummary(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, null, null);
        }
    }

    public static class CustomDisconnectedTask extends SimpleDriveTask
    {
        private final AtomicInteger customDisconnectedCount = new AtomicInteger();

        private CustomDisconnectedTask()
        {
            super(1L, 1L);
        }

        @Override
        @Listen(when = EventEnum.disconnected)
        public void onDisconnected()
        {
            customDisconnectedCount.incrementAndGet();
            super.onDisconnected();
        }
    }
}
