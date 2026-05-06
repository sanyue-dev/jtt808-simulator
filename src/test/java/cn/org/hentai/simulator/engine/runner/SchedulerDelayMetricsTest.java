package cn.org.hentai.simulator.engine.runner;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SchedulerDelayMetricsTest
{
    @Test
    void aggregatesPlannedVsActualDelay()
    {
        SchedulerDelayMetrics metrics = new SchedulerDelayMetrics();

        metrics.record(1000L, 1010L);
        metrics.record(2000L, 2025L);
        metrics.record(3000L, 2990L);

        SchedulerDelaySummary summary = metrics.summary();

        assertEquals(3L, summary.getSamples());
        assertEquals(35D / 3D, summary.getAverageDelayMillis());
        assertEquals(25L, summary.getMaxDelayMillis());
    }
}
