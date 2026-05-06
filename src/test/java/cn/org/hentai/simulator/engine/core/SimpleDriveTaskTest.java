package cn.org.hentai.simulator.engine.core;

import cn.org.hentai.simulator.domain.model.DrivePlan;
import cn.org.hentai.simulator.domain.model.TaskInfo;
import cn.org.hentai.simulator.domain.model.TaskLifecycleObserver;
import org.junit.jupiter.api.Test;
import org.yzh.protocol.basics.JTMessage;
import org.yzh.protocol.commons.JT808;
import org.yzh.protocol.t808.T0200;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SimpleDriveTaskTest
{
    @Test
    void terminatedTaskSkipsProtocolSendWithoutRecordingFailure()
    {
        SimpleDriveTask task = new SimpleDriveTask(1L, 1L);
        AtomicInteger sendFailed = new AtomicInteger(0);
        task.connectionId = "closed-channel";
        task.init(new HashMap<>(), new DrivePlan(), new TaskLifecycleObserver()
        {
            @Override
            public void onSendFailed(TaskInfo taskInfo, JTMessage message, Throwable cause)
            {
                sendFailed.incrementAndGet();
            }
        });
        task.terminate();

        T0200 message = new T0200();
        message.setMessageId(JT808.位置信息汇报);

        assertDoesNotThrow(() -> task.send(message));
        assertEquals(0, sendFailed.get());
    }

    @Test
    void activeTaskStillRecordsRealSendFailure()
    {
        SimpleDriveTask task = new SimpleDriveTask(1L, 1L);
        AtomicInteger sendFailed = new AtomicInteger(0);
        task.connectionId = "missing-channel";
        task.init(new HashMap<>(), new DrivePlan(), new TaskLifecycleObserver()
        {
            @Override
            public void onSendFailed(TaskInfo taskInfo, JTMessage message, Throwable cause)
            {
                sendFailed.incrementAndGet();
            }
        });

        T0200 message = new T0200();
        message.setMessageId(JT808.位置信息汇报);

        assertThrows(RuntimeException.class, () -> task.send(message));
        assertEquals(1, sendFailed.get());
    }
}
