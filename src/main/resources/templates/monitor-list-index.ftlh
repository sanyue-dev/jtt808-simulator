<!DOCTYPE html>
<html lang="en">
<head>
<#include "inc/resource.ftlh">
    <title>行程任务</title>
</head>
<body>
<div class="layui-layout layui-layout-admin app-layout">
<#include "inc/sidebar.ftlh">
    <div class="layui-body app-body">
        <div class="app-page">
            <div class="page-header">
                <h2>行程任务</h2>
            </div>
            <div class="page-body">
                <div class="table-toolbar">
                    <form class="layui-form table-toolbar__filters monitor-toolbar__filters" id="monitor-filter">
                        <input type="hidden" name="taskGroupId" value="" />
                        <div class="layui-input-inline monitor-toolbar__state">
                            <select name="state" id="stateSelect">
                                <option value="">全部状态</option>
                                <option value="空闲">空闲</option>
                                <option value="行驶中">行驶中</option>
                                <option value="停车中">停车中</option>
                                <option value="行驶终止">行驶终止</option>
                            </select>
                        </div>
                        <div class="layui-input-inline monitor-toolbar__keyword">
                            <input class="layui-input" type="text" name="keyword" placeholder="车牌号 / SIM / 终端ID" />
                        </div>
                        <button class="layui-btn layui-btn-sm" type="submit"><i class="layui-icon layui-icon-search"></i>筛选</button>
                        <button class="layui-btn layui-btn-primary layui-btn-sm" type="button" id="btn-clear-filter">清空</button>
                    </form>
                    <div class="table-toolbar__actions">
                        <button class="layui-btn layui-btn-danger layui-btn-sm" type="button" id="btn-terminate-all">停止全部未终止任务</button>
                    </div>
                </div>
                <blockquote class="layui-elem-quote layui-quote-nm monitor-filter-context" id="task-group-filter-context">
                    <span>当前按任务组过滤：</span>
                    <strong id="task-group-filter-value"></strong>
                    <a id="task-group-filter-link" href="/task-groups/monitor" class="layui-font-blue">查看任务组监控</a>
                </blockquote>
                <div class="app-table-panel">
                    <table id="monitor-table" lay-filter="monitor-table"></table>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
<#include "inc/footer.ftlh">
<style>
    .monitor-filter-context { display: none; margin-bottom: 12px; }
    .monitor-toolbar__state { width: 180px; }
    .monitor-toolbar__keyword { width: 260px; }
    .monitor-actions {
        display: inline-flex;
        align-items: center;
        gap: 12px;
        white-space: nowrap;
    }
    .monitor-action-link {
        color: var(--color-primary);
        line-height: 22px;
    }
    .monitor-action-link:hover {
        color: var(--color-primary);
        text-decoration: underline;
    }
    .monitor-action-danger,
    .monitor-action-danger:hover {
        color: var(--app-danger);
    }
</style>
<script type="text/javascript">
    layui.use('form', function() { layui.form.render(); });
    $(document).ready(function()
    {
        setCurrentMenu('list-monitor');
        var initialTaskGroupId = new URLSearchParams(window.location.search).get('taskGroupId') || '';
        $('#monitor-filter input[name="taskGroupId"]').val(initialTaskGroupId);
        if (initialTaskGroupId)
        {
            $('#task-group-filter-value').text(initialTaskGroupId);
            $('#task-group-filter-link').attr('href', '/task-groups/monitor?taskGroupId=' + encodeURIComponent(initialTaskGroupId));
            $('#task-group-filter-context').show();
        }

        function filterParams()
        {
            var values = {};
            $('#monitor-filter').serializeArray().forEach(function(item)
            {
                values[item.name] = item.value;
            });
            return values;
        }

        function reloadTable(resetPage)
        {
            var options = { where: filterParams() };
            if (resetPage) options.page = { curr: 1 };
            layui.table.reload('monitor-table', options);
        }

        layui.use('table', function()
        {
            var table = layui.table;
            window.monitorTable = table.render($.extend(true, request.table('/monitor/list/json'), {
                elem: '#monitor-table',
                page: { layout: ['prev', 'page', 'next', 'count', 'limit'] },
                limit: 20,
                where: filterParams(),
                cols: [[
                    { field: 'state', title: '状态', align: 'center' },
                    {
                        field: 'taskGroupDisplayName',
                        title: '任务组',
                        align: 'left',
                        templet: function(r) { return r.taskGroupDisplayName ? escapeHtml(r.taskGroupDisplayName) : '--'; }
                    },
                    {
                        field: 'routeName',
                        title: '线路名称',
                        align: 'left',
                        templet: function(r) { return escapeHtml(r.routeName); }
                    },
                    {
                        field: 'routeMileages',
                        title: '线路里程',
                        align: 'right',
                        templet: function(r) { return parseInt(r.routeMileages / 1000) + ' km'; }
                    },
                    { field: 'vehicleNumber', title: '车牌号', align: 'center' },
                    { field: 'simNumber', title: 'SIM卡号', align: 'center' },
                    { field: 'deviceSn', title: '终端ID', align: 'center' },
                    {
                        field: 'failureStage',
                        title: '失败阶段',
                        align: 'center',
                        templet: function(r) { return r.failureStage ? escapeHtml(r.failureStage) : '--'; }
                    },
                    {
                        field: 'failureReason',
                        title: '失败原因',
                        align: 'left',
                        templet: function(r) { return r.failureReason ? escapeHtml(r.failureReason) : '--'; }
                    },
                    {
                        field: 'startTime',
                        title: '启动时间',
                        align: 'center',
                        templet: function(r) { return new Date(r.startTime).format('yyyy-MM-dd hh:mm:ss'); }
                    },
                    {
                        field: 'longitude',
                        title: '经度',
                        align: 'right',
                        templet: function(r) { return r.longitude ? String(r.longitude).replace(/^(\d+\.\d{6})\d+$/gi, '$1') : '--'; }
                    },
                    {
                        field: 'latitude',
                        title: '纬度',
                        align: 'right',
                        templet: function(r) { return r.latitude ? String(r.latitude).replace(/^(\d+\.\d{6})\d+$/gi, '$1') : '--'; }
                    },
                    {
                        title: '操作',
                        align: 'left',
                        width: 120,
                        templet: function(r)
                        {
                            var html = '<span class="monitor-actions">';
                            html += '<a href="/monitor/view?id=' + r.id + '" target="_blank" class="monitor-action-link">详情</a>';
                            if (r.state !== '行驶终止') html += '<a href="javascript:;" lay-event="terminate" class="monitor-action-link monitor-action-danger">停止</a>';
                            html += '</span>';
                            return html;
                        }
                    }
                ]]
            }));

            table.on('tool(monitor-table)', function(obj)
            {
                if (obj.event === 'terminate') terminateTask(obj.data.id);
            });
        });

        $('#monitor-filter').submit(function()
        {
            reloadTable(true);
            return false;
        });

        $('#btn-clear-filter').click(function()
        {
            $('#monitor-filter').get(0).reset();
            $('#monitor-filter input[name="taskGroupId"]').val('');
            $('#task-group-filter-context').hide();
            layui.form.render();
            reloadTable(true);
        });

        function terminateTask(id)
        {
            appConfirm('确定停止这个行程任务？', function()
            {
                request.post('/monitor/list/terminate', { id: id }).done(function()
                {
                    appNotify('success', '已提交停止任务');
                    reloadTable();
                });
            });
        }

        $('#btn-terminate-all').click(function()
        {
            appConfirm('确定停止全部未终止的行程任务？', function()
            {
                request.post('/monitor/list/terminate-all', {}).done(function(data)
                {
                    data = data || {};
                    var message = '停止提交完成：成功 ' + (data.succeeded || 0) + ' 个，失败 ' + (data.failed || 0) + ' 个';
                    appNotify(data.failed ? 'error' : 'success', message);
                    reloadTable();
                });
            });
        });
    });
</script>
</html>
