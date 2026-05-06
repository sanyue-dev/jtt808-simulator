package cn.org.hentai.simulator.service.task;

public class TaskLaunchWindow
{
    private final int startIndex;
    private final int endIndex;
    private final long delayMillis;

    public TaskLaunchWindow(int startIndex, int endIndex, long delayMillis)
    {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.delayMillis = delayMillis;
    }

    public int getStartIndex()
    {
        return startIndex;
    }

    public int getEndIndex()
    {
        return endIndex;
    }

    public long getDelayMillis()
    {
        return delayMillis;
    }
}
