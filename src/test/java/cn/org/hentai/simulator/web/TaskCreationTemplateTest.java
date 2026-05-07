package cn.org.hentai.simulator.web;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class TaskCreationTemplateTest
{
    @Test
    void singleTripCreationKeepsRequiredControlsAndTaskGroupRedirect() throws Exception
    {
        String template = read("/templates/task-create.ftlh");

        assertThat(template).contains("id=\"routeDropdown\"");
        assertThat(template).contains("id=\"routeId\"");
        assertThat(template).contains("id=\"vehicleNumber\"");
        assertThat(template).contains("id=\"deviceSn\"");
        assertThat(template).contains("id=\"simNumber\"");
        assertThat(template).contains("id=\"mileages\"");
        assertThat(template).contains("id=\"serverAddress\"");
        assertThat(template).contains("id=\"serverPort\"");
        assertThat(template).contains("id=\"btn-regen-all\"");
        assertThat(template).contains("id=\"btn-run\"");
        assertThat(template).contains("$.post('./run', params");
        assertThat(template).contains("window.location.href = '/task-groups/monitor?taskGroupId=' + result.data.taskGroupId");
    }

    @Test
    void batchTripCreationKeepsLaunchControlsProgressPollingAndTaskGroupRedirect() throws Exception
    {
        String template = read("/templates/task-batch-create.ftlh");

        assertThat(template).contains("id=\"route-list\"");
        assertThat(template).contains("id=\"btn-select-all\"");
        assertThat(template).contains("id=\"btn-clear-all\"");
        assertThat(template).contains("id=\"selected-count\"");
        assertThat(template).contains("id=\"vehicleCount\"");
        assertThat(template).contains("id=\"vehicleNumberPattern\"");
        assertThat(template).contains("id=\"deviceSnPattern\"");
        assertThat(template).contains("id=\"simNumberPattern\"");
        assertThat(template).contains("id=\"serverAddress\"");
        assertThat(template).contains("id=\"serverPort\"");
        assertThat(template).contains("id=\"reportIntervalSeconds\"");
        assertThat(template).contains("id=\"runDurationSeconds\"");
        assertThat(template).contains("id=\"rampUpBatchSize\"");
        assertThat(template).contains("id=\"rampUpIntervalMillis\"");
        assertThat(template).contains("id=\"batch-progress\"");
        assertThat(template).contains("startProgressPolling()");
        assertThat(template).contains("$.post('./progress'");
        assertThat(template).contains("$.post('./run', params");
        assertThat(template).contains("window.location.href = '/task-groups/monitor?taskGroupId=' + result.data.taskGroupId");
    }

    @Test
    void creationPagesUseSharedCardSectionsInsteadOfLegacySectionHeaderClass() throws Exception
    {
        String taskCreate = read("/templates/task-create.ftlh");
        String batchCreate = read("/templates/task-batch-create.ftlh");

        assertThat(taskCreate).contains("class=\"card-section__header\"");
        assertThat(batchCreate).contains("class=\"card-section__header\"");
        assertThat(taskCreate).doesNotContain("card-section-header");
        assertThat(batchCreate).doesNotContain("card-section-header");
    }

    @Test
    void creationPagesUseChineseTaskGroupAndRampUpWindowTerms() throws Exception
    {
        String taskCreate = read("/templates/task-create.ftlh");
        String batchCreate = read("/templates/task-batch-create.ftlh");
        String monitorList = read("/templates/monitor-list-index.ftlh");

        assertThat(taskCreate).contains("启动后会生成任务组");
        assertThat(batchCreate).contains("创建后会生成任务组");
        assertThat(monitorList).contains("当前按任务组过滤");
        assertThat(batchCreate).contains("ramp-up 窗口大小");
        assertThat(batchCreate).contains("0 表示一次性提交全部任务");
        assertThat(taskCreate).doesNotContain("Task Group");
        assertThat(batchCreate).doesNotContain("Task Group");
        assertThat(monitorList).doesNotContain("Task Group");
        assertThat(batchCreate).doesNotContain("ramp-up 批次大小");
    }

    private String read(String path) throws Exception
    {
        return new String(getClass().getResourceAsStream(path).readAllBytes(), StandardCharsets.UTF_8);
    }
}
