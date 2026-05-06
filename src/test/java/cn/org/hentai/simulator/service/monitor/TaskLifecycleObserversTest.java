package cn.org.hentai.simulator.service.monitor;

import cn.org.hentai.simulator.domain.model.TaskInfo;
import cn.org.hentai.simulator.domain.model.TaskLifecycleObserver;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskLifecycleObserversTest
{
    @Test
    void forwardsLifecycleEventsToBothObservers()
    {
        AtomicInteger first = new AtomicInteger();
        AtomicInteger second = new AtomicInteger();
        TaskInfo taskInfo = new TaskInfo().withId(1L);

        TaskLifecycleObserver observer = TaskLifecycleObservers.composite(
                new TaskLifecycleObserver()
                {
                    @Override
                    public void onConnected(TaskInfo taskInfo)
                    {
                        first.incrementAndGet();
                    }
                },
                new TaskLifecycleObserver()
                {
                    @Override
                    public void onConnected(TaskInfo taskInfo)
                    {
                        second.incrementAndGet();
                    }
                }
        );

        observer.onConnected(taskInfo);

        assertEquals(1, first.get());
        assertEquals(1, second.get());
    }
}
