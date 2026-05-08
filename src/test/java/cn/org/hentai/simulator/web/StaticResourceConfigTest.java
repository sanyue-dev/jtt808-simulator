package cn.org.hentai.simulator.web;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class StaticResourceConfigTest
{
    @Test
    void vendorAndImageAssetsHaveCachePolicyWithoutCachingAppCssAndJs() throws Exception
    {
        String config = readSource("src/main/java/cn/org/hentai/simulator/web/config/StaticResourceConfig.java");

        assertThat(config).contains("addResourceHandler(\"/static/vendor/**\")");
        assertThat(config).contains("addResourceLocations(\"classpath:/static/vendor/\")");
        assertThat(config).contains("TimeUnit.DAYS.toSeconds(7)");
        assertThat(config).contains("addResourceHandler(\"/static/img/**\")");
        assertThat(config).contains("addResourceLocations(\"classpath:/static/img/\")");
        assertThat(config).doesNotContain("addResourceHandler(\"/static/css/**\")");
        assertThat(config).doesNotContain("addResourceHandler(\"/static/js/**\")");
    }

    @Test
    void textCompressionIsEnabledForPageAndStaticTextResponses() throws Exception
    {
        String applicationYaml = read("/application.yml");

        assertThat(applicationYaml).contains("compression:");
        assertThat(applicationYaml).contains("enabled: true");
        assertThat(applicationYaml).contains("text/html");
        assertThat(applicationYaml).contains("text/css");
        assertThat(applicationYaml).contains("application/javascript");
        assertThat(applicationYaml).contains("application/json");
    }

    private String read(String path) throws Exception
    {
        return new String(getClass().getResourceAsStream(path).readAllBytes(), StandardCharsets.UTF_8);
    }

    private String readSource(String path) throws Exception
    {
        return Files.readString(Path.of(path), StandardCharsets.UTF_8);
    }
}
