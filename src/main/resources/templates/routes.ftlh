<!DOCTYPE html>
<html lang="en">
<head>
<#include "inc/resource.ftlh">
    <title>线路管理</title>
</head>
<body>
<div class="layui-layout layui-layout-admin app-layout">
<#include "inc/sidebar.ftlh">
    <div class="layui-body app-body">
        <div class="app-page">
            <div class="page-header">
                <h2>线路管理</h2>
                <a href="/route/create" class="layui-btn layui-btn-sm"><i class="layui-icon layui-icon-add-1"></i>创建新线路</a>
            </div>
            <div class="page-body">
                <div class="app-table-panel">
                    <div id="route-table"></div>
                    <div id="route-page" class="app-pagination"></div>
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
        window.routeTable = appTable.render({
            elem: '#route-table',
            pageElem: '#route-page',
            url: '/route/list',
            fields: [
                {
                    name: 'id',
                    title: '线路ID',
                    align: 'center',
                    width: '160px',
                    formatter: function (i, v, r)
                    {
                        return v;
                    }
                },
                {
                    name: 'name',
                    title: '线路名称',
                    align: 'left',
                    formatter: function (i, v, r)
                    {

                        return v;
                    }
                },
                {
                    name: 'minSpeed',
                    title: '最低速度',
                    align: 'right',
                    width : '160px',
                    formatter: function (i, v, r)
                    {
                        return v + ' km/h';
                    }
                },
                {
                    name: 'maxSpeed',
                    title: '最高速度',
                    align: 'right',
                    width : '160px',
                    formatter: function (i, v, r)
                    {
                        return v + ' km/h';
                    }
                },
                {
                    name: 'mileages',
                    title: '里程',
                    align: 'right',
                    width : '160px',
                    formatter: function (i, v, r)
                    {
                        return v ? (v / 1000).toFixed(1) + ' km' : '--';
                    }
                },
                {
                    name: 'id',
                    title: '操作',
                    align: 'center',
                    width : '160px',
                    formatter: function (i, v, r)
                    {
                        var html = '';
                        html += '<a href="/route/edit?id=' + v + '" class="layui-btn layui-btn-xs">编辑</a>';
                        html += '<a href="javascript:;" onclick="remove(\'' + v + '\')" class="layui-btn layui-btn-xs layui-btn-danger">删除</a>';
                        return html;
                    }
                }
            ]
        });
    });

    function remove(id)
    {
        appConfirm('真的要删除吗？', function()
        {
            $.post('/route/remove', { id : id }, function(result)
            {
                if (result.error && result.error.code) return appNotify('error', result.error.reason);
                appNotify('success', '删除成功');
                window.routeTable.reload();
            });
        });
    }

</script>
</html>
