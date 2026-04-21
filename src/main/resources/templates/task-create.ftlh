<!DOCTYPE html>
<html lang="en">
<head>
    <#include "inc/resource.ftlh">
    <title>创建临时行程</title>
</head>
<body>
<div class="layout">
    <#include "inc/sidebar.ftlh">
    <div class="layout__content">
        <div class="layout__page">
            <div class="page-header">
                <h2>创建临时行程任务</h2>
            </div>
            <div class="page-body">
                <div class="task">

            <!-- 线路选择 -->
            <div class="card-section">
                <div class="card-section__header">
                    <h3>行驶线路</h3>
                </div>
                <select name="routeId" id="routeId" class="task__select">
                    <option value="0">- 请选择行驶线路 -</option>
                    <#list routes as item>
                        <option value="${item.id}">${item.name} - ${item.kilometers!'NaN'}公里</option>
                    </#list>
                </select>
            </div>

            <!-- 车辆信息 -->
            <div class="card-section">
                <div class="card-section__header">
                    <h3>车辆信息</h3>
                    <button type="button" class="task__regen-all" id="btn-regen-all" title="全部重新生成"><i class="fa fa-refresh"></i> 全部重新生成</button>
                </div>
                <div class="card-section__fields">
                    <div class="card-section__field">
                        <label class="card-section__label">车牌号</label>
                        <div class="task__input-row">
                            <input type="text" name="vehicleNumber" id="vehicleNumber" class="card-section__input" />
                            <button type="button" class="task__regen-btn" data-target="vehicleNumber" title="重新生成"><i class="fa fa-refresh"></i></button>
                        </div>
                    </div>
                    <div class="card-section__field">
                        <label class="card-section__label">终端ID</label>
                        <div class="task__input-row">
                            <input type="text" name="deviceSn" id="deviceSn" class="card-section__input" />
                            <button type="button" class="task__regen-btn" data-target="deviceSn" title="重新生成"><i class="fa fa-refresh"></i></button>
                        </div>
                    </div>
                    <div class="card-section__field">
                        <label class="card-section__label">终端SIM卡号</label>
                        <div class="task__input-row">
                            <input type="text" name="simNumber" id="simNumber" class="card-section__input" />
                            <button type="button" class="task__regen-btn" data-target="simNumber" title="重新生成"><i class="fa fa-refresh"></i></button>
                        </div>
                    </div>
                    <div class="card-section__field" style="max-width: 180px;">
                        <label class="card-section__label">初始里程数</label>
                        <input type="text" name="mileages" id="mileages" class="card-section__input" placeholder="公里，默认0" />
                    </div>
                </div>
            </div>

            <!-- 服务器配置 -->
            <div class="card-section">
                <div class="card-section__header">
                    <h3>服务器配置</h3>
                </div>
                <div class="card-section__fields">
                    <div class="card-section__field">
                        <label class="card-section__label">808 网关地址</label>
                        <input type="text" name="serverAddress" id="serverAddress" class="card-section__input" value="${vehicleServerAddr}" />
                    </div>
                    <div class="card-section__field" style="max-width: 180px;">
                        <label class="card-section__label">端口</label>
                        <input type="text" name="serverPort" id="serverPort" class="card-section__input" value="${vehicleServerPort}" />
                    </div>
                </div>
            </div>

            <!-- 提交 -->
            <div class="card-section__actions">
                <button class="btn btn--blue" id="btn-run" style="min-width: 120px; height: 38px; line-height: 38px; font-size: 14px;">启动</button>
            </div>

                </div>
            </div>
        </div>
    </div>
</div>
</body>
<#include "inc/footer.ftlh">
<style>
    .task { max-width: 860px; }
    .task__select {
        height: 40px; width: 100%; padding: 0 30px 0 14px;
        box-sizing: border-box;
        border: 1px solid var(--color-border); border-radius: var(--radius-sm);
        background-color: var(--color-bg-panel); color: var(--color-text-primary);
        appearance: none;
        background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath d='M2 4l4 4 4-4' fill='none' stroke='%23999' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round'/%3E%3C/svg%3E");
        background-repeat: no-repeat; background-position: right 12px center;
        font-size: 13px; cursor: pointer;
        transition: border-color var(--transition-fast);
    }
    .task__select:focus { border-color: var(--color-text-primary); outline: none; }

    .task__input-row { display: flex; align-items: center; }
    .task__input-row .card-section__input { flex: 1; border-radius: var(--radius-sm) 0 0 var(--radius-sm); }
    .task__regen-btn {
        width: 36px; height: 36px; border: 1px solid var(--color-border);
        border-left: none; border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
        background: var(--color-bg-panel-muted); color: var(--color-text-secondary);
        cursor: pointer; display: flex; align-items: center; justify-content: center;
        transition: background var(--transition-fast), color var(--transition-fast);
    }
    .task__regen-btn:hover { background: var(--color-bg-muted); color: var(--color-text-primary); }
    .task__regen-btn i { font-size: 11px; transition: transform .3s ease; }
    .task__regen-btn:active i { transform: rotate(180deg); }

    .task__regen-all {
        background: none; border: 1px solid var(--color-border); border-radius: var(--radius-sm);
        cursor: pointer; font-size: 11px; color: var(--color-text-secondary);
        padding: 4px 10px; display: flex; align-items: center; gap: 5px;
        transition: background var(--transition-fast), color var(--transition-fast), border-color var(--transition-fast);
    }
    .task__regen-all:hover { background: var(--color-bg-muted); color: var(--color-text-primary); border-color: var(--color-border-strong); }
    .task__regen-all i { font-size: 10px; }
</style>
<script type="text/javascript">
    $(document).ready(function()
    {
        setCurrentMenu("task");

        function randHex(len)
        {
            var s = '', c = '0123456789abcdef';
            for (var i = 0; i < len; i++) s += c.charAt(Math.floor(Math.random() * c.length));
            return s;
        }
        function randDigit(len)
        {
            var s = '';
            for (var i = 0; i < len; i++) s += Math.floor(Math.random() * 10);
            return s;
        }
        var generators = {
            vehicleNumber: function() { return '京A' + randDigit(5); },
            deviceSn: function() { return randHex(12).toUpperCase(); },
            simNumber: function() { return '013800' + randDigit(6); }
        };

        function fillAll()
        {
            for (var k in generators) $('#' + k).val(generators[k]());
        }
        fillAll();

        $('.task__regen-btn').click(function()
        {
            var t = $(this).data('target');
            if (generators[t]) $('#' + t).val(generators[t]());
        });
        $('#btn-regen-all').click(function() { fillAll(); });

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
                fillAll();
            });
        });
    });
</script>
</html>
