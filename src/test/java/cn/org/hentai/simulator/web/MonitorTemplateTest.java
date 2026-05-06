package cn.org.hentai.simulator.web;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class MonitorTemplateTest
{
    @Test
    void vehicleNumberFieldIsUpdatedByMatchingSelector() throws Exception
    {
        String template = new String(
                getClass().getResourceAsStream("/templates/monitor.ftlh").readAllBytes(),
                StandardCharsets.UTF_8
        );

        assertThat(template).contains("id=\"vehicleNumber\"");
        assertThat(template).contains("$('#vehicleNumber').html(info.vehicleNumber)");
        assertThat(template).doesNotContain("$('#vehiclenumber')");
    }
}
