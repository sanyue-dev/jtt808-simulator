package cn.org.hentai.simulator.engine.core;

import cn.org.hentai.simulator.domain.model.DrivePlan;
import cn.org.hentai.simulator.domain.model.TaskInfo;
import cn.org.hentai.simulator.domain.model.TaskLifecycleObserver;
import cn.org.hentai.simulator.domain.enums.TaskState;
import cn.org.hentai.simulator.domain.enums.TaskStatus;
import org.junit.jupiter.api.Test;
import org.yzh.protocol.basics.JTMessage;
import org.yzh.protocol.commons.JT808;
import org.yzh.protocol.t808.T0001;
import org.yzh.protocol.t808.T0100;
import org.yzh.protocol.t808.T0102;
import org.yzh.protocol.t808.T0200;
import org.yzh.protocol.t808.T8100;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleDriveTaskTest
{
    @Test
    void connectedTaskSendsRegistrationMessage()
    {
        RecordingSimpleDriveTask task = new RecordingSimpleDriveTask();
        HashMap<String, String> params = new HashMap<>();
        params.put("device.sn", "A000001");
        params.put("vehicle.number", "京000001");
        task.init(params, new DrivePlan());

        task.onConnected();

        assertEquals(1, task.sentMessages.size());
        assertEquals(JT808.终端注册, task.sentMessages.get(0).getMessageId() & 0xffff);
        assertEquals("A000001", ((T0100) task.sentMessages.get(0)).getDeviceId());
    }

    @Test
    void terminatedTaskIgnoresConnectedEventWithoutSendingRegistration()
    {
        RecordingSimpleDriveTask task = new RecordingSimpleDriveTask();
        HashMap<String, String> params = new HashMap<>();
        params.put("device.sn", "A000001");
        params.put("vehicle.number", "京000001");
        task.init(params, new DrivePlan());
        task.connectionId = "closed-channel";
        task.terminate();

        task.onConnected();

        assertEquals(0, task.sentMessages.size());
    }

    @Test
    void registrationSuccessSendsAuthenticationMessage() throws Exception
    {
        RecordingSimpleDriveTask task = new RecordingSimpleDriveTask();
        HashMap<String, String> params = new HashMap<>();
        params.put("device.sim", "013800000001");
        task.init(params, new DrivePlan());

        T8100 message = new T8100();
        message.setResultCode(0);
        message.setToken("token-1");

        task.onRegisterResponsed(message);

        waitUntil(() -> task.sentMessages.size() == 1);
        assertEquals(JT808.终端鉴权, task.sentMessages.get(0).getMessageId() & 0xffff);
        assertEquals("token-1", ((T0102) task.sentMessages.get(0)).getToken());
    }


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

    @Test
    void terminatedTaskIgnoresRegisterResponseWithoutRecordingRegistrationSuccess()
    {
        AtomicInteger registrationSucceeded = new AtomicInteger(0);
        SimpleDriveTask task = new SimpleDriveTask(1L, 1L);
        task.connectionId = "closed-channel";
        task.init(new HashMap<>(), new DrivePlan(), new TaskLifecycleObserver()
        {
            @Override
            public void onRegistrationSucceeded(TaskInfo taskInfo)
            {
                registrationSucceeded.incrementAndGet();
            }
        });
        task.terminate();

        T8100 message = new T8100();
        message.setResultCode(0);

        task.onRegisterResponsed(message);

        assertEquals(0, registrationSucceeded.get());
    }

    @Test
    void registrationFailureRecordsFailureAndTerminatesTask()
    {
        AtomicInteger registrationFailed = new AtomicInteger(0);
        SimpleDriveTask task = new SimpleDriveTask(1L, 1L);
        task.connectionId = "closed-channel";
        task.init(new HashMap<>(), new DrivePlan(), new TaskLifecycleObserver()
        {
            @Override
            public void onRegistrationFailed(TaskInfo taskInfo, String reason)
            {
                registrationFailed.incrementAndGet();
            }
        });

        T8100 message = new T8100();
        message.setResultCode(1);

        task.onRegisterResponsed(message);

        assertEquals(1, registrationFailed.get());
        assertEquals(TaskState.terminated, task.getState());
    }


    @Test
    void terminatedTaskIgnoresAuthenticationResponseWithoutRecordingAuthenticationSuccess()
    {
        AtomicInteger authenticationSucceeded = new AtomicInteger(0);
        SimpleDriveTask task = new SimpleDriveTask(1L, 1L);
        task.connectionId = "closed-channel";
        task.init(new HashMap<>(), new DrivePlan(), new TaskLifecycleObserver()
        {
            @Override
            public void onAuthenticationSucceeded(TaskInfo taskInfo)
            {
                authenticationSucceeded.incrementAndGet();
            }
        });
        task.status = TaskStatus.AUTHENTICATING;
        task.terminate();

        T0001 message = new T0001();
        message.setResultCode(0);

        task.onGenericResponse(message);

        assertEquals(0, authenticationSucceeded.get());
    }

    @Test
    void authenticationFailureRecordsFailureAndTerminatesTask()
    {
        AtomicInteger authenticationFailed = new AtomicInteger(0);
        SimpleDriveTask task = new SimpleDriveTask(1L, 1L);
        task.connectionId = "closed-channel";
        task.init(new HashMap<>(), new DrivePlan(), new TaskLifecycleObserver()
        {
            @Override
            public void onAuthenticationFailed(TaskInfo taskInfo, String reason)
            {
                authenticationFailed.incrementAndGet();
            }
        });
        task.status = TaskStatus.AUTHENTICATING;

        T0001 message = new T0001();
        message.setResultCode(1);

        task.onGenericResponse(message);

        assertEquals(1, authenticationFailed.get());
        assertEquals(TaskState.terminated, task.getState());
    }

    @Test
    void authenticationSuccessStartsLocationReporting()
    {
        AtomicInteger authenticationSucceeded = new AtomicInteger(0);
        RecordingSimpleDriveTask task = new RecordingSimpleDriveTask();
        task.init(new HashMap<>(), new DrivePlan(), new TaskLifecycleObserver()
        {
            @Override
            public void onAuthenticationSucceeded(TaskInfo taskInfo)
            {
                authenticationSucceeded.incrementAndGet();
            }
        });
        task.status = TaskStatus.AUTHENTICATING;

        T0001 message = new T0001();
        message.setResultCode(0);

        task.onGenericResponse(message);

        assertEquals(1, authenticationSucceeded.get());
        assertEquals(1, task.locationReportStarted);
    }

    static class RecordingSimpleDriveTask extends SimpleDriveTask
    {
        private final List<JTMessage> sentMessages = new ArrayList<>();
        private int locationReportStarted = 0;

        RecordingSimpleDriveTask()
        {
            super(1L, 1L);
        }

        @Override
        public void send(JTMessage msg)
        {
            sentMessages.add(msg);
        }

        @Override
        public void reportLocation()
        {
            locationReportStarted++;
        }
    }

    private static void waitUntil(BooleanSupplier condition) throws InterruptedException
    {
        long deadline = System.currentTimeMillis() + 4000L;
        while (System.currentTimeMillis() < deadline)
        {
            if (condition.getAsBoolean()) return;
            Thread.sleep(20L);
        }
        assertTrue(condition.getAsBoolean());
    }
}
