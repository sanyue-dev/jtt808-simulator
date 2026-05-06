package cn.org.hentai.simulator.engine.runner;

import java.util.concurrent.atomic.AtomicLong;

public class SchedulerDelayMetrics
{
    private final AtomicLong samples = new AtomicLong();
    private final AtomicLong totalDelayMillis = new AtomicLong();
    private final AtomicLong maxDelayMillis = new AtomicLong();

    public void record(long plannedAtMillis, long actualAtMillis)
    {
        long delay = Math.max(actualAtMillis - plannedAtMillis, 0L);
        samples.incrementAndGet();
        totalDelayMillis.addAndGet(delay);
        maxDelayMillis.accumulateAndGet(delay, Math::max);
    }

    public SchedulerDelaySummary summary()
    {
        long count = samples.get();
        long total = totalDelayMillis.get();
        return new SchedulerDelaySummary(count, count == 0 ? 0D : total * 1D / count, maxDelayMillis.get());
    }

    void reset()
    {
        samples.set(0L);
        totalDelayMillis.set(0L);
        maxDelayMillis.set(0L);
    }
}
