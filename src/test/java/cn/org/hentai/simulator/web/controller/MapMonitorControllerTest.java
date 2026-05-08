package cn.org.hentai.simulator.web.controller;

import cn.org.hentai.simulator.web.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MapMonitorControllerTest
{
    @Test
    void infoRejectsUnknownTaskId()
    {
        MapMonitorController controller = new MapMonitorController();

        ValidationException ex = assertThrows(ValidationException.class, () -> controller.info(-1L));

        assertEquals("任务不存在或已结束: -1", ex.getMessage());
    }
}
