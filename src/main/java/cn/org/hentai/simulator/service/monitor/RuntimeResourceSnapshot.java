package cn.org.hentai.simulator.service.monitor;

public class RuntimeResourceSnapshot
{
    private final long heapUsedBytes;
    private final long heapCommittedBytes;
    private final long heapMaxBytes;
    private final int threadCount;
    private final int peakThreadCount;
    private final int daemonThreadCount;
    private final int availableProcessors;
    private final double systemLoadAverage;
    private final double processCpuLoad;
    private final long openFileDescriptorCount;
    private final long maxFileDescriptorCount;

    public RuntimeResourceSnapshot(long heapUsedBytes,
                                   long heapCommittedBytes,
                                   long heapMaxBytes,
                                   int threadCount,
                                   int peakThreadCount,
                                   int daemonThreadCount,
                                   int availableProcessors,
                                   double systemLoadAverage,
                                   double processCpuLoad,
                                   long openFileDescriptorCount,
                                   long maxFileDescriptorCount)
    {
        this.heapUsedBytes = heapUsedBytes;
        this.heapCommittedBytes = heapCommittedBytes;
        this.heapMaxBytes = heapMaxBytes;
        this.threadCount = threadCount;
        this.peakThreadCount = peakThreadCount;
        this.daemonThreadCount = daemonThreadCount;
        this.availableProcessors = availableProcessors;
        this.systemLoadAverage = systemLoadAverage;
        this.processCpuLoad = processCpuLoad;
        this.openFileDescriptorCount = openFileDescriptorCount;
        this.maxFileDescriptorCount = maxFileDescriptorCount;
    }

    public long getHeapUsedBytes() { return heapUsedBytes; }
    public long getHeapCommittedBytes() { return heapCommittedBytes; }
    public long getHeapMaxBytes() { return heapMaxBytes; }
    public int getThreadCount() { return threadCount; }
    public int getPeakThreadCount() { return peakThreadCount; }
    public int getDaemonThreadCount() { return daemonThreadCount; }
    public int getAvailableProcessors() { return availableProcessors; }
    public double getSystemLoadAverage() { return systemLoadAverage; }
    public double getProcessCpuLoad() { return processCpuLoad; }
    public long getOpenFileDescriptorCount() { return openFileDescriptorCount; }
    public long getMaxFileDescriptorCount() { return maxFileDescriptorCount; }
}
