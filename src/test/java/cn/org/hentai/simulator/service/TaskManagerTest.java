package cn.org.hentai.simulator.service;

import cn.org.hentai.simulator.domain.model.DrivePlan;
import cn.org.hentai.simulator.domain.model.TaskInfo;
import cn.org.hentai.simulator.engine.core.AbstractDriveTask;
import cn.org.hentai.simulator.service.monitor.TaskRuntimeSummary;
import cn.org.hentai.simulator.service.monitor.TaskStopResult;
import cn.org.hentai.simulator.web.vo.Page;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.yzh.protocol.basics.JTMessage;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskManagerTest
{
    private final TaskManager taskManager = TaskManager.getInstance();

    @AfterEach
    void tearDown()
    {
        taskManager.tasks.clear();
    }

    @Test
    void summarizesCurrentTaskStates()
    {
        TestDriveTask active = initializedTask(1L);
        TestDriveTask terminated = initializedTask(2L);
        terminated.terminate();

        taskManager.tasks.put(active.getId(), active);
        taskManager.tasks.put(terminated.getId(), terminated);

        TaskRuntimeSummary summary = taskManager.getRuntimeSummary();

        assertEquals(2L, summary.getTotalTasks());
        assertEquals(1L, summary.getActiveTasks());
        assertEquals(1L, summary.getTerminatedTasks());
    }

    @Test
    void terminatesAllActiveTasksAndSkipsAlreadyTerminatedTasks()
    {
        TestDriveTask active = initializedTask(1L);
        TestDriveTask terminated = initializedTask(2L);
        terminated.terminate();

        taskManager.tasks.put(active.getId(), active);
        taskManager.tasks.put(terminated.getId(), terminated);

        TaskStopResult result = taskManager.terminateActiveTasks();

        assertEquals(1L, result.getSucceeded());
        assertEquals(0L, result.getFailed());
    }

    @Test
    void filtersPagedTasksByStateAndKeyword()
    {
        TestDriveTask first = initializedTask(1L, "京A00001", "SN00001", "013800000001");
        TestDriveTask second = initializedTask(2L, "沪B00002", "SN00002", "013800000002");
        second.terminate();

        taskManager.tasks.put(first.getId(), first);
        taskManager.tasks.put(second.getId(), second);

        Page<TaskInfo> page = taskManager.find(1, 20, "行驶终止", "沪B");

        assertEquals(1L, page.getRecordCount());
        assertEquals(2L, page.getList().get(0).getId());
    }

    @Test
    void filtersPagedTasksByTaskGroupBeforePagination()
    {
        TestDriveTask first = initializedTask(1L, "京A00001", "SN00001", "013800000001");
        TestDriveTask second = initializedTask(2L, "京A00002", "SN00002", "013800000002");
        TestDriveTask third = initializedTask(3L, "京A00003", "SN00003", "013800000003");
        first.getInfo().setTaskGroupId("TG-1");
        first.getInfo().setTaskGroupDisplayName("批量创建 2 台 #1");
        second.getInfo().setTaskGroupId("TG-2");
        second.getInfo().setTaskGroupDisplayName("单车创建 #2");
        third.getInfo().setTaskGroupId("TG-1");
        third.getInfo().setTaskGroupDisplayName("批量创建 2 台 #1");

        taskManager.tasks.put(first.getId(), first);
        taskManager.tasks.put(second.getId(), second);
        taskManager.tasks.put(third.getId(), third);

        Page<TaskInfo> page = taskManager.find(2, 1, null, null, "TG-1");

        assertEquals(2L, page.getRecordCount());
        assertEquals(3L, page.getList().get(0).getId());
        assertEquals("TG-1", page.getList().get(0).getTaskGroupId());
        assertEquals("批量创建 2 台 #1", page.getList().get(0).getTaskGroupDisplayName());
    }

    private TestDriveTask initializedTask(long id)
    {
        return initializedTask(id, null, null, null);
    }

    private TestDriveTask initializedTask(long id, String vehicleNumber, String deviceSn, String simNumber)
    {
        TestDriveTask task = new TestDriveTask(id, 10L);
        HashMap<String, String> params = new HashMap<>();
        params.put("vehicle.number", vehicleNumber);
        params.put("device.sn", deviceSn);
        params.put("device.sim", simNumber);
        task.init(params, new DrivePlan());
        return task;
    }

    static class TestDriveTask extends AbstractDriveTask
    {
        TestDriveTask(long id, long routeId)
        {
            super(id, routeId);
        }

        @Override
        public void startup()
        {
        }

        @Override
        public void send(JTMessage msg)
        {
        }
    }
}
