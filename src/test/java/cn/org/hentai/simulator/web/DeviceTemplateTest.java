package cn.org.hentai.simulator.web;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceTemplateTest
{
    @Test
    void deviceCreateActionStaysInTableToolbarActions() throws Exception
    {
        String template = read("/templates/devices.ftlh");
        String commonCss = read("/static/css/common.css");

        assertThat(template).contains("<div class=\"table-toolbar\">");
        assertThat(template).contains("<form class=\"layui-form table-toolbar__filters\" id=\"device-filter\">");
        assertThat(template).contains("<div class=\"table-toolbar__actions\">");
        assertThat(template.indexOf("<style>")).isLessThan(template.indexOf("</head>"));
        assertThat(template).contains("<button type=\"button\" class=\"layui-btn layui-btn-sm\" id=\"btn-create-device\"");
        assertThat(template).contains("id=\"btn-clear-filter\"");

        int headerStart = template.indexOf("<div class=\"page-header\">");
        int headerEnd = template.indexOf("<div class=\"page-body\">");
        String header = template.substring(headerStart, headerEnd);
        assertThat(header).doesNotContain("id=\"btn-create-device\"");

        int filterStart = template.indexOf("<form class=\"layui-form table-toolbar__filters\" id=\"device-filter\">");
        int filterEnd = template.indexOf("</form>", filterStart);
        String filters = template.substring(filterStart, filterEnd);
        assertThat(filters).contains("id=\"btn-clear-filter\"");
        assertThat(filters).doesNotContain("id=\"btn-create-device\"");

        int actionsStart = template.indexOf("<div class=\"table-toolbar__actions\">");
        int actionsEnd = template.indexOf("</div>", actionsStart);
        String actions = template.substring(actionsStart, actionsEnd);
        assertThat(actions).contains("id=\"btn-create-device\"");

        assertThat(commonCss).contains(".table-toolbar");
        assertThat(commonCss).contains("justify-content: space-between;");
        assertThat(commonCss).contains(".table-toolbar__actions");
        assertThat(commonCss).contains("margin-left: auto;");
        assertThat(commonCss).contains(".table-toolbar .layui-btn");
        assertThat(commonCss).contains("margin-left: 0;");
        assertThat(commonCss).contains("transition: none;");
        assertThat(commonCss).contains(".table-toolbar .layui-input,");
        assertThat(commonCss).contains("height: 32px;");
        assertThat(commonCss).contains("line-height: 32px;");
    }

    @Test
    void deviceRemarkUsesSeparatedFullWidthFormRow() throws Exception
    {
        String template = read("/templates/devices.ftlh");

        assertThat(template).contains("class=\"layui-form-item device-form__remark\"");
        assertThat(template).contains(".device-form__remark");
        assertThat(template).contains("grid-column: 1 / -1;");
    }

    @Test
    void deviceStatusUsesSwitchInStatusColumnInsteadOfActionButton() throws Exception
    {
        String template = read("/templates/devices.ftlh");

        assertThat(template).contains("window.deviceTable = table.render($.extend(true, request.table('/device/list')");
        assertThat(template).contains("elem: '#device-table'");
        assertThat(template).doesNotContain("parseData: function(result)");
        assertThat(template).doesNotContain("result.error");
        assertThat(template).doesNotContain("result.data");
        assertThat(template).contains("{ field: 'vehicleNumber', title: '车牌号', align: 'left'");
        assertThat(template).contains("{ field: 'deviceSn', title: '终端ID', align: 'left'");
        assertThat(template).contains("{ field: 'simNumber', title: 'SIM卡号', align: 'left'");
        assertThat(template).contains("{ field: 'authMode', title: '上线方式', align: 'left'");
        assertThat(template).contains("{ field: 'serverAddress', title: '网关', align: 'left'");
        assertThat(template).contains("<table id=\"device-table\" lay-filter=\"device-table\"></table>");
        assertThat(template).contains("lay-skin=\"switch\"");
        assertThat(template).contains("title=\"启用|停用\"");
        assertThat(template).doesNotContain("lay-text=\"启用|停用\"");
        assertThat(template).contains("lay-filter=\"device-status\"");
        assertThat(template).contains("title: '状态'");
        assertThat(template).contains("field: 'enabled'");
        assertThat(template).contains("layui.form.on('switch(device-status)'");
        assertThat(template).contains("request.post('/device/status'");
        assertThat(template).doesNotContain("class=\"layui-btn layui-btn-xs layui-btn-primary btn-status\"");
        assertThat(template).doesNotContain("layui-badge layui-bg-gray");
        assertThat(template).doesNotContain("layui-badge layui-bg-green");
        assertThat(template).doesNotContain("appTable.render");

        int switchStart = template.indexOf("layui.form.on('switch(device-status)'");
        int switchEnd = template.indexOf("function removeDevice", switchStart);
        String switchHandler = template.substring(switchStart, switchEnd);
        assertThat(switchHandler).doesNotContain("reloadTable()");
    }

    @Test
    void deviceActionsUseLightweightLeftAlignedTextActions() throws Exception
    {
        String template = read("/templates/devices.ftlh");

        assertThat(template).contains(".device-actions");
        assertThat(template).contains("display: inline-flex;");
        assertThat(template).contains("title: '操作'");
        assertThat(template).contains("align: 'left'");
        assertThat(template).contains("width: 120");
        assertThat(template).contains("lay-event=\"edit\" class=\"device-action-link\">编辑");
        assertThat(template).contains("lay-event=\"remove\" class=\"device-action-link device-action-danger\">删除");
        assertThat(template).contains("table.on('tool(device-table)'");
        assertThat(template).doesNotContain("class=\"layui-btn layui-btn-primary layui-btn-xs btn-edit\"");
        assertThat(template).doesNotContain("class=\"layui-btn layui-btn-danger layui-btn-xs btn-remove\"");
        assertThat(template).doesNotContain("title: '操作',\n                    align: 'center',\n                    width: '220px'");
    }

    private String read(String path) throws Exception
    {
        return new String(getClass().getResourceAsStream(path).readAllBytes(), StandardCharsets.UTF_8);
    }
}
