<!DOCTYPE html>
<html lang="en">
<head>
    <#include "inc/resource.ftlh">
    <title>任务组监控</title>
</head>
<body>
<div class="layout">
    <#include "inc/sidebar.ftlh">
    <div class="layout__content">
        <div class="layout__page">
            <div class="page-header">
                <h2>任务组监控</h2>
            </div>
            <div class="page-body">
                <div class="monitor-summary" id="task-group-runtime-summary">
                    <section class="monitor-summary__section">
                        <h3>任务状态</h3>
                        <div class="monitor-summary__grid">
                            <div class="monitor-summary__item"><span>总任务数</span><strong data-field="runtimeSummary.totalTasks">0</strong></div>
                            <div class="monitor-summary__item"><span>未终止</span><strong data-field="runtimeSummary.activeTasks">0</strong></div>
                            <div class="monitor-summary__item"><span>停车中</span><strong data-field="runtimeSummary.parkingTasks">0</strong></div>
                            <div class="monitor-summary__item"><span>已终止</span><strong data-field="runtimeSummary.terminatedTasks">0</strong></div>
                        </div>
                    </section>
                    <section class="monitor-summary__section">
                        <h3>协议运行</h3>
                        <div class="monitor-summary__grid">
                            <div class="monitor-summary__item"><span>连接成功</span><strong data-field="runtimeSummary.connectionSucceeded">0</strong></div>
                            <div class="monitor-summary__item"><span>连接失败</span><strong data-field="runtimeSummary.connectionFailed">0</strong></div>
                            <div class="monitor-summary__item"><span>注册成功</span><strong data-field="runtimeSummary.registrationSucceeded">0</strong></div>
                            <div class="monitor-summary__item"><span>注册失败</span><strong data-field="runtimeSummary.registrationFailed">0</strong></div>
                            <div class="monitor-summary__item"><span>鉴权成功</span><strong data-field="runtimeSummary.authenticationSucceeded">0</strong></div>
                            <div class="monitor-summary__item"><span>鉴权失败</span><strong data-field="runtimeSummary.authenticationFailed">0</strong></div>
                            <div class="monitor-summary__item"><span>位置上报</span><strong data-field="runtimeSummary.locationReportSent">0</strong></div>
                            <div class="monitor-summary__item"><span>位置上报/s</span><strong data-field="runtimeSummary.locationReportRate" data-format="rate">0</strong></div>
                            <div class="monitor-summary__item"><span>断连</span><strong data-field="runtimeSummary.disconnected">0</strong></div>
                            <div class="monitor-summary__item"><span>终止事件</span><strong data-field="runtimeSummary.terminated">0</strong></div>
                            <div class="monitor-summary__item"><span>发送失败</span><strong data-field="runtimeSummary.sendFailed">0</strong></div>
                            <div class="monitor-summary__item"><span>协议异常</span><strong data-field="runtimeSummary.protocolExceptions">0</strong></div>
                        </div>
                    </section>
                    <section class="monitor-summary__section">
                        <h3>资源占用</h3>
                        <div class="monitor-summary__grid">
                            <div class="monitor-summary__item"><span>Heap 使用</span><strong data-field="runtimeSummary.runtimeResources.heapUsedBytes" data-format="bytes">0</strong></div>
                            <div class="monitor-summary__item"><span>Heap 上限</span><strong data-field="runtimeSummary.runtimeResources.heapMaxBytes" data-format="bytes">0</strong></div>
                            <div class="monitor-summary__item"><span>线程数</span><strong data-field="runtimeSummary.runtimeResources.threadCount">0</strong></div>
                            <div class="monitor-summary__item"><span>FD 使用</span><strong data-field="runtimeSummary.runtimeResources.openFileDescriptorCount">0</strong></div>
                            <div class="monitor-summary__item"><span>FD 上限</span><strong data-field="runtimeSummary.runtimeResources.maxFileDescriptorCount">0</strong></div>
                        </div>
                    </section>
                    <section class="monitor-summary__section">
                        <h3>调度健康</h3>
                        <div class="monitor-summary__grid">
                            <div class="monitor-summary__item"><span>调度延迟均值</span><strong data-field="runtimeSummary.schedulerDelay.averageDelayMillis" data-format="millis">0</strong></div>
                            <div class="monitor-summary__item"><span>调度延迟最大</span><strong data-field="runtimeSummary.schedulerDelay.maxDelayMillis" data-format="millis">0</strong></div>
                        </div>
                    </section>
                </div>

                <div class="task-group-monitor__section-heading">
                    <div>
                        <h3>任务组列表</h3>
                        <p>Task Group List</p>
                    </div>
                </div>

                <div class="task-group-table">
                    <table class="data-table">
                        <thead>
                        <tr>
                            <th>状态</th>
                            <th>任务组</th>
                            <th>来源</th>
                            <th>目标数</th>
                            <th>已启动</th>
                            <th>活动中</th>
                            <th>已终止</th>
                            <th>操作</th>
                        </tr>
                        </thead>
                        <tbody id="task-group-list"></tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
<#include "inc/footer.ftlh">
<style>
    .monitor-summary {
        display: flex;
        flex-direction: column;
        gap: 14px;
        margin-bottom: 16px;
    }
    .monitor-summary__section h3 {
        margin: 0 0 8px;
        font-size: 14px;
        color: var(--color-text-primary);
        font-weight: 600;
    }
    .monitor-summary__grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(130px, 1fr));
        gap: 10px;
    }
    .monitor-summary__item {
        border: 1px solid var(--color-border);
        border-radius: var(--radius-sm);
        background: var(--color-bg-panel);
        padding: 10px 12px;
    }
    .monitor-summary__item span {
        display: block;
        color: var(--color-text-secondary);
        font-size: 12px;
        margin-bottom: 6px;
    }
    .monitor-summary__item strong {
        color: var(--color-text-primary);
        font-size: 18px;
    }
    .task-group-table table {
        width: 100%;
        border-collapse: collapse;
        background: var(--color-bg-panel);
        border: 1px solid var(--color-border);
    }
    .task-group-table th,
    .task-group-table td {
        padding: 10px 12px;
        border-bottom: 1px solid var(--color-border);
        text-align: left;
        font-size: 13px;
    }
    .task-group-table th { color: var(--color-text-secondary); font-weight: 500; }
    .task-group-monitor__section-heading {
        display: flex;
        align-items: flex-end;
        justify-content: space-between;
        margin: 18px 0 8px;
        border-bottom: 1px solid var(--color-border);
        padding-bottom: 8px;
    }
    .task-group-monitor__section-heading h3 {
        margin: 0;
        color: var(--color-text-primary);
        font-size: 15px;
        font-weight: 600;
    }
    .task-group-monitor__section-heading p {
        margin: 3px 0 0;
        color: var(--color-text-secondary);
        font-size: 12px;
    }
    .task-group-row__primary td {
        vertical-align: top;
    }
    .task-group-row__identity {
        display: flex;
        flex-direction: column;
        gap: 4px;
        min-width: 180px;
    }
    .task-group-row__display-name {
        color: var(--color-text-primary);
        font-weight: 600;
        font-size: 13px;
    }
    .task-group-row__meta {
        color: var(--color-text-secondary);
        font-size: 12px;
    }
    .task-group-row__metrics {
        display: grid;
        grid-template-columns: repeat(4, minmax(72px, 1fr));
        gap: 8px;
        min-width: 360px;
    }
    .task-group-row__metric {
        display: flex;
        flex-direction: column;
        gap: 3px;
        border-left: 2px solid var(--color-border);
        padding-left: 8px;
    }
    .task-group-row__metric span {
        color: var(--color-text-secondary);
        font-size: 11px;
    }
    .task-group-row__metric strong {
        color: var(--color-text-primary);
        font-size: 15px;
        font-weight: 600;
    }
    .task-group-state {
        display: inline-flex;
        align-items: center;
        min-width: 64px;
        height: 24px;
        padding: 0 8px;
        border-left: 3px solid var(--color-border-strong);
        background: var(--color-bg-panel-muted);
        color: var(--color-text-primary);
        font-size: 12px;
        font-weight: 600;
    }
    .task-group-state--running { border-left-color: var(--color-success); }
    .task-group-state--creating,
    .task-group-state--stopping { border-left-color: var(--color-primary); }
    .task-group-state--completed { border-left-color: var(--color-border-strong); color: var(--color-text-secondary); }
    .task-group-state--failed { border-left-color: var(--color-danger); }
    .task-group-table__details td { background: var(--color-bg-panel-muted); }
    .task-group-detail {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
        gap: 12px;
    }
    .task-group-detail__section {
        border: 1px solid var(--color-border);
        border-radius: var(--radius-sm);
        padding: 10px;
        background: var(--color-bg-panel);
    }
    .task-group-detail__section h4 {
        margin: 0 0 8px;
        font-size: 13px;
        color: var(--color-text-primary);
    }
    .task-group-detail__section h4 span {
        display: block;
        margin-top: 2px;
        color: var(--color-text-secondary);
        font-size: 11px;
        font-weight: 400;
    }
    .task-group-detail__section p {
        margin: 4px 0;
        font-size: 12px;
        color: var(--color-text-secondary);
    }
    .task-group-actions {
        display: flex;
        gap: 6px;
        flex-wrap: wrap;
        min-width: 208px;
    }
    .task-group-table__btn {
        border: 1px solid var(--color-border);
        background: var(--color-bg-panel-muted);
        border-radius: var(--radius-sm);
        height: 28px;
        padding: 0 10px;
        cursor: pointer;
        color: var(--color-text-primary);
    }
    .task-group-table__btn i { margin-right: 4px; }
    .task-group-table__btn:hover {
        border-color: var(--color-border-strong);
        background: var(--color-bg-muted);
    }
    .task-group-table__btn--danger {
        border-color: var(--color-danger);
        color: var(--color-danger);
    }
    .task-group-table__btn--danger:hover {
        background: var(--color-danger);
        border-color: var(--color-danger);
        color: var(--color-text-inverse);
    }
</style>
<script type="text/javascript">
    $(document).ready(function()
    {
        setCurrentMenu('task-group-monitor');

        var expandedTaskGroupIds = {};
        var focusedTaskGroupId = new URLSearchParams(window.location.search).get('taskGroupId');
        if (focusedTaskGroupId) expandedTaskGroupIds[focusedTaskGroupId] = true;

        function loadSnapshot()
        {
            $.post('/task-groups/snapshot', function(result)
            {
                if (result.error && result.error.code) return toastr('error', result.error.reason);
                renderSnapshot(result.data || {});
            });
        }

        function renderSnapshot(data)
        {
            $('#task-group-runtime-summary [data-field]').each(function()
            {
                var field = $(this).attr('data-field');
                var value = getValue(data, field);
                if ($(this).attr('data-format') === 'rate') value = Number(value || 0).toFixed(2);
                if ($(this).attr('data-format') === 'bytes') value = formatBytes(value);
                if ($(this).attr('data-format') === 'millis') value = Number(value || 0).toFixed(2) + ' ms';
                $(this).text(value == null ? 0 : value);
            });

            var rows = [];
            (data.taskGroups || []).forEach(function(group)
            {
                var expanded = !!expandedTaskGroupIds[group.taskGroupId];
                var actions = '<div class="task-group-actions">';
                actions += '<button type="button" class="task-group-table__btn" data-action="toggle"><i class="fa ' + (expanded ? 'fa-chevron-up' : 'fa-chevron-down') + '"></i>' + (expanded ? '收起' : '展开') + '</button>';
                actions += '<button type="button" class="task-group-table__btn" data-action="view-tasks"><i class="fa fa-list-ul"></i>查看行程任务</button>';
                if (canStop(group))
                {
                    actions += '<button type="button" class="task-group-table__btn task-group-table__btn--danger" data-action="stop"><i class="fa fa-stop-circle"></i>停止任务组</button>';
                }
                actions += '</div>';
                rows.push('<tr class="task-group-row__primary" data-task-group-id="' + escapeHtml(group.taskGroupId) + '">' +
                    '<td><span class="task-group-state task-group-state--' + stateClass(group.state) + '">' + formatState(group.state) + '</span></td>' +
                    '<td><div class="task-group-row__identity"><strong class="task-group-row__display-name">' + escapeHtml(group.displayName) + '</strong><span class="task-group-row__meta">' + escapeHtml(group.taskGroupId) + '</span></div></td>' +
                    '<td>' + escapeHtml(group.sourceText || group.source) + '</td>' +
                    '<td colspan="4"><div class="task-group-row__metrics">' +
                    '<div class="task-group-row__metric task-group-row__metric--target"><span>目标数</span><strong>' + (group.targetTasks || 0) + '</strong></div>' +
                    '<div class="task-group-row__metric"><span>已启动</span><strong>' + (group.startedTasks || 0) + '</strong></div>' +
                    '<div class="task-group-row__metric"><span>活动中</span><strong>' + (group.activeTasks || 0) + '</strong></div>' +
                    '<div class="task-group-row__metric"><span>已终止</span><strong>' + (group.terminatedTasks || 0) + '</strong></div>' +
                    '</div></td>' +
                    '<td>' + actions + '</td>' +
                    '</tr>');
                if (expanded) rows.push(detailRow(group));
            });
            $('#task-group-list').html(rows.join(''));
        }

        function detailRow(group)
        {
            return '<tr class="task-group-table__details"><td colspan="8">' +
                '<div class="task-group-detail">' +
                '<div class="task-group-detail__section task-group-detail__section--launch"><h4>启动情况<span>Task Group Launch Status</span></h4>' +
                '<p>目标任务数：' + (group.targetTasks || 0) + '</p>' +
                '<p>已启动任务：' + (group.startedTasks || 0) + '</p>' +
                '<p>ramp-up：' + (group.executedWindowCount || 0) + ' / ' + (group.rampUpWindowCount || 0) + '</p>' +
                '<p>失败原因：' + escapeHtml(group.failureReason || '无') + '</p>' +
                '</div>' +
                '<div class="task-group-detail__section task-group-detail__section--current"><h4>当前运行情况<span>Task Group Current Status</span></h4>' +
                '<p>状态：' + formatState(group.state) + '</p>' +
                '<p>活动中：' + (group.activeTasks || 0) + '</p>' +
                '<p>已终止：' + (group.terminatedTasks || 0) + '</p>' +
                '<p>连接成功：' + (group.connectionSucceeded || 0) + '</p>' +
                '<p>连接失败：' + (group.connectionFailed || 0) + '</p>' +
                '<p>注册成功：' + (group.registrationSucceeded || 0) + '</p>' +
                '<p>注册失败：' + (group.registrationFailed || 0) + '</p>' +
                '<p>鉴权成功：' + (group.authenticationSucceeded || 0) + '</p>' +
                '<p>鉴权失败：' + (group.authenticationFailed || 0) + '</p>' +
                '<p>位置上报：' + (group.locationReportSent || 0) + '</p>' +
                '<p>上报速率/s：' + formatRate(group.locationReportRate) + '</p>' +
                '<p>断连：' + (group.disconnected || 0) + '</p>' +
                '<p>发送失败：' + (group.sendFailed || 0) + '</p>' +
                '<p>协议异常：' + (group.protocolExceptions || 0) + '</p>' +
                '<p>停止成功：' + (group.stopSucceeded || 0) + '</p>' +
                '<p>停止失败：' + (group.stopFailed || 0) + '</p>' +
                '<p>停止失败明细：' + formatStopFailures(group.stopFailures || []) + '</p>' +
                '</div>' +
                '</div>' +
                '</td></tr>';
        }

        $('#task-group-list').on('click', '[data-action="toggle"]', function()
        {
            var id = $(this).closest('tr').attr('data-task-group-id');
            expandedTaskGroupIds[id] = !expandedTaskGroupIds[id];
            loadSnapshot();
        });

        $('#task-group-list').on('click', '[data-action="stop"]', function()
        {
            var id = $(this).closest('tr').attr('data-task-group-id');
            $.post('/task-groups/stop', { taskGroupId: id }, function(result)
            {
                if (result.error && result.error.code) return toastr('error', result.error.reason);
                loadSnapshot();
            });
        });

        $('#task-group-list').on('click', '[data-action="view-tasks"]', function()
        {
            var id = $(this).closest('tr').attr('data-task-group-id');
            window.location.href = '/monitor/list/index?taskGroupId=' + encodeURIComponent(id);
        });

        function canStop(group)
        {
            return group.state === 'creating' || group.state === 'running';
        }

        function formatStopFailures(failures)
        {
            if (!failures.length) return '无';
            return failures.map(function(failure)
            {
                return escapeHtml(failure.taskId) + '：' + escapeHtml(failure.reason || '');
            }).join('；');
        }

        function formatRate(value)
        {
            return Number(value || 0).toFixed(2);
        }

        function formatBytes(value)
        {
            var n = Number(value || 0);
            if (n < 0) return 'N/A';
            if (n < 1024) return n + ' B';
            if (n < 1024 * 1024) return (n / 1024).toFixed(1) + ' KB';
            if (n < 1024 * 1024 * 1024) return (n / 1024 / 1024).toFixed(1) + ' MB';
            return (n / 1024 / 1024 / 1024).toFixed(1) + ' GB';
        }

        function getValue(data, path)
        {
            var node = data;
            var parts = path.split('.');
            for (var i = 0; i < parts.length; i++)
            {
                if (node == null) return 0;
                node = node[parts[i]];
            }
            return node == null ? 0 : node;
        }

        function formatState(state)
        {
            if (state === 'creating') return '创建中';
            if (state === 'running') return '运行中';
            if (state === 'stopping') return '停止中';
            if (state === 'completed') return '已完成';
            if (state === 'failed') return '失败';
            return '-';
        }

        function stateClass(state)
        {
            if (state === 'creating') return 'creating';
            if (state === 'running') return 'running';
            if (state === 'stopping') return 'stopping';
            if (state === 'completed') return 'completed';
            if (state === 'failed') return 'failed';
            return 'unknown';
        }

        function escapeHtml(value)
        {
            return $('<div>').text(value == null ? '' : String(value)).html();
        }

        loadSnapshot();
        setInterval(loadSnapshot, 1000);
    });
</script>
</html>
