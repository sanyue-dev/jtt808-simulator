<!DOCTYPE html>
<html lang="en">
<head>
    <#include "inc/resource.ftlh">
    <title>创建临时行程</title>
</head>
<body>
<div class="layui-layout layui-layout-admin app-layout">
    <#include "inc/sidebar.ftlh">
    <div class="layui-body app-body">
        <div class="app-page">
            <div class="page-header">
                <h2>创建临时行程任务</h2>
            </div>
            <div class="page-body">
                <form class="layui-form task-form" lay-filter="task-form">
                    <div class="layui-card">
                        <div class="layui-card-header">行驶线路</div>
                        <div class="layui-card-body">
                            <div class="layui-form-item">
                                <label class="layui-form-label">线路</label>
                                <div class="layui-input-block">
                                    <select name="routeId" id="routeId" lay-search>
                                        <option value="0">请选择行驶线路</option>
                                        <#list routes as item>
                                        <option value="${item.id}">${item.name} - ${item.kilometers!'NaN'}公里</option>
                                        </#list>
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="layui-card">
                        <div class="layui-card-header">
                            <span>车辆信息</span>
                            <button type="button" class="layui-btn layui-btn-primary layui-btn-sm" id="btn-regen-all"><i class="layui-icon layui-icon-refresh"></i>全部重新生成</button>
                        </div>
                        <div class="layui-card-body">
                            <div class="form-grid">
                                <div class="layui-form-item">
                                    <label class="layui-form-label">车牌号</label>
                                    <div class="layui-input-block task__input-row">
                                        <input type="text" name="vehicleNumber" id="vehicleNumber" class="layui-input" />
                                        <button type="button" class="layui-btn layui-btn-primary task__regen-btn" data-target="vehicleNumber" title="重新生成"><i class="layui-icon layui-icon-refresh"></i></button>
                                    </div>
                                </div>
                                <div class="layui-form-item">
                                    <label class="layui-form-label">终端ID</label>
                                    <div class="layui-input-block task__input-row">
                                        <input type="text" name="deviceSn" id="deviceSn" class="layui-input" />
                                        <button type="button" class="layui-btn layui-btn-primary task__regen-btn" data-target="deviceSn" title="重新生成"><i class="layui-icon layui-icon-refresh"></i></button>
                                    </div>
                                </div>
                                <div class="layui-form-item">
                                    <label class="layui-form-label">SIM卡号</label>
                                    <div class="layui-input-block task__input-row">
                                        <input type="text" name="simNumber" id="simNumber" class="layui-input" />
                                        <button type="button" class="layui-btn layui-btn-primary task__regen-btn" data-target="simNumber" title="重新生成"><i class="layui-icon layui-icon-refresh"></i></button>
                                    </div>
                                </div>
                                <div class="layui-form-item">
                                    <label class="layui-form-label">初始里程</label>
                                    <div class="layui-input-block">
                                        <input type="text" name="mileages" id="mileages" class="layui-input" placeholder="公里，默认0" />
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="layui-card">
                        <div class="layui-card-header">服务器配置</div>
                        <div class="layui-card-body">
                            <div class="form-grid">
                                <div class="layui-form-item">
                                    <label class="layui-form-label">网关地址</label>
                                    <div class="layui-input-block"><input type="text" name="serverAddress" id="serverAddress" class="layui-input" value="${vehicleServerAddr}" /></div>
                                </div>
                                <div class="layui-form-item">
                                    <label class="layui-form-label">端口</label>
                                    <div class="layui-input-block"><input type="text" name="serverPort" id="serverPort" class="layui-input" value="${vehicleServerPort}" /></div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="app-form-note">启动后会生成任务组，并进入任务组监控页观察启动与运行状态。</div>
                    <div class="page-actions">
                        <button type="button" class="layui-btn" id="btn-run"><i class="layui-icon layui-icon-play"></i>启动</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
</body>
<#include "inc/footer.ftlh">
<style>
    .task-form { max-width: 980px; }
    .task__input-row { display: flex; }
    .task__input-row .layui-input { flex: 1; }
    .task__regen-btn { width: 38px; padding: 0; border-left: 0; }
</style>
<script type="text/javascript">
    layui.use('form', function()
    {
        layui.form.render();
    });

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

            request.post('./run', params).done(function(result)
            {
                window.location.href = '/task-groups/monitor?taskGroupId=' + result.taskGroupId;
            });
        });
    });
</script>
</html>
