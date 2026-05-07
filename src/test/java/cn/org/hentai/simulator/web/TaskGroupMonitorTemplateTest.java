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

        assertThat(template).contains("$.post('/task-groups/snapshot', function(result)");
        assertThat(template).contains("setInterval(loadSnapshot, 1000)");
        assertThat(template).contains("expandedTaskGroupIds");
        assertThat(template).contains("data-field=\"runtimeSummary.totalTasks\"");
        assertThat(template).contains("<table class=\"layui-table\" lay-skin=\"line\">");
        assertThat(template).contains("id=\"task-group-list\"");
        assertThat(template).doesNotContain("paginate(");
        assertThat(template).doesNotContain("toastr");
        assertThat(template).contains("appNotify(");
    }

    @Test
    void monitorPageSpecificStylesLoadBeforeBodyToAvoidUnstyledFirstPaint() throws Exception
    {
        String template = read("/templates/task-group-monitor.ftlh");

        assertThat(template.indexOf("<style>")).isLessThan(template.indexOf("</head>"));
        assertThat(template.indexOf("<style>")).isLessThan(template.indexOf("<body>"));
        assertThat(template.indexOf("</body>")).isLessThan(template.indexOf("<script type=\"text/javascript\">"));
    }

    @Test
    void monitorShowsGlobalAndTaskGroupRuntimeMetricsWithoutTripTaskRows() throws Exception
    {
        String template = read("/templates/task-group-monitor.ftlh");

        assertThat(template).contains("任务状态");
        assertThat(template).contains("协议运行");
        assertThat(template).contains("资源占用");
        assertThat(template).contains("调度健康");
        assertThat(template).contains("data-field=\"runtimeSummary.terminated\"");
        assertThat(template).contains("data-field=\"runtimeSummary.runtimeResources.heapUsedBytes\"");
        assertThat(template).contains("data-field=\"runtimeSummary.runtimeResources.heapMaxBytes\"");
        assertThat(template).contains("data-field=\"runtimeSummary.runtimeResources.threadCount\"");
        assertThat(template).contains("data-field=\"runtimeSummary.runtimeResources.openFileDescriptorCount\"");
        assertThat(template).contains("data-field=\"runtimeSummary.runtimeResources.maxFileDescriptorCount\"");
        assertThat(template).contains("data-field=\"runtimeSummary.schedulerDelay.averageDelayMillis\"");
        assertThat(template).contains("data-field=\"runtimeSummary.schedulerDelay.maxDelayMillis\"");
        assertThat(template).contains("data-field=\"runtimeSummary.registrationSucceeded\"");
        assertThat(template).contains("data-field=\"runtimeSummary.registrationFailed\"");
        assertThat(template).contains("data-field=\"runtimeSummary.authenticationFailed\"");
        assertThat(template).contains("data-field=\"runtimeSummary.locationReportSent\"");
        assertThat(template).doesNotContain("runtimeSummary.locationReportRate");
        assertThat(template).contains("data-field=\"runtimeSummary.sendFailed\"");
        assertThat(template).contains("data-field=\"runtimeSummary.protocolExceptions\"");
        assertThat(template).contains("detailItem('连接成功', group.connectionSucceeded || 0)");
        assertThat(template).contains("detailItem('注册失败', group.registrationFailed || 0)");
        assertThat(template).contains("detailItem('鉴权失败', group.authenticationFailed || 0)");
        assertThat(template).contains("detailItem('位置上报', group.locationReportSent || 0)");
        assertThat(template).contains("detailItem('上报速率/s', formatRate(group.locationReportRate))");
        assertThat(template).contains("detailItem('发送失败', group.sendFailed || 0)");
        assertThat(template).contains("detailItem('协议异常', group.protocolExceptions || 0)");
        assertThat(template).doesNotContain("taskGroup.tripTasks");
    }

    @Test
    void creationPagesNavigateToTaskGroupMonitorAfterSuccess() throws Exception
    {
        String taskCreate = read("/templates/task-create.ftlh");
        String batchCreate = read("/templates/task-batch-create.ftlh");

        assertThat(taskCreate).contains("window.location.href = '/task-groups/monitor?taskGroupId=' + result.data.taskGroupId");
        assertThat(batchCreate).contains("window.location.href = '/task-groups/monitor?taskGroupId=' + result.data.taskGroupId");
    }

    @Test
    void monitorCanStopTaskGroupAndShowStopResult() throws Exception
    {
        String template = read("/templates/task-group-monitor.ftlh");

        assertThat(template).contains("停止任务组");
        assertThat(template).contains("$.post('/task-groups/stop'");
        assertThat(template).contains("data-action=\"stop\"");
        assertThat(template).contains("data-action=\"view-tasks\"");
        assertThat(template).contains("layui-btn-group task-group-actions");
        assertThat(template).contains("'/monitor/list/index?taskGroupId=' + encodeURIComponent(id)");
        assertThat(template).contains("detailItem('停止成功', group.stopSucceeded || 0)");
        assertThat(template).contains("detailItem('停止失败', group.stopFailed || 0)");
        assertThat(template).contains("return group.state === 'creating' || group.state === 'running'");
    }

    @Test
    void monitorGivesTaskGroupListAnOperationalInformationHierarchy() throws Exception
    {
        String template = read("/templates/task-group-monitor.ftlh");

        assertThat(template).contains("task-group-monitor__section-heading");
        assertThat(template).contains("任务组列表");
        assertThat(template).contains("task-group-row__identity");
        assertThat(template).contains("task-group-row__display-name");
        assertThat(template).contains("task-group-row__number");
        assertThat(template).contains("'<td><span class=\"task-group-row__number\">' + (group.targetTasks || 0) + '</span></td>'");
        assertThat(template).contains("'<td><span class=\"task-group-row__number\">' + (group.startedTasks || 0) + '</span></td>'");
        assertThat(template).contains("'<td><span class=\"task-group-row__number\">' + (group.activeTasks || 0) + '</span></td>'");
        assertThat(template).contains("'<td><span class=\"task-group-row__number\">' + (group.terminatedTasks || 0) + '</span></td>'");
        assertThat(template).contains("layui-badge ' + stateClass(group.state)");
        assertThat(template).contains("if (state === 'running') return 'layui-bg-green'");
        assertThat(template).contains("task-group-detail__section--launch");
        assertThat(template).contains("task-group-detail__section--current");
        assertThat(template).contains("task-group-detail__grid");
        assertThat(template).contains("task-group-detail__item");
        assertThat(template).contains("function detailItem(label, value, wide, muted)");
        assertThat(template).contains("task-group-actions");
        assertThat(template).doesNotContain("task-group-row__metrics");
        assertThat(template).doesNotContain("task-group-row__metric--target");
        assertThat(template).doesNotContain("<td colspan=\"4\">");
    }

    @Test
    void monitorUsesChineseOperationTermsAndKeepsRawIdentifierInDetailsOnly() throws Exception
    {
        String template = read("/templates/task-group-monitor.ftlh");

        assertThat(template).doesNotContain("Task Group List");
        assertThat(template).doesNotContain("Task Group Launch Status");
        assertThat(template).doesNotContain("Task Group Current Status");
        assertThat(template).doesNotContain("task-group-row__meta");
        assertThat(template).doesNotContain("<strong class=\"task-group-row__display-name\">' + escapeHtml(group.displayName) + '</strong><span");
        assertThat(template).contains("detailItem('任务组标识', escapeHtml(group.taskGroupId), true)");
    }

    private String read(String path) throws Exception
    {
        return new String(getClass().getResourceAsStream(path).readAllBytes(), StandardCharsets.UTF_8);
    }
}
