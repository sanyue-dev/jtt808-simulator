package cn.org.hentai.simulator.web.controller;

import cn.org.hentai.simulator.domain.entity.Route;
import cn.org.hentai.simulator.domain.model.DrivePlan;
import cn.org.hentai.simulator.domain.model.TaskInfo;
import cn.org.hentai.simulator.engine.core.AbstractDriveTask;
import cn.org.hentai.simulator.service.RouteService;
import cn.org.hentai.simulator.service.TaskManager;
import cn.org.hentai.simulator.web.vo.Page;
import cn.org.hentai.simulator.web.vo.Result;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.yzh.protocol.basics.JTMessage;

import java.util.HashMap;
import java.util.NavigableMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MonitorControllerTest
{
    @AfterEach
    void tearDown()
    {
        tasks().clear();
    }

    @Test
    @SuppressWarnings("unchecked")
    void listJsonFiltersTasksByTaskGroupId()
    {
        MonitorController controller = new MonitorController();
        ReflectionTestUtils.setField(controller, "routeService", new FakeRouteService());
        TestDriveTask first = initializedTask(1L, "TG-1", "批量创建 2 台 #1");
        TestDriveTask second = initializedTask(2L, "TG-2", "单车创建 #2");
        tasks().put(first.getId(), first);
        tasks().put(second.getId(), second);

        Result result = controller.listJson(1, 20, null, null, "TG-1");

        Page<TaskInfo> page = (Page<TaskInfo>) result.getData();
        assertEquals(0, result.getError().getCode());
        assertEquals(1L, page.getRecordCount());
        assertEquals(1L, page.getList().get(0).getId());
        assertEquals("TG-1", page.getList().get(0).getTaskGroupId());
        assertEquals("批量创建 2 台 #1", page.getList().get(0).getTaskGroupDisplayName());
        assertEquals("测试线路", page.getList().get(0).getRouteName());
    }

    @SuppressWarnings("unchecked")
    private NavigableMap<Long, AbstractDriveTask> tasks()
    {
        return (NavigableMap<Long, AbstractDriveTask>) ReflectionTestUtils.getField(TaskManager.getInstance(), "tasks");
    }

    private TestDriveTask initializedTask(long id, String taskGroupId, String taskGroupDisplayName)
    {
        TestDriveTask task = new TestDriveTask(id, 10L);
        HashMap<String, String> params = new HashMap<>();
        params.put("vehicle.number", "京A00001");
        params.put("device.sn", "SN00001");
        params.put("device.sim", "013800000001");
        task.init(params, new DrivePlan());
        task.getInfo().setTaskGroupId(taskGroupId);
        task.getInfo().setTaskGroupDisplayName(taskGroupDisplayName);
        return task;
    }

    private static class FakeRouteService extends RouteService
    {
        @Override
        public Route getById(Long id)
        {
            return new Route().withName("测试线路").withMileages(1000);
        }
    }

    private static class TestDriveTask extends AbstractDriveTask
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
