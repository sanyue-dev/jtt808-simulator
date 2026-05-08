<!DOCTYPE html>
<html lang="en">
<head>
<#include "inc/resource.ftlh">
    <title>线路管理</title>
    <style>
        .route-actions {
            display: inline-flex;
            align-items: center;
            gap: 12px;
            white-space: nowrap;
        }
        .route-action-link {
            color: var(--color-primary);
            line-height: 22px;
        }
        .route-action-link:hover {
            color: var(--color-primary);
            text-decoration: underline;
        }
        .route-action-danger,
        .route-action-danger:hover {
            color: var(--app-danger);
        }
    </style>
</head>
<body>
<div class="layui-layout layui-layout-admin app-layout">
<#include "inc/sidebar.ftlh">
    <div class="layui-body app-body">
        <div class="app-page">
            <div class="page-header">
                <h2>线路管理</h2>
            </div>
            <div class="page-body">
                <div class="table-toolbar">
                    <div class="table-toolbar__filters"></div>
                    <div class="table-toolbar__actions">
                        <a href="/route/create" class="layui-btn layui-btn-sm"><i class="layui-icon layui-icon-add-1"></i>创建新线路</a>
                    </div>
                </div>
                <div class="app-table-panel">
                    <table id="route-table" lay-filter="route-table"></table>
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
        setCurrentMenu('route');
        layui.use('table', function()
        {
            var table = layui.table;
            window.routeTable = table.render($.extend(true, request.table('/route/list'), {
                elem: '#route-table',
                page: { layout: ['prev', 'page', 'next', 'count', 'limit'] },
                limit: 20,
                cols: [[
                    {
                        field: 'id',
                        title: '线路ID',
                        align: 'center',
                        width: 160
                    },
                    {
                        field: 'name',
                        title: '线路名称',
                        align: 'left',
                        templet: function(r)
                        {
                            return escapeHtml(r.name);
                        }
                    },
                    {
                        field: 'minSpeed',
                        title: '最低速度',
                        align: 'right',
                        width: 160,
                        templet: function(r)
                        {
                            return escapeHtml(r.minSpeed) + ' km/h';
                        }
                    },
                    {
                        field: 'maxSpeed',
                        title: '最高速度',
                        align: 'right',
                        width: 160,
                        templet: function(r)
                        {
                            return escapeHtml(r.maxSpeed) + ' km/h';
                        }
                    },
                    {
                        field: 'mileages',
                        title: '里程',
                        align: 'right',
                        width: 160,
                        templet: function(r)
                        {
                            return r.mileages ? (r.mileages / 1000).toFixed(1) + ' km' : '--';
                        }
                    },
                    {
                        title: '操作',
                        align: 'left',
                        width: 120,
                        templet: function(r)
                        {
                            var html = '<span class="route-actions">';
                            html += '<a href="/route/edit?id=' + r.id + '" class="route-action-link">编辑</a>';
                            html += '<a href="javascript:;" lay-event="remove" class="route-action-link route-action-danger">删除</a>';
                            html += '</span>';
                            return html;
                        }
                    }
                ]]
            }));
            table.on('tool(route-table)', function(obj)
            {
                if (obj.event === 'remove') remove(obj.data.id);
            });
        });
    });

    function remove(id)
    {
        appConfirm('真的要删除吗？', function()
        {
            request.post('/route/remove', { id : id }).done(function()
            {
                appNotify('success', '删除成功');
                layui.table.reload('route-table');
            });
        });
    }

</script>
</html>
