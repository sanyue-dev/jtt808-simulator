<!DOCTYPE html>
<html lang="en">
<head>
    <#include "inc/resource.ftlh">
    <title>创建临时行程</title>
</head>
<body>
<div class="layout">
    <#include "inc/sidebar.ftlh">
    <div class="layout__content form">
        <h2>创建临时行程任务</h2>
        <hr />
        <div class="grid__row">
            <div class="grid__col--2 text-right">行驶线路：</div>
            <div class="grid__col--6">
                <select name="routeId" id="routeId" class="">
                    <option value="0">- 请选择行驶线路 -</option>
                    <#list routes as item>
                        <option value="${item.id}">${item.name} - ${item.kilometers!'NaN'}公里</option>
                    </#list>
                </select>
            </div>
        </div>
        <div class="grid__row">
            <div class="grid__col--2 text-right">车牌号：</div>
            <div class="grid__col--6"><input type="text" name="vehicleNumber" id="vehicleNumber" /></div>
        </div>
        <div class="grid__row">
            <div class="grid__col--2 text-right">终端ID：</div>
            <div class="grid__col--6"><input type="text" name="deviceSn" id="deviceSn" /></div>
        </div>
        <div class="grid__row">
            <div class="grid__col--2 text-right">终端SIM卡号：</div>
            <div class="grid__col--6"><input type="text" name="simNumber" id="simNumber" /></div>
        </div>

        <div class="grid__row">
            <div class="grid__col--2 text-right">初始里程数：</div>
            <div class="grid__col--6"><input type="text" name="mileages" id="mileages" placeholder="单位公里，默认为0" /></div>
        </div>

        <div class="grid__row">
            <div class="grid__col--2 text-right">808网关服务器：</div>
            <div class="grid__col--6"><input type="text" name="serverAddress" id="serverAddress" value="${vehicleServerAddr}" /></div>
        </div>
        <div class="grid__row">
            <div class="grid__col--2 text-right">808网关服务器端口：</div>
            <div class="grid__col--6"><input type="text" name="serverPort" id="serverPort" value="${vehicleServerPort}" /></div>
        </div>
        <div class="grid__row">
            <div class="grid__col--2 text-right">&nbsp;</div>
            <div class="grid__col--6">
                <button class="btn btn--blue" id="btn-run">启动</button>
            </div>
        </div>
    </div>
</div>
</body>
<#include "inc/footer.ftlh">
<script type="text/javascript">
    $(document).ready(function()
    {
        setCurrentMenu("task");
        $('#btn-run').click(function()
        {
            var params = {
                routeId : $('#routeId').val(),
                vehicleNumber : $('#vehicleNumber').val(),
                deviceSn : $('#deviceSn').val(),
                simNumber : $('#simNumber').val(),
                mileages : $('#mileages').val(),
                serverAddress : $('#serverAddress').val(),
                serverPort : $('#serverPort').val()
            };

            $.post('./run', params, function(result)
            {
                if (result.error && result.error.code) return toastr('error', result.error.reason);
                toastr('success', '启动成功');
            });
        });
    });
</script>
</html>
