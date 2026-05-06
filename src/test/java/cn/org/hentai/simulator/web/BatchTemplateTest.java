package cn.org.hentai.simulator.web;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class BatchTemplateTest
{
    @Test
    void batchPagePollsCurrentLaunchProgressAfterSubmittingRun() throws Exception
    {
        String template = new String(
                getClass().getResourceAsStream("/templates/task-batch-create.ftlh").readAllBytes(),
                StandardCharsets.UTF_8
        );

        assertThat(template).contains("id=\"batch-progress\"");
        assertThat(template).contains("$.post('./progress'");
        assertThat(template).contains("startProgressPolling()");
    }
}
