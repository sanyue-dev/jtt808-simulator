package cn.org.hentai.simulator.engine.runner;

public class SchedulerDelaySummary
{
    private final long samples;
    private final double averageDelayMillis;
    private final long maxDelayMillis;

    public SchedulerDelaySummary(long samples, double averageDelayMillis, long maxDelayMillis)
    {
        this.samples = samples;
        this.averageDelayMillis = averageDelayMillis;
        this.maxDelayMillis = maxDelayMillis;
    }

    public long getSamples()
    {
        return samples;
    }

    public double getAverageDelayMillis()
    {
        return averageDelayMillis;
    }

    public long getMaxDelayMillis()
    {
        return maxDelayMillis;
    }
}
