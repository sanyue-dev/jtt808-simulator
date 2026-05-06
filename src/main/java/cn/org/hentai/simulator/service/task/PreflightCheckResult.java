package cn.org.hentai.simulator.service.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PreflightCheckResult
{
    private final List<String> failures = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();
    private long openFileDescriptorCount = -1L;
    private long maxFileDescriptorCount = -1L;
    private long requiredFileDescriptorCount = -1L;
    private long singleDestinationEphemeralPortCapacity = -1L;

    public void fail(String message)
    {
        failures.add(message);
    }

    public void warn(String message)
    {
        warnings.add(message);
    }

    public boolean hasFailures()
    {
        return failures.isEmpty() == false;
    }

    public List<String> getFailures()
    {
        return Collections.unmodifiableList(failures);
    }

    public List<String> getWarnings()
    {
        return Collections.unmodifiableList(warnings);
    }

    public long getOpenFileDescriptorCount()
    {
        return openFileDescriptorCount;
    }

    public void setOpenFileDescriptorCount(long openFileDescriptorCount)
    {
        this.openFileDescriptorCount = openFileDescriptorCount;
    }

    public long getMaxFileDescriptorCount()
    {
        return maxFileDescriptorCount;
    }

    public void setMaxFileDescriptorCount(long maxFileDescriptorCount)
    {
        this.maxFileDescriptorCount = maxFileDescriptorCount;
    }

    public long getRequiredFileDescriptorCount()
    {
        return requiredFileDescriptorCount;
    }

    public void setRequiredFileDescriptorCount(long requiredFileDescriptorCount)
    {
        this.requiredFileDescriptorCount = requiredFileDescriptorCount;
    }

    public long getSingleDestinationEphemeralPortCapacity()
    {
        return singleDestinationEphemeralPortCapacity;
    }

    public void setSingleDestinationEphemeralPortCapacity(long singleDestinationEphemeralPortCapacity)
    {
        this.singleDestinationEphemeralPortCapacity = singleDestinationEphemeralPortCapacity;
    }
}
