<!DOCTYPE html>
<html lang="en">
<head>
<#include "inc/resource.ftlh">
    <title>行程任务</title>
</head>
<body>
<div class="container">
<#include "inc/sidebar.ftlh">
    <div class="content datasheet">
        <h2>行程任务</h2>
        <hr />
        <div id="route-table"></div>
        <ul class="pagination"></ul>
    </div>
</div>
</body>
<#include "inc/footer.ftlh">
<script type="text/javascript">
    $(document).ready(function()
    {
        setCurrentMenu('list-monitor');
        $('#route-table').paginate({
            url: '/monitor/list/json',
            fields: [
                {
                    name: 'routeName',
                    title: '线路名称',
                    align: 'left',
                },
                {
                    name : 'routeMileages',
                    title : '线路里程',
                    align : 'right',
                    formatter : function(i, v, r)
                    {
                        return parseInt(v / 1000) + ' km';
                    }
                },
                {
                    name: 'vehicleNumber',
                    title: '车牌号',
                    align: 'center',
                },
                {
                    name: 'simNumber',
                    title: 'SIM卡号',
                    align: 'center',
                },
                {
                    name: 'deviceSn',
                    title: '终端ID',
                    align: 'center',
                },
                {
                    name : 'startTime',
                    title : '启动时间',
                    align : 'center',
                    formatter : function(i, v, r)
                    {
                        return new Date(v).format('yyyy-MM-dd hh:mm:ss');
                    }
                },
                {
                    name : 'longitude',
                    title : '经度',
                    align : 'right',
                    formatter : function(i, v, r)
                    {
                        return v ? String(v).replace(/^(\d+\.\d{6})\d+$/gi, '$1') : '--';
                    }
                },
                {
                    name : 'latitude',
                    title : '纬度',
                    align : 'right',
                    formatter : function(i, v, r)
                    {
                        return v ? String(v).replace(/^(\d+\.\d{6})\d+$/gi, '$1') : '--';
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
                        html += '<a href="/monitor/view?id=' + v + '" target="_blank" class="btn btn-sm btn-blue">详情</a>';
                        return html;
                    }
                }
            ]
        });
    });
</script>
</html>
