package cn.org.hentai.simulator.engine.runner;

import cn.org.hentai.simulator.engine.core.AbstractDriveTask;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by matrixy when 2020/5/8.
 * LoopRunner线程管理器，用于分发/管理任务
 */
public final class RunnerManager
{
    static final RunnerManager instance = new RunnerManager();
    private final ScheduledExecutorService scheduler;

    private RunnerManager()
    {
        this.scheduler = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    }

    // 只要在应用程序启动时调用一下这个方法，就可以完成本类的static变量的初始化，省得加锁了
    public static void init()
    {
        // do nothing here...
    }

    public static RunnerManager getInstance()
    {
        return instance;
    }

    // 委托运行某任务
    public void execute(AbstractDriveTask driveTask, Executable task)
    {
        execute(driveTask, task, 0, 0);
    }

    public void execute(AbstractDriveTask driveTask, Executable task, int milliseconds)
    {
        execute(driveTask, task, milliseconds, 0);
    }

    // 委托在milliseconds时间后运行某任务
    public void execute(AbstractDriveTask driveTask, Executable task, int milliseconds, int interval) {
        scheduler.schedule(() -> {
            task.execute(driveTask);
        }, milliseconds, TimeUnit.MILLISECONDS);
    }

    // 关闭线程组里的所有LoopRunner
    public void shutdown()
    {
        scheduler.shutdownNow();
    }
}
