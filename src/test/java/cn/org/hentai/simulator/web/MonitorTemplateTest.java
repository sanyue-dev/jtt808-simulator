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

    @Test
    void tripTaskListDoesNotRenderRuntimeSummary() throws Exception
    {
        String template = read("/templates/monitor-list-index.ftlh");

        assertThat(template).doesNotContain("id=\"monitor-summary\"");
        assertThat(template).doesNotContain("data-field=\"runtimeResources.heapUsedBytes\"");
        assertThat(template).doesNotContain("data-field=\"schedulerDelay.averageDelayMillis\"");
    }

    @Test
    void tripTaskListDoesNotPollRuntimeSummary() throws Exception
    {
        String template = read("/templates/monitor-list-index.ftlh");

        assertThat(template).doesNotContain("/monitor/list/summary");
        assertThat(template).doesNotContain("loadSummary()");
    }

    @Test
    void tripTaskListKeepsGlobalStopAction() throws Exception
    {
        String template = read("/templates/monitor-list-index.ftlh");

        assertThat(template).contains("id=\"btn-terminate-all\"");
        assertThat(template).contains("停止全部未终止任务");
        assertThat(template).contains("$.post('/monitor/list/terminate-all'");
    }

    private String read(String path) throws Exception
    {
        return new String(getClass().getResourceAsStream(path).readAllBytes(), StandardCharsets.UTF_8);
    }
}
