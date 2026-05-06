package cn.org.hentai.simulator.web;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class MonitorTemplateTest
{
    @Test
    void vehicleNumberFieldIsUpdatedByMatchingSelector() throws Exception
    {
        String template = read("/templates/monitor.ftlh");

        assertThat(template).contains("id=\"vehicleNumber\"");
        assertThat(template).contains("$('#vehicleNumber').html(info.vehicleNumber)");
        assertThat(template).doesNotContain("$('#vehiclenumber')");
    }

    @Test
    void tripTaskListKeepsTaskGroupFilterInPaginatedRequests() throws Exception
    {
        String template = read("/templates/monitor-list-index.ftlh");

        assertThat(template).contains("name=\"taskGroupId\"");
        assertThat(template).contains("new URLSearchParams(window.location.search).get('taskGroupId')");
        assertThat(template).contains("taskGroupDisplayName");
        assertThat(template).contains("title: '任务组'");
    }

    private String read(String path) throws Exception
    {
        return new String(getClass().getResourceAsStream(path).readAllBytes(), StandardCharsets.UTF_8);
    }
}
