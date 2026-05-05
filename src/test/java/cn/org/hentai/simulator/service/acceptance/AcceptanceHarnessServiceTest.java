package cn.org.hentai.simulator.service.acceptance;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AcceptanceHarnessServiceTest
{
    @Test
    void validatesTenKAcceptanceConfig()
    {
        AcceptanceHarnessService service = new AcceptanceHarnessService(null);
        AcceptanceConfig config = new AcceptanceConfig();
        config.setTerminalCount(10000);
        config.setReportIntervalSeconds(5);
        config.setRunDurationSeconds(300);
        config.setRampUpBatchSize(100);
        config.setRampUpIntervalMillis(1000);
        config.setServerAddress("127.0.0.1");
        config.setServerPort(20021);

        assertDoesNotThrow(() -> service.validate(config));
    }

    @Test
    void rejectsUnsupportedTerminalCounts()
    {
        AcceptanceHarnessService service = new AcceptanceHarnessService(null);
        AcceptanceConfig config = new AcceptanceConfig();
        config.setTerminalCount(5000);
        config.setServerAddress("127.0.0.1");
        config.setServerPort(20021);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.validate(config));
        assertEquals("验收阶段仅支持 1000 或 10000 个终端", ex.getMessage());
    }

    @Test
    void rejectsInvalidRampUpValues()
    {
        AcceptanceHarnessService service = new AcceptanceHarnessService(null);
        AcceptanceConfig config = new AcceptanceConfig();
        config.setTerminalCount(10000);
        config.setServerAddress("127.0.0.1");
        config.setServerPort(20021);
        config.setRampUpBatchSize(0);

        IllegalArgumentException batchSizeError = assertThrows(IllegalArgumentException.class, () -> service.validate(config));
        assertEquals("ramp-up 批次大小必须大于 0", batchSizeError.getMessage());

        config.setRampUpBatchSize(100);
        config.setRampUpIntervalMillis(0);
        IllegalArgumentException intervalError = assertThrows(IllegalArgumentException.class, () -> service.validate(config));
        assertEquals("ramp-up 间隔必须大于 0 毫秒", intervalError.getMessage());
    }

    @Test
    void buildsRampUpLaunchWindows()
    {
        AcceptanceHarnessService service = new AcceptanceHarnessService(null);

        List<AcceptanceHarnessService.LaunchWindow> windows = service.buildLaunchWindows(10000, 100, 1000);

        assertEquals(100, windows.size());
        assertEquals(0, windows.get(0).getStartIndex());
        assertEquals(100, windows.get(0).getEndIndex());
        assertEquals(0L, windows.get(0).getDelayMillis());
        assertEquals(9900, windows.get(99).getStartIndex());
        assertEquals(10000, windows.get(99).getEndIndex());
        assertEquals(99000L, windows.get(99).getDelayMillis());
    }
}
