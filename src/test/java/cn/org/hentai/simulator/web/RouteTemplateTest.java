package cn.org.hentai.simulator.web;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class RouteTemplateTest
{
    @Test
    void routeActionsUseLightweightLeftAlignedTextActions() throws Exception
    {
        String template = read("/templates/routes.ftlh");

        assertThat(template).contains(".route-actions");
        assertThat(template).contains("display: inline-flex;");
        assertThat(template).contains("title: '操作'");
        assertThat(template).contains("align: 'left'");
        assertThat(template).contains("width: 120");
        assertThat(template).contains("class=\"route-action-link\">编辑");
        assertThat(template).contains("lay-event=\"remove\" class=\"route-action-link route-action-danger\">删除");
        assertThat(template).doesNotContain("class=\"layui-btn layui-btn-xs\">编辑");
        assertThat(template).doesNotContain("class=\"layui-btn layui-btn-xs layui-btn-danger\">删除");
    }

    private String read(String path) throws Exception
    {
        return new String(getClass().getResourceAsStream(path).readAllBytes(), StandardCharsets.UTF_8);
    }
}
