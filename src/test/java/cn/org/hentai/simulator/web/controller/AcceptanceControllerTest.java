package cn.org.hentai.simulator.web.controller;

import cn.org.hentai.simulator.web.vo.Result;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AcceptanceControllerTest
{
    @Test
    void oneKEndpointRejectsTenKTerminalCount()
    {
        AcceptanceController controller = new AcceptanceController();
        controller.vehicleServerAddr = "127.0.0.1";
        controller.vehicleServerPort = 20021;

        Result result = controller.run(10000, 5, 300, null, null, "京%06d", "A%06d", "013800%06d", null);

        assertEquals(1, result.getError().getCode());
        assertEquals("1k 阶段验收必须启动 1000 个终端", result.getError().getReason());
    }
}
