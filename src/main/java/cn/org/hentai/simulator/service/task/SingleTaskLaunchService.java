package cn.org.hentai.simulator.service.task;

import cn.org.hentai.simulator.service.TaskManager;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class SingleTaskLaunchService
{
    private final TaskGroupMonitorService taskGroupMonitorService;

    public SingleTaskLaunchService(TaskGroupMonitorService taskGroupMonitorService)
    {
        this.taskGroupMonitorService = taskGroupMonitorService;
    }

    public TaskCreationResult launch(SingleTaskLaunchRequest request)
    {
        validate(request);
        String simNumber = normalizeSimNumber(request.getSimNumber());
        int kilometers = parseKilometers(request.getMileages());
        Map<String, String> params = params(request, simNumber, kilometers);

        TaskCreationResult creation = taskGroupMonitorService.createGroup(TaskGroupSource.SINGLE, 1);
        try
        {
            long taskId = TaskManager.getInstance().run(params, request.getRouteId(), 5, taskGroupMonitorService.observer(creation.getTaskGroupId()));
            taskGroupMonitorService.recordTaskStarted(creation.getTaskGroupId(), taskId);
            return creation;
        }
        catch(RuntimeException ex)
        {
            taskGroupMonitorService.recordLaunchFailure(creation.getTaskGroupId(), ex);
            throw ex;
        }
    }

    private void validate(SingleTaskLaunchRequest request)
    {
        if (StringUtils.isEmpty(request.getVehicleNumber()) || request.getVehicleNumber().matches("^[\u4e00-\u9fa5]\\w{6,7}$") == false)
            throw new RuntimeException("请填写正确的车牌号");

        if (StringUtils.isEmpty(request.getDeviceSn()) || request.getDeviceSn().matches("^\\w{7,30}$") == false)
            throw new RuntimeException("请填写正确的终端ID");

        if (StringUtils.isEmpty(request.getSimNumber()) || request.getSimNumber().matches("^\\d{11,12}$") == false)
            throw new RuntimeException("请填写正确的SIM卡号");

        if (StringUtils.isEmpty(request.getServerPort()) || request.getServerPort().matches("^\\d{1,5}$") == false)
            throw new RuntimeException("请填写正确的服务器端口");
    }

    private String normalizeSimNumber(String simNumber)
    {
        if (simNumber.length() < 12) return ("0000000000000" + simNumber).replaceAll("^0+(\\d{12})$", "$1");
        return simNumber;
    }

    private int parseKilometers(String mileages)
    {
        if (StringUtils.isEmpty(mileages)) return 0;
        if (mileages.matches("^\\d+$")) return Integer.parseInt(mileages);
        throw new RuntimeException("请填写正确的初始里程数，必须为整数，如：“100”表示100公里。");
    }

    private Map<String, String> params(SingleTaskLaunchRequest request, String simNumber, int kilometers)
    {
        Map<String, String> params = new HashMap<>();
        params.put("vehicle.number", request.getVehicleNumber());
        params.put("device.sn", request.getDeviceSn());
        params.put("device.sim", simNumber);
        params.put("server.address", request.getServerAddress());
        params.put("server.port", request.getServerPort());
        params.put("mode", request.getMode());
        params.put("mileages", String.valueOf(Math.max(kilometers, 0)));
        return params;
    }
}
