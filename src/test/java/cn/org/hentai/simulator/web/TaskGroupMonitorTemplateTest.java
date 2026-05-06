package cn.org.hentai.simulator.web;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class TaskGroupMonitorTemplateTest
{
    @Test
    void monitorPollsSnapshotEverySecondAndPreservesExpandedGroups() throws Exception
    {
        String template = read("/templates/task-group-monitor.ftlh");

        assertThat(template).contains("$.post('/task-groups/snapshot'");
        assertThat(template).contains("setInterval(loadSnapshot, 1000)");
        assertThat(template).contains("expandedTaskGroupIds");
        assertThat(template).contains("data-field=\"runtimeSummary.totalTasks\"");
        assertThat(template).contains("id=\"task-group-list\"");
        assertThat(template).doesNotContain("paginate(");
    }

    @Test
    void creationPagesNavigateToTaskGroupMonitorAfterSuccess() throws Exception
    {
        String taskCreate = read("/templates/task-create.ftlh");
        String batchCreate = read("/templates/task-batch-create.ftlh");

        assertThat(taskCreate).contains("window.location.href = '/task-groups/monitor?taskGroupId=' + result.data.taskGroupId");
        assertThat(batchCreate).contains("window.location.href = '/task-groups/monitor?taskGroupId=' + result.data.taskGroupId");
    }

    private String read(String path) throws Exception
    {
        return new String(getClass().getResourceAsStream(path).readAllBytes(), StandardCharsets.UTF_8);
    }
}
