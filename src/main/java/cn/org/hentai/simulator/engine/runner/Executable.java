package cn.org.hentai.simulator.engine.runner;

import cn.org.hentai.simulator.engine.core.AbstractDriveTask;

/**
 * Created by matrixy when 2020/5/8.
 * 用于LoopRunner执行的任务片断
 */
public interface Executable
{
    /**
     * 任务执行入口
     * @param driveTask 行驶行程任务
     */
    public void execute(AbstractDriveTask driveTask);
}