package cn.org.hentai.simulator.service;

import cn.org.hentai.simulator.domain.model.DrivePlan;
import cn.org.hentai.simulator.domain.model.Point;
import cn.org.hentai.simulator.domain.model.TaskInfo;
import cn.org.hentai.simulator.domain.model.TaskLifecycleObserver;
import cn.org.hentai.simulator.engine.core.AbstractDriveTask;
import cn.org.hentai.simulator.engine.core.SimpleDriveTask;
import cn.org.hentai.simulator.engine.log.Log;
import cn.org.hentai.simulator.engine.runner.Executable;
import cn.org.hentai.simulator.domain.enums.TaskState;
import cn.org.hentai.simulator.service.monitor.TaskLifecycleObservers;
import cn.org.hentai.simulator.service.monitor.TaskRuntimeMetrics;
import cn.org.hentai.simulator.service.monitor.TaskRuntimeSummary;
import cn.org.hentai.simulator.service.monitor.TaskStopResult;
import cn.org.hentai.simulator.web.vo.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 行程任务管理器
 */
public final class TaskManager
{
    static Logger logger = LoggerFactory.getLogger(TaskManager.class);

    Object lock;
    NavigableMap<Long, AbstractDriveTask> tasks;
    AtomicLong sequence;
    AtomicLong index;
    TaskRuntimeMetrics runtimeMetrics;

    static final Comparator<AbstractDriveTask> SORT_COMPARATOR = new Comparator<AbstractDriveTask>()
    {
        @Override
        public int compare(AbstractDriveTask o1, AbstractDriveTask o2)
        {
            long x = o1.getId() - o2.getId();
            if (x > 0) return 1;
            else if (x == 0) return 0;
            else return -1;
        }
    };

    private TaskManager()
    {
        this.lock = new Object();
        this.tasks = new ConcurrentSkipListMap<Long, AbstractDriveTask>();

        this.index = new AtomicLong(0L);
        this.sequence = new AtomicLong(0L);
        this.runtimeMetrics = new TaskRuntimeMetrics();
    }

    /**
     * 按给定的参数集，开启任务
     * @param params
     * @param routeId
     */
    public long run(Map params, Long routeId)
    {
        return run(params, routeId, 5, null);
    }

    public long run(Map params, Long routeId, int reportIntervalSeconds, TaskLifecycleObserver lifecycleObserver)
    {
        long taskId = nextTaskId();
        run(taskId, params, routeId, reportIntervalSeconds, lifecycleObserver);
        return taskId;
    }

    public void run(long taskId, Map params, Long routeId, int reportIntervalSeconds, TaskLifecycleObserver lifecycleObserver)
    {
        // TODO: 需要检查一下是不是有冲突（终端ID及SIM卡号不能重复）
        DrivePlan plan = RouteManager.getInstance().generate(routeId, new Date(), reportIntervalSeconds);

        AbstractDriveTask task = new SimpleDriveTask(taskId, routeId);
        task.init(params, plan, TaskLifecycleObservers.composite(runtimeMetrics, lifecycleObserver));
        task.startup();
        tasks.put(task.getId(), task);
    }

    public long reserveIndexes(int count)
    {
        if (count < 1) throw new IllegalArgumentException("数量必须大于 0");
        return this.index.getAndAdd(count) + 1;
    }

    public long nextTaskId()
    {
        return this.sequence.addAndGet(1L);
    }

    public long nextIndex()
    {
        return this.index.addAndGet(1L);
    }

    // 分页查找，用于列表显示运行中的行程任务状态
    public Page<TaskInfo> find(int pageIndex, int pageSize)
    {
        return find(pageIndex, pageSize, null, null);
    }

    public Page<TaskInfo> find(int pageIndex, int pageSize, String state, String keyword)
    {
        List<TaskInfo> results = new ArrayList<TaskInfo>(pageSize);
        int start = Math.max((pageIndex - 1) * pageSize, 0);
        long matched = 0L;
        for (AbstractDriveTask task : tasks.values())
        {
            TaskInfo info = task.getInfo();
            runtimeMetrics.applyFailureInfo(info);
            if (matches(info, state, keyword) == false) continue;
            if (matched >= start && results.size() < pageSize) results.add(info);
            matched++;
        }
        Page<TaskInfo> page = new Page(pageIndex, pageSize);
        page.setList(results);
        page.setRecordCount(matched);
        return page;
    }

    private boolean matches(TaskInfo task, String state, String keyword)
    {
        if (StringUtils.hasText(state) && state.equals(task.getState()) == false) return false;
        if (StringUtils.hasText(keyword) == false) return true;
        return contains(task.getVehicleNumber(), keyword)
                || contains(task.getDeviceSn(), keyword)
                || contains(task.getSimNumber(), keyword);
    }

    private boolean contains(String value, String keyword)
    {
        return value != null && value.contains(keyword);
    }

    public TaskRuntimeSummary getRuntimeSummary()
    {
        long total = 0L;
        long active = 0L;
        long parking = 0L;
        long terminated = 0L;
        for (AbstractDriveTask task : tasks.values())
        {
            total++;
            TaskState state = task.getState();
            if (state == TaskState.terminated) terminated++;
            else active++;
            if (state == TaskState.parking) parking++;
        }
        return runtimeMetrics.summary(total, active, parking, terminated);
    }

    // 获取timeAfter时间之后的任务日志
    public List<Log> getLogsById(Long id, long timeAfter)
    {
        AbstractDriveTask task = tasks.get(id);
        if (task != null) return task.getLogs(timeAfter);
        else return null;
    }

    public TaskInfo getById(Long id)
    {
        AbstractDriveTask task = tasks.get(id);
        if (task == null) return null;
        else
        {
            TaskInfo info = task.getInfo();
            runtimeMetrics.applyFailureInfo(info);
            return info;
        }
    }

    // 获取当前位置信息
    public Point getCurrentPositionById(Long id)
    {
        AbstractDriveTask task = tasks.get(id);
        if (task == null) return null;
        else return task.getCurrentPosition();
    }

    // 修改车辆状态标志位
    public void setStateFlagById(Long id, int index, boolean on)
    {
        AbstractDriveTask task = tasks.get(id);
        if (task != null) task.setStateFlag(index, on);
    }

    // 修改报警状态标志位
    public void setWarningFlagById(Long id, int index, boolean on)
    {
        AbstractDriveTask task = tasks.get(id);
        if (task != null) task.setWarningFlag(index, on);
    }

    // 任务终止
    // TODO: 什么时候把任务从map里删除掉好呢？
    public void terminate(Long id)
    {
        AbstractDriveTask task = tasks.get(id);
        if (task == null) throw new RuntimeException("无此任务或任务已终止");
        task.execute(new Executable()
        {
            @Override
            public void execute(AbstractDriveTask driveTask)
            {
                driveTask.terminate();
            }
        });
    }

    public TaskStopResult terminateActiveTasks()
    {
        TaskStopResult result = new TaskStopResult();
        for (AbstractDriveTask task : tasks.values())
        {
            if (task.getState() == TaskState.terminated) continue;
            try
            {
                terminate(task.getId());
                result.recordSuccess();
            }
            catch(RuntimeException ex)
            {
                result.recordFailure(task.getId(), ex.getMessage());
            }
        }
        return result;
    }

    static final TaskManager instance = new TaskManager();
    public static void init()
    {
        // ...
    }
    public static TaskManager getInstance()
    {
        return instance;
    }
}
