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
                    <div class="monitor-summary__item"><span>总任务数</span><strong data-field="runtimeSummary.totalTasks">0</strong></div>
                    <div class="monitor-summary__item"><span>未终止</span><strong data-field="runtimeSummary.activeTasks">0</strong></div>
                    <div class="monitor-summary__item"><span>停车中</span><strong data-field="runtimeSummary.parkingTasks">0</strong></div>
                    <div class="monitor-summary__item"><span>已终止</span><strong data-field="runtimeSummary.terminatedTasks">0</strong></div>
                    <div class="monitor-summary__item"><span>连接成功</span><strong data-field="runtimeSummary.connectionSucceeded">0</strong></div>
                    <div class="monitor-summary__item"><span>连接失败</span><strong data-field="runtimeSummary.connectionFailed">0</strong></div>
                    <div class="monitor-summary__item"><span>注册成功</span><strong data-field="runtimeSummary.registrationSucceeded">0</strong></div>
                    <div class="monitor-summary__item"><span>注册失败</span><strong data-field="runtimeSummary.registrationFailed">0</strong></div>
                    <div class="monitor-summary__item"><span>鉴权成功</span><strong data-field="runtimeSummary.authenticationSucceeded">0</strong></div>
                    <div class="monitor-summary__item"><span>鉴权失败</span><strong data-field="runtimeSummary.authenticationFailed">0</strong></div>
                    <div class="monitor-summary__item"><span>位置上报</span><strong data-field="runtimeSummary.locationReportSent">0</strong></div>
                    <div class="monitor-summary__item"><span>位置上报/s</span><strong data-field="runtimeSummary.locationReportRate" data-format="rate">0</strong></div>
                    <div class="monitor-summary__item"><span>断连</span><strong data-field="runtimeSummary.disconnected">0</strong></div>
                    <div class="monitor-summary__item"><span>发送失败</span><strong data-field="runtimeSummary.sendFailed">0</strong></div>
                    <div class="monitor-summary__item"><span>协议异常</span><strong data-field="runtimeSummary.protocolExceptions">0</strong></div>
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
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(130px, 1fr));
        gap: 10px;
        margin-bottom: 16px;
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
    .task-group-detail__section p {
        margin: 4px 0;
        font-size: 12px;
        color: var(--color-text-secondary);
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
    .task-group-table__btn--danger {
        border-color: #dc3545;
        color: #dc3545;
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
                $(this).text(value == null ? 0 : value);
            });

            var rows = [];
            (data.taskGroups || []).forEach(function(group)
            {
                var expanded = !!expandedTaskGroupIds[group.taskGroupId];
                var actions = '<button type="button" class="task-group-table__btn" data-action="toggle">' + (expanded ? '收起' : '展开') + '</button>';
                actions += ' <button type="button" class="task-group-table__btn" data-action="view-tasks">查看行程任务</button>';
                if (canStop(group))
                {
                    actions += ' <button type="button" class="task-group-table__btn task-group-table__btn--danger" data-action="stop">停止任务组</button>';
                }
                rows.push('<tr data-task-group-id="' + escapeHtml(group.taskGroupId) + '">' +
                    '<td>' + formatState(group.state) + '</td>' +
                    '<td>' + escapeHtml(group.displayName) + '</td>' +
                    '<td>' + escapeHtml(group.sourceText || group.source) + '</td>' +
                    '<td>' + (group.targetTasks || 0) + '</td>' +
                    '<td>' + (group.startedTasks || 0) + '</td>' +
                    '<td>' + (group.activeTasks || 0) + '</td>' +
                    '<td>' + (group.terminatedTasks || 0) + '</td>' +
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
                '<div class="task-group-detail__section"><h4>启动情况</h4>' +
                '<p>目标任务数：' + (group.targetTasks || 0) + '</p>' +
                '<p>已启动任务：' + (group.startedTasks || 0) + '</p>' +
                '<p>ramp-up：' + (group.executedWindowCount || 0) + ' / ' + (group.rampUpWindowCount || 0) + '</p>' +
                '<p>失败原因：' + escapeHtml(group.failureReason || '无') + '</p>' +
                '</div>' +
                '<div class="task-group-detail__section"><h4>当前运行情况</h4>' +
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

        function escapeHtml(value)
        {
            return $('<div>').text(value == null ? '' : String(value)).html();
        }

        loadSnapshot();
        setInterval(loadSnapshot, 1000);
    });
</script>
</html>
