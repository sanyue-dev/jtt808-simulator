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
                <form class="layui-form toolbar" id="monitor-filter">
                    <input type="hidden" name="taskGroupId" value="" />
                    <div class="layui-input-inline">
                        <select name="state" id="stateSelect">
                            <option value="">全部状态</option>
                            <option value="空闲">空闲</option>
                            <option value="行驶中">行驶中</option>
                            <option value="停车中">停车中</option>
                            <option value="行驶终止">行驶终止</option>
                        </select>
                    </div>
                    <div class="layui-input-inline">
                        <input class="layui-input" type="text" name="keyword" placeholder="车牌号 / SIM / 终端ID" />
                    </div>
                    <button class="layui-btn layui-btn-sm" type="submit"><i class="layui-icon layui-icon-search"></i>筛选</button>
                    <button class="layui-btn layui-btn-primary layui-btn-sm" type="button" id="btn-clear-filter">清空</button>
                    <button class="layui-btn layui-btn-danger layui-btn-sm" type="button" id="btn-terminate-all">停止全部未终止任务</button>
                </form>
                <blockquote class="layui-elem-quote layui-quote-nm monitor-filter-context" id="task-group-filter-context">
                    <span>当前按任务组过滤：</span>
                    <strong id="task-group-filter-value"></strong>
                    <a id="task-group-filter-link" href="/task-groups/monitor" class="layui-font-blue">查看任务组监控</a>
                </blockquote>
                <div class="app-table-panel">
                    <div id="route-table"></div>
                    <div id="monitor-page" class="app-pagination"></div>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
<#include "inc/footer.ftlh">
<style>
    .monitor-filter-context { display: none; margin-bottom: 12px; }
    .toolbar .layui-input-inline { width: 180px; margin-right: 0; }
    .toolbar .layui-input-inline:nth-child(3) { width: 260px; }
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

        function reloadTable()
        {
            window.monitorTable.reload();
        }

        window.monitorTable = appTable.render({
            elem: '#route-table',
            pageElem: '#monitor-page',
            url: '/monitor/list/json',
            form: $('#monitor-filter'),
            fields: [
                { name: 'state', title: '状态', align: 'center' },
                {
                    name: 'taskGroupDisplayName',
                    title: '任务组',
                    align: 'left',
                    formatter: function(i, v) { return v ? escapeHtml(v) : '--'; }
                },
                { name: 'routeName', title: '线路名称', align: 'left' },
                {
                    name : 'routeMileages',
                    title : '线路里程',
                    align : 'right',
                    formatter : function(i, v) { return parseInt(v / 1000) + ' km'; }
                },
                { name: 'vehicleNumber', title: '车牌号', align: 'center' },
                { name: 'simNumber', title: 'SIM卡号', align: 'center' },
                { name: 'deviceSn', title: '终端ID', align: 'center' },
                {
                    name: 'failureStage',
                    title: '失败阶段',
                    align: 'center',
                    formatter: function(i, v) { return v ? escapeHtml(v) : '--'; }
                },
                {
                    name: 'failureReason',
                    title: '失败原因',
                    align: 'left',
                    formatter: function(i, v) { return v ? escapeHtml(v) : '--'; }
                },
                {
                    name : 'startTime',
                    title : '启动时间',
                    align : 'center',
                    formatter : function(i, v) { return new Date(v).format('yyyy-MM-dd hh:mm:ss'); }
                },
                {
                    name : 'longitude',
                    title : '经度',
                    align : 'right',
                    formatter : function(i, v) { return v ? String(v).replace(/^(\d+\.\d{6})\d+$/gi, '$1') : '--'; }
                },
                {
                    name : 'latitude',
                    title : '纬度',
                    align : 'right',
                    formatter : function(i, v) { return v ? String(v).replace(/^(\d+\.\d{6})\d+$/gi, '$1') : '--'; }
                },
                {
                    name: 'id',
                    title: '操作',
                    align: 'center',
                    width : '170px',
                    formatter: function (i, v, r)
                    {
                        var html = '';
                        html += '<a href="/monitor/view?id=' + v + '" target="_blank" class="layui-btn layui-btn-xs">详情</a>';
                        if (r.state !== '行驶终止') html += ' <a href="javascript:;" data-id="' + v + '" class="layui-btn layui-btn-xs layui-btn-primary btn-terminate">停止</a>';
                        return html;
                    }
                }
            ]
        });

        $('#btn-clear-filter').click(function()
        {
            $('#monitor-filter').get(0).reset();
            $('#monitor-filter input[name="taskGroupId"]').val('');
            $('#task-group-filter-context').hide();
            layui.form.render();
            reloadTable();
        });

        $('#route-table').on('click', '.btn-terminate', function()
        {
            var id = $(this).attr('data-id');
            appConfirm('确定停止这个行程任务？', function()
            {
                $.post('/monitor/list/terminate', { id: id }, function(result)
                {
                    if (result.error && result.error.code) return appNotify('error', result.error.reason);
                    appNotify('success', '已提交停止任务');
                    reloadTable();
                });
            });
        });

        $('#btn-terminate-all').click(function()
        {
            appConfirm('确定停止全部未终止的行程任务？', function()
            {
                $.post('/monitor/list/terminate-all', {}, function(result)
                {
                    if (result.error && result.error.code) return appNotify('error', result.error.reason);
                    var data = result.data || {};
                    var message = '停止提交完成：成功 ' + (data.succeeded || 0) + ' 个，失败 ' + (data.failed || 0) + ' 个';
                    appNotify(data.failed ? 'error' : 'success', message);
                    reloadTable();
                });
            });
        });
    });
</script>
</html>
