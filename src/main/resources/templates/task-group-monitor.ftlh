<!DOCTYPE html>
<html lang="en">
<head>
    <#include "inc/resource.ftlh">
    <title>任务组监控</title>
    <style>
        .monitor-summary {
            display: grid;
            grid-template-columns: minmax(180px, .85fr) minmax(360px, 1.75fr) minmax(260px, 1.25fr) minmax(180px, .85fr);
            gap: 12px;
            margin-bottom: 12px;
        }
        .monitor-summary__section {
            min-width: 0;
            border: 1px solid var(--color-border);
            border-radius: var(--radius-sm);
            background: var(--color-bg-panel);
            padding: 10px 12px;
        }
        .monitor-summary__section h3 {
            margin: 0 0 10px;
            font-size: 13px;
            color: var(--color-text-primary);
            font-weight: 600;
            line-height: 20px;
        }
        .monitor-summary__grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(96px, 1fr));
            gap: 6px 10px;
        }
        .monitor-summary__section--wide .monitor-summary__grid {
            grid-template-columns: repeat(auto-fit, minmax(96px, 1fr));
        }
        .monitor-summary__item {
            display: flex;
            align-items: baseline;
            justify-content: space-between;
            min-width: 0;
            gap: 8px;
            border-bottom: 1px solid var(--color-border);
            padding: 4px 0 6px;
            line-height: 20px;
        }
        .monitor-summary__item span {
            min-width: 0;
            color: var(--color-text-secondary);
            font-size: 12px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }
        .monitor-summary__item strong {
            flex: 0 0 auto;
            color: var(--color-text-primary);
            font-size: 14px;
            font-weight: 600;
            white-space: nowrap;
        }
        @media screen and (max-width: 1280px) {
            .monitor-summary {
                grid-template-columns: repeat(2, minmax(0, 1fr));
            }
        }
        @media screen and (max-width: 760px) {
            .monitor-summary {
                grid-template-columns: 1fr;
            }
        }
        .task-group-table {
            overflow-x: auto;
        }
        .task-group-table .layui-table {
            min-width: 960px;
            margin: 0;
        }
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
            vertical-align: middle;
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
        .task-group-row__number {
            color: var(--color-text-primary);
            font-size: 14px;
            font-weight: 600;
        }
        .task-group-table__details td { background: var(--color-bg-panel-muted); }
        .task-group-detail {
            display: grid;
            grid-template-columns: minmax(260px, .72fr) minmax(420px, 1.28fr);
            gap: 12px;
        }
        .task-group-detail__section {
            border: 1px solid var(--color-border);
            border-radius: var(--radius-sm);
            padding: 12px;
            background: var(--color-bg-panel);
        }
        .task-group-detail__section h4 {
            display: flex;
            align-items: center;
            gap: 6px;
            margin: 0 0 10px;
            font-size: 14px;
            color: var(--color-text-primary);
        }
        .task-group-detail__section h4 .layui-icon {
            color: var(--color-primary);
            font-size: 15px;
        }
        .task-group-detail__section h4 span {
            display: block;
            margin-top: 2px;
            color: var(--color-text-secondary);
            font-size: 11px;
            font-weight: 400;
        }
        .task-group-detail__grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(128px, 1fr));
            gap: 8px;
            margin: 0;
        }
        .task-group-detail__item {
            min-width: 0;
            margin: 0;
            padding: 8px 10px;
            border: 1px solid var(--color-border);
            border-radius: var(--radius-sm);
            background: var(--color-bg-panel-muted);
        }
        .task-group-detail__item--wide {
            grid-column: 1 / -1;
        }
        .task-group-detail__item dt {
            margin-bottom: 4px;
            color: var(--color-text-secondary);
            font-size: 12px;
            line-height: 18px;
        }
        .task-group-detail__item dd {
            margin: 0;
            color: var(--color-text-primary);
            font-size: 14px;
            font-weight: 600;
            line-height: 20px;
            overflow-wrap: anywhere;
        }
        .task-group-detail__item--muted dd {
            color: var(--color-text-secondary);
            font-size: 13px;
            font-weight: 400;
        }
        @media screen and (max-width: 1100px) {
            .task-group-detail {
                grid-template-columns: 1fr;
            }
        }
        .task-group-actions {
            min-width: 208px;
        }
        .task-group-actions .layui-btn i { margin-right: 3px; }
    </style>
</head>
<body>
<div class="layui-layout layui-layout-admin app-layout">
    <#include "inc/sidebar.ftlh">
    <div class="layui-body app-body">
        <div class="app-page">
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
                    <section class="monitor-summary__section monitor-summary__section--wide">
                        <h3>协议运行</h3>
                        <div class="monitor-summary__grid">
                            <div class="monitor-summary__item"><span>连接成功</span><strong data-field="runtimeSummary.connectionSucceeded">0</strong></div>
                            <div class="monitor-summary__item"><span>连接失败</span><strong data-field="runtimeSummary.connectionFailed">0</strong></div>
                            <div class="monitor-summary__item"><span>注册成功</span><strong data-field="runtimeSummary.registrationSucceeded">0</strong></div>
                            <div class="monitor-summary__item"><span>注册失败</span><strong data-field="runtimeSummary.registrationFailed">0</strong></div>
                            <div class="monitor-summary__item"><span>鉴权成功</span><strong data-field="runtimeSummary.authenticationSucceeded">0</strong></div>
                            <div class="monitor-summary__item"><span>鉴权失败</span><strong data-field="runtimeSummary.authenticationFailed">0</strong></div>
                            <div class="monitor-summary__item"><span>位置上报</span><strong data-field="runtimeSummary.locationReportSent">0</strong></div>
                            <div class="monitor-summary__item"><span>断连</span><strong data-field="runtimeSummary.disconnected">0</strong></div>
                            <div class="monitor-summary__item"><span>终止事件</span><strong data-field="runtimeSummary.terminated">0</strong></div>
                            <div class="monitor-summary__item"><span>发送失败</span><strong data-field="runtimeSummary.sendFailed">0</strong></div>
                            <div class="monitor-summary__item"><span>协议异常</span><strong data-field="runtimeSummary.protocolExceptions">0</strong></div>
                        </div>
                    </section>
                    <section class="monitor-summary__section monitor-summary__section--wide">
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
                    </div>
                </div>

                <div class="task-group-table">
                    <table class="layui-table" lay-skin="line">
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
<script type="text/javascript">
    $(document).ready(function()
    {
        setCurrentMenu('task-group-monitor');

        var expandedTaskGroupIds = {};
        var focusedTaskGroupId = new URLSearchParams(window.location.search).get('taskGroupId');
        if (focusedTaskGroupId) expandedTaskGroupIds[focusedTaskGroupId] = true;

        function loadSnapshot()
        {
            request.post('/task-groups/snapshot', {}, { silentError: true }).done(function(result)
            {
                renderSnapshot(result || {});
            }).fail(function(xhr)
            {
                console.error(xhr.responseJSON || xhr.responseText);
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
                var actions = '<div class="layui-btn-group task-group-actions">';
                actions += '<button type="button" class="layui-btn layui-btn-primary layui-btn-xs" data-action="toggle"><i class="layui-icon ' + (expanded ? 'layui-icon-up' : 'layui-icon-down') + '"></i>' + (expanded ? '收起' : '展开') + '</button>';
                actions += '<button type="button" class="layui-btn layui-btn-primary layui-btn-xs" data-action="view-tasks"><i class="layui-icon layui-icon-list"></i>查看行程任务</button>';
                if (canStop(group))
                {
                    actions += '<button type="button" class="layui-btn layui-btn-danger layui-btn-xs" data-action="stop"><i class="layui-icon layui-icon-pause"></i>停止任务组</button>';
                }
                actions += '</div>';
                rows.push('<tr class="task-group-row__primary" data-task-group-id="' + escapeHtml(group.taskGroupId) + '">' +
                    '<td><span class="layui-badge ' + stateClass(group.state) + '">' + formatState(group.state) + '</span></td>' +
                    '<td><div class="task-group-row__identity"><strong class="task-group-row__display-name">' + escapeHtml(group.displayName) + '</strong></div></td>' +
                    '<td>' + escapeHtml(group.sourceText || group.source) + '</td>' +
                    '<td><span class="task-group-row__number">' + (group.targetTasks || 0) + '</span></td>' +
                    '<td><span class="task-group-row__number">' + (group.startedTasks || 0) + '</span></td>' +
                    '<td><span class="task-group-row__number">' + (group.activeTasks || 0) + '</span></td>' +
                    '<td><span class="task-group-row__number">' + (group.terminatedTasks || 0) + '</span></td>' +
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
                '<div class="task-group-detail__section task-group-detail__section--launch"><h4><i class="layui-icon layui-icon-release"></i>启动情况</h4>' +
                '<dl class="task-group-detail__grid">' +
                detailItem('任务组标识', escapeHtml(group.taskGroupId), true) +
                detailItem('目标任务数', group.targetTasks || 0) +
                detailItem('已启动任务', group.startedTasks || 0) +
                detailItem('ramp-up', (group.executedWindowCount || 0) + ' / ' + (group.rampUpWindowCount || 0)) +
                detailItem('失败原因', escapeHtml(group.failureReason || '无'), true, true) +
                '</dl>' +
                '</div>' +
                '<div class="task-group-detail__section task-group-detail__section--current"><h4><i class="layui-icon layui-icon-chart-screen"></i>当前运行情况</h4>' +
                '<dl class="task-group-detail__grid">' +
                detailItem('状态', formatState(group.state)) +
                detailItem('活动中', group.activeTasks || 0) +
                detailItem('已终止', group.terminatedTasks || 0) +
                detailItem('连接成功', group.connectionSucceeded || 0) +
                detailItem('连接失败', group.connectionFailed || 0) +
                detailItem('注册成功', group.registrationSucceeded || 0) +
                detailItem('注册失败', group.registrationFailed || 0) +
                detailItem('鉴权成功', group.authenticationSucceeded || 0) +
                detailItem('鉴权失败', group.authenticationFailed || 0) +
                detailItem('位置上报', group.locationReportSent || 0) +
                detailItem('上报速率/s', formatRate(group.locationReportRate)) +
                detailItem('断连', group.disconnected || 0) +
                detailItem('发送失败', group.sendFailed || 0) +
                detailItem('协议异常', group.protocolExceptions || 0) +
                detailItem('停止成功', group.stopSucceeded || 0) +
                detailItem('停止失败', group.stopFailed || 0) +
                detailItem('停止失败明细', formatStopFailures(group.stopFailures || []), true, true) +
                '</dl>' +
                '</div>' +
                '</div>' +
                '</td></tr>';
        }

        function detailItem(label, value, wide, muted)
        {
            var className = 'task-group-detail__item';
            if (wide) className += ' task-group-detail__item--wide';
            if (muted) className += ' task-group-detail__item--muted';
            return '<div class="' + className + '"><dt>' + escapeHtml(label) + '</dt><dd>' + value + '</dd></div>';
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
            request.post('/task-groups/stop', { taskGroupId: id }).done(function()
            {
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
            if (state === 'creating') return 'layui-bg-blue';
            if (state === 'running') return 'layui-bg-green';
            if (state === 'stopping') return 'layui-bg-orange';
            if (state === 'completed') return 'layui-bg-gray';
            if (state === 'failed') return 'layui-bg-red';
            return '';
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
