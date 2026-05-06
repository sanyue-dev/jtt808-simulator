package cn.org.hentai.simulator.service.monitor;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;

public class RuntimeResourceProbe
{
    public RuntimeResourceSnapshot snapshot()
    {
        MemoryUsage heap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        ThreadMXBean threads = ManagementFactory.getThreadMXBean();
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        double processCpuLoad = os instanceof com.sun.management.OperatingSystemMXBean sunOs ? sunOs.getProcessCpuLoad() : -1D;
        long openFd = os instanceof com.sun.management.UnixOperatingSystemMXBean unixOs ? unixOs.getOpenFileDescriptorCount() : -1L;
        long maxFd = os instanceof com.sun.management.UnixOperatingSystemMXBean unixOs ? unixOs.getMaxFileDescriptorCount() : -1L;
        return new RuntimeResourceSnapshot(
                heap.getUsed(),
                heap.getCommitted(),
                heap.getMax(),
                threads.getThreadCount(),
                threads.getPeakThreadCount(),
                threads.getDaemonThreadCount(),
                os.getAvailableProcessors(),
                os.getSystemLoadAverage(),
                processCpuLoad,
                openFd,
                maxFd
        );
    }
}
