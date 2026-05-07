package cn.org.hentai.simulator.web;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalDesignBaselineTest
{
    @Test
    void sharedStylesExposeLayuiApplicationShellTokens() throws Exception
    {
        String css = read("/static/css/common.css");

        assertThat(css).contains("--app-side-width: 200px;");
        assertThat(css).contains("--app-side-collapsed-width: 60px;");
        assertThat(css).contains("--app-header-height: 56px;");
        assertThat(css).contains("--app-primary: #16baaa;");
        assertThat(css).contains("--app-danger: #ff5722;");
        assertThat(css).contains("--color-primary: var(--app-primary);");
        assertThat(css).contains("--color-danger: var(--app-danger);");
        assertThat(css).contains("--color-bg-page: var(--app-bg);");
        assertThat(css).contains("--color-bg-panel: var(--app-panel);");
    }

    @Test
    void sharedComponentsKeepOperationalConsoleStructure() throws Exception
    {
        String css = read("/static/css/common.css");

        assertThat(css).contains(".app-layout");
        assertThat(css).contains(".app-sidebar");
        assertThat(css).contains(".app-body");
        assertThat(css).contains(".app-page");
        assertThat(css).contains("width: 100% !important;");
        assertThat(css).contains(".app-menu .layui-nav-item");
        assertThat(css).contains("display: flex;");
        assertThat(css).contains("align-items: center;");
        assertThat(css).contains("justify-content: center;");
        assertThat(css).contains(".page-header");
        assertThat(css).contains(".layui-card");
        assertThat(css).contains(".layui-form-label");
        assertThat(css).contains(".app-table-panel");
        assertThat(css).contains(".app-pagination");
        assertThat(css).contains(".app-metrics");
        assertThat(css).doesNotContain(".btn--");
        assertThat(css).doesNotContain(".data-table");
        assertThat(css).doesNotContain(".card-section");
        assertThat(css).doesNotContain(".toast");
        assertThat(css).doesNotContain(".confirm__dialog");
        assertThat(css).doesNotContain(".grid__row");
        assertThat(css).doesNotContain(".grid__col");
        assertThat(css).doesNotContain("@import url(");
        assertThat(css).doesNotContain("@font-face");
    }

    @Test
    void applicationShellUsesDefaultPageHeaderWithoutEmptyLayuiAdminHeader() throws Exception
    {
        String css = read("/static/css/common.css");
        String taskGroup = read("/templates/task-group-monitor.ftlh");

        assertThat(css).contains(".app-sidebar {\n    top: 0 !important;");
        assertThat(css).contains(".app-body {\n    position: absolute;\n    left: var(--app-side-width) !important;\n    top: 0 !important;");
        assertThat(css).contains("border-bottom: 1px solid rgba(255,255,255,.08);");
        assertThat(css).contains(".page-header h2 { margin: 0; color: inherit;");
        assertThat(css).doesNotContain(".app-header");
        assertThat(css).doesNotContain(".page-header.layui-bg-gray");
        assertThat(taskGroup).doesNotContain("<#include \"inc/header.ftlh\">");
        assertThat(taskGroup).doesNotContain("page-header layui-bg-gray");
    }

    @Test
    void routeEditorPanelRowsUseRouteEditorLayout() throws Exception
    {
        String template = read("/templates/route-create.ftlh");

        assertThat(template).contains(".route-editor-page #panel");
        assertThat(template).contains("background-color: var(--color-bg-muted);");
        assertThat(template).contains("#panel .route-editor-row {\n            display: flex;");
        assertThat(template).contains("background: var(--color-bg-panel);");
        assertThat(template).contains("min-height: 44px;");
        assertThat(template).contains(".station .station-value input");
        assertThat(template).contains("height: 40px;");
        assertThat(template).doesNotContain("grid__row");
        assertThat(template).doesNotContain("grid__col");
    }

    @Test
    void sharedScriptsUseLayuiApplicationHelpersOnly() throws Exception
    {
        String script = read("/static/js/common.js");

        assertThat(script).contains("function appNotify(type, text, timeout)");
        assertThat(script).contains("function appConfirm(text, onOk, onCancel)");
        assertThat(script).contains("var appTable = (function()");
        assertThat(script).contains("layui.laypage.render({");
        assertThat(script).doesNotContain("$.fn.paginate");
        assertThat(script).doesNotContain("confirmDialog");
        assertThat(script).doesNotContain("toastr");
        assertThat(script).doesNotContain("blockUI");
    }

    @Test
    void routeEditorTroubleSegmentEventCategoryUsesLayuiSelect() throws Exception
    {
        String template = read("/templates/route-create.ftlh");

        assertThat(template).contains("class=\"route-event-select\"");
        assertThat(template).contains("data-role=\"eventCode\"");
        assertThat(template).contains("lay-filter=\"routeEvent\"");
        assertThat(template).contains("layui.form.on('select(routeEvent)'");
        assertThat(template).doesNotContain("selectRouteEventOption");
        assertThat(template).doesNotContain("task__dropdown task__dropdown--compact route-event-dropdown");
    }

    private String read(String path) throws Exception
    {
        return new String(getClass().getResourceAsStream(path).readAllBytes(), StandardCharsets.UTF_8);
    }
}
