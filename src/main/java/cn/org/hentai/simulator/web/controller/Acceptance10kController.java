package cn.org.hentai.simulator.web.controller;

import cn.org.hentai.simulator.service.acceptance.AcceptanceConfig;
import cn.org.hentai.simulator.service.acceptance.AcceptanceHarnessService;
import cn.org.hentai.simulator.service.acceptance.AcceptanceRun;
import cn.org.hentai.simulator.web.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/acceptance/10k")
public class Acceptance10kController
{
    @Autowired
    AcceptanceHarnessService acceptanceHarnessService;

    @Value("${vehicle-server.addr}")
    String vehicleServerAddr;

    @Value("${vehicle-server.port}")
    int vehicleServerPort;

    @PostMapping("/run")
    public Result run(@RequestParam(defaultValue = "5") int reportIntervalSeconds,
                      @RequestParam(defaultValue = "300") int runDurationSeconds,
                      @RequestParam(defaultValue = "100") int rampUpBatchSize,
                      @RequestParam(defaultValue = "1000") int rampUpIntervalMillis,
                      @RequestParam(required = false) String serverAddress,
                      @RequestParam(required = false) Integer serverPort,
                      @RequestParam(defaultValue = "京%06d") String vehicleNumberPattern,
                      @RequestParam(defaultValue = "A%06d") String deviceSnPattern,
                      @RequestParam(defaultValue = "013800%06d") String simNumberPattern,
                      @RequestParam(required = false) List<Long> routeIds)
    {
        try
        {
            AcceptanceConfig config = new AcceptanceConfig();
            config.setTerminalCount(10000);
            config.setReportIntervalSeconds(reportIntervalSeconds);
            config.setRunDurationSeconds(runDurationSeconds);
            config.setRampUpBatchSize(rampUpBatchSize);
            config.setRampUpIntervalMillis(rampUpIntervalMillis);
            config.setServerAddress(serverAddress == null || serverAddress.isBlank() ? vehicleServerAddr : serverAddress);
            config.setServerPort(serverPort == null ? vehicleServerPort : serverPort);
            config.setVehicleNumberPattern(vehicleNumberPattern);
            config.setDeviceSnPattern(deviceSnPattern);
            config.setSimNumberPattern(simNumberPattern);
            config.setRouteIds(routeIds);

            AcceptanceRun run = acceptanceHarnessService.start(config);
            return new Result().withData(Result.values("runId", run.getId(), "config", run.getConfig(), "summary", run.getSummary()));
        }
        catch(Exception ex)
        {
            return Result.error(ex);
        }
    }

    @GetMapping("/{runId}")
    public Result status(@PathVariable String runId)
    {
        try
        {
            AcceptanceRun run = acceptanceHarnessService.get(runId);
            return new Result().withData(Result.values(
                    "runId", run.getId(),
                    "state", run.getState(),
                    "finishFailureReason", run.getFinishFailureReason(),
                    "startedAt", run.getStartedAt(),
                    "finishedAt", run.getFinishedAt(),
                    "config", run.getConfig(),
                    "summary", run.getSummary(),
                    "records", run.getRecords()
            ));
        }
        catch(Exception ex)
        {
            return Result.error(ex);
        }
    }
}
