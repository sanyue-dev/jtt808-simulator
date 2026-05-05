package cn.org.hentai.simulator.engine.core;

import cn.org.hentai.simulator.domain.model.DrivePlan;
import cn.org.hentai.simulator.domain.model.TaskInfo;
import cn.org.hentai.simulator.domain.model.TaskLifecycleObserver;
import org.junit.jupiter.api.Test;
import org.yzh.protocol.basics.JTMessage;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AbstractDriveTaskTest
{
    @Test
    void emitsTerminationCallbackOnlyOnce()
    {
        TestDriveTask task = new TestDriveTask(1L, 1L);
        AtomicInteger terminated = new AtomicInteger(0);

        task.init(new HashMap<>(), new DrivePlan(), new TaskLifecycleObserver()
        {
            @Override
            public void onTerminated(TaskInfo taskInfo)
            {
                terminated.incrementAndGet();
            }
        });

        task.terminate();
        task.terminate();

        assertEquals(1, terminated.get());
    }

    static class TestDriveTask extends AbstractDriveTask
    {
        TestDriveTask(long id, long routeId)
        {
            super(id, routeId);
        }

        @Override
        public void startup()
        {
        }

        @Override
        public void send(JTMessage msg)
        {
        }
    }
}
