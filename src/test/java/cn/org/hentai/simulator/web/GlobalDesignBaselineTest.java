package cn.org.hentai.simulator.web;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalDesignBaselineTest
{
    @Test
    void sharedStylesExposeIbmCarbonLikeDesignTokens() throws Exception
    {
        String css = read("/static/css/common.css");

        assertThat(css).contains("--color-primary: #0f62fe;");
        assertThat(css).contains("--color-primary-hover: #0050e6;");
        assertThat(css).contains("--color-primary-active: #002d9c;");
        assertThat(css).contains("--color-bg-page: #f4f4f4;");
        assertThat(css).contains("--color-bg-panel: #ffffff;");
        assertThat(css).contains("--color-text-primary: #161616;");
        assertThat(css).contains("--color-text-secondary: #525252;");
        assertThat(css).contains("--color-border: #e0e0e0;");
        assertThat(css).contains("--color-success: #24a148;");
        assertThat(css).contains("--color-warning: #f1c21b;");
        assertThat(css).contains("--color-danger: #da1e28;");
        assertThat(css).contains("\"IBM Plex Sans\", \"Noto Sans SC\", system-ui");
    }

    @Test
    void sharedComponentsKeepOperationalConsoleStructure() throws Exception
    {
        String css = read("/static/css/common.css");

        assertThat(css).contains(".layout__sidebar");
        assertThat(css).contains(".page-header");
        assertThat(css).contains("input[type=text]");
        assertThat(css).contains("select");
        assertThat(css).contains(".btn");
        assertThat(css).contains(".data-table");
        assertThat(css).contains(".pagination");
        assertThat(css).contains(".card-section");
        assertThat(css).contains(".toast");
        assertThat(css).contains(".confirm__dialog");
        assertThat(css).doesNotContain("@import url(");
        assertThat(css).doesNotContain("@font-face");
    }

    private String read(String path) throws Exception
    {
        return new String(getClass().getResourceAsStream(path).readAllBytes(), StandardCharsets.UTF_8);
    }
}
