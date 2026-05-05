package cn.org.hentai.simulator.service.acceptance;

import cn.org.hentai.simulator.domain.model.TerminalIdentity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdentityBatchGeneratorTest
{
    private final IdentityBatchGenerator generator = new IdentityBatchGenerator();

    @Test
    void generatesUniqueTerminalIdentities()
    {
        List<TerminalIdentity> identities = generator.generate(3, 1, "京%06d", "A%06d", "013800%06d");

        assertEquals("京000001", identities.get(0).getVehicleNumber());
        assertEquals("A000001", identities.get(0).getDeviceSn());
        assertEquals("013800000001", identities.get(0).getSimNumber());
        assertEquals("京000003", identities.get(2).getVehicleNumber());
    }

    @Test
    void failsWhenPatternGeneratesDuplicateVehicleNumbers()
    {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> generator.generate(2, 1, "京A00000", "A%06d", "013800%06d"));

        assertEquals("车牌号生成重复: 京A00000", ex.getMessage());
    }

    @Test
    void failsWhenPatternCannotFormat()
    {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> generator.generate(1, 1, "京%s%s", "A%06d", "013800%06d"));

        assertTrue(ex.getMessage().startsWith("车牌号规则无法格式化 index=1"));
    }
}
