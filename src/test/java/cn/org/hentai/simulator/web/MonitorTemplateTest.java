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
    void monitorDetailMapDoesNotOccupyPanels() throws Exception
    {
        String template = read("/templates/monitor.ftlh");

        assertThat(template).contains(".monitor__map");
        assertThat(template).contains("right: 350px;");
        assertThat(template).contains("bottom: 300px;");
        assertThat(template).contains(".monitor__info-panel");
        assertThat(template).contains("z-index: 2;");
        assertThat(template).contains(".monitor__log-panel");
        assertThat(template).contains("z-index: 3;");
        assertThat(template).contains("<link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.css\"");
        assertThat(template.indexOf("leaflet.css")).isLessThan(template.indexOf("</head>"));
    }

    @Test
    void monitorFlagControlsUseGlobalTypographyAndReadableSize() throws Exception
    {
        String template = read("/templates/monitor.ftlh");

        assertThat(template).contains(".monitor__grids");
        assertThat(template).contains("grid-template-columns: repeat(2, minmax(0, 1fr));");
        assertThat(template).contains("font-family: inherit;");
        assertThat(template).contains("font-size: 13px;");
        assertThat(template).contains("min-height: 30px;");
        assertThat(template).contains("text-overflow: ellipsis;");
        assertThat(template).doesNotContain("font-size: 10px;");
        assertThat(template).doesNotContain("font-family: consolas;");
    }

    @Test
    void tripTaskListKeepsTaskGroupFilterInPaginatedRequests() throws Exception
    {
        String template = read("/templates/monitor-list-index.ftlh");

        assertThat(template).contains("window.monitorTable = table.render($.extend(true, request.table('/monitor/list/json')");
        assertThat(template).contains("elem: '#monitor-table'");
        assertThat(template).doesNotContain("parseData: function(result)");
        assertThat(template).doesNotContain("result.error");
        assertThat(template).doesNotContain("result.data");
        assertThat(template).contains("name=\"taskGroupId\"");
        assertThat(template).contains("id=\"task-group-filter-context\"");
        assertThat(template).contains("id=\"task-group-filter-value\"");
        assertThat(template).contains("id=\"task-group-filter-link\"");
        assertThat(template).contains("new URLSearchParams(window.location.search).get('taskGroupId')");
        assertThat(template).contains("taskGroupDisplayName");
        assertThat(template).contains("title: '任务组'");
        assertThat(template).doesNotContain("appTable.render");
        assertThat(template).doesNotContain("id=\"monitor-page\"");
        assertThat(template).doesNotContain("$.fn.paginate");
        assertThat(template).doesNotContain(".paginate(");
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
        assertThat(template).contains("request.post('/monitor/list/terminate-all'");
    }

    @Test
    void tripTaskFilterToolbarKeepsKeywordInputCompactOnDesktop() throws Exception
    {
        String template = read("/templates/monitor-list-index.ftlh");

        assertThat(template).contains("<div class=\"table-toolbar\">");
        assertThat(template).contains("<form class=\"layui-form table-toolbar__filters monitor-toolbar__filters\"");
        assertThat(template).contains("<div class=\"table-toolbar__actions\">");
        assertThat(template).contains("class=\"layui-input\" type=\"text\" name=\"keyword\"");
        assertThat(template).contains("monitor-toolbar__keyword");
        assertThat(template).contains(".monitor-toolbar__keyword");
        assertThat(template).contains("width: 260px;");
    }

    @Test
    void tripTaskActionsUseLightweightLeftAlignedTextActions() throws Exception
    {
        String template = read("/templates/monitor-list-index.ftlh");

        assertThat(template).contains(".monitor-actions");
        assertThat(template).contains("display: inline-flex;");
        assertThat(template).contains("title: '操作'");
        assertThat(template).contains("align: 'left'");
        assertThat(template).contains("width: 120");
        assertThat(template).contains("target=\"_blank\" class=\"monitor-action-link\">详情");
        assertThat(template).contains("lay-event=\"terminate\" class=\"monitor-action-link monitor-action-danger\">停止");
        assertThat(template).doesNotContain("class=\"layui-btn layui-btn-xs\">详情");
        assertThat(template).doesNotContain("class=\"layui-btn layui-btn-xs layui-btn-primary\">停止");
    }

    private String read(String path) throws Exception
    {
        return new String(getClass().getResourceAsStream(path).readAllBytes(), StandardCharsets.UTF_8);
    }
}
