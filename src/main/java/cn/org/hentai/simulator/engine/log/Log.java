package cn.org.hentai.simulator.engine.log;

import cn.org.hentai.simulator.domain.enums.LogType;

public class Log
{
    public LogType type;
    public long time;
    public String attachment;

    public Log(LogType type, long time, String attachment)
    {
        this.type = type;
        this.time = time;
        this.attachment = attachment;
    }
}