<!DOCTYPE html>
<html lang="en">
<head>
    <#include "inc/resource.ftlh">
    <title>批量创建行程</title>
</head>
<body>
<div class="layui-layout layui-layout-admin app-layout">
    <#include "inc/sidebar.ftlh">
    <div class="layui-body app-body">
        <div class="app-page">
            <div class="page-header layui-bg-gray">
                <h2>批量创建行程任务</h2>
            </div>
            <div class="page-body">
                <form class="layui-form batch-form">
                    <div class="layui-card">
                        <div class="layui-card-header">
                            <span>行驶线路</span>
                            <div>
                                <button type="button" class="layui-btn layui-btn-primary layui-btn-sm" id="btn-select-all">全选</button>
                                <button type="button" class="layui-btn layui-btn-primary layui-btn-sm" id="btn-clear-all">清空</button>
                                <span class="layui-badge layui-bg-gray" id="selected-count"></span>
                            </div>
                        </div>
                        <div class="layui-card-body">
                            <blockquote class="layui-elem-quote layui-quote-nm">不选则由系统从所有线路中轮询分配，选中则从所选线路中轮询分配</blockquote>
                            <div class="batch__route-transfer" id="route-transfer"></div>
                        </div>
                    </div>

                    <div class="layui-card">
                        <div class="layui-card-header">车辆配置</div>
                        <div class="layui-card-body">
                            <div class="form-grid">
                                <div class="layui-form-item">
                                    <label class="layui-form-label" for="vehicleCount">车辆数量</label>
                                    <div class="layui-input-block">
                                        <input type="number" name="vehicleCount" id="vehicleCount" class="layui-input" min="1" max="100000" value="1000" />
                                        <div class="layui-form-mid">最多 100,000 辆</div>
                                    </div>
                                </div>
                                <div class="layui-form-item">
                                    <label class="layui-form-label" for="vehicleNumberPattern">车牌号规则</label>
                                    <div class="layui-input-block batch__pattern-row">
                                        <input type="text" name="vehicleNumberPattern" id="vehicleNumberPattern" class="layui-input" value="京%06d" />
                                        <span class="tag-muted" id="preview-plate">京000001</span>
                                    </div>
                                </div>
                                <div class="layui-form-item">
                                    <label class="layui-form-label" for="deviceSnPattern">终端ID规则</label>
                                    <div class="layui-input-block batch__pattern-row">
                                        <input type="text" name="deviceSnPattern" id="deviceSnPattern" class="layui-input" value="A%06d" />
                                        <span class="tag-muted" id="preview-sn">A000001</span>
                                    </div>
                                </div>
                                <div class="layui-form-item">
                                    <label class="layui-form-label" for="simNumberPattern">SIM卡规则</label>
                                    <div class="layui-input-block batch__pattern-row">
                                        <input type="text" name="simNumberPattern" id="simNumberPattern" class="layui-input" value="013800%06d" />
                                        <span class="tag-muted" id="preview-sim">013800000001</span>
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
                                    <label class="layui-form-label" for="serverAddress">网关地址</label>
                                    <div class="layui-input-block"><input type="text" name="serverAddress" id="serverAddress" class="layui-input" value="${vehicleServerAddr}" /></div>
                                </div>
                                <div class="layui-form-item">
                                    <label class="layui-form-label" for="serverPort">端口</label>
                                    <div class="layui-input-block"><input type="text" name="serverPort" id="serverPort" class="layui-input" value="${vehicleServerPort}" /></div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="layui-card">
                        <div class="layui-card-header">启动控制</div>
                        <div class="layui-card-body">
                            <div class="form-grid">
                                <div class="layui-form-item">
                                    <label class="layui-form-label" for="reportIntervalSeconds">上报间隔</label>
                                    <div class="layui-input-block"><input type="number" name="reportIntervalSeconds" id="reportIntervalSeconds" class="layui-input" min="1" value="5" /></div>
                                </div>
                                <div class="layui-form-item">
                                    <label class="layui-form-label" for="runDurationSeconds">运行时长</label>
                                    <div class="layui-input-block">
                                        <input type="number" name="runDurationSeconds" id="runDurationSeconds" class="layui-input" min="0" value="0" />
                                        <div class="layui-form-mid">0 表示不自动停止</div>
                                    </div>
                                </div>
                                <div class="layui-form-item">
                                    <label class="layui-form-label" for="rampUpBatchSize">ramp-up 窗口大小</label>
                                    <div class="layui-input-block">
                                        <input type="number" name="rampUpBatchSize" id="rampUpBatchSize" class="layui-input" min="0" value="0" />
                                        <div class="layui-form-mid">0 表示一次性提交全部任务</div>
                                    </div>
                                </div>
                                <div class="layui-form-item">
                                    <label class="layui-form-label" for="rampUpIntervalMillis">窗口间隔</label>
                                    <div class="layui-input-block"><input type="number" name="rampUpIntervalMillis" id="rampUpIntervalMillis" class="layui-input" min="1" value="1" /></div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="app-form-note">创建后会生成任务组，并进入任务组监控页观察启动进度。</div>
                    <div class="page-actions">
                        <button type="button" class="layui-btn" id="btn-run"><i class="layui-icon layui-icon-play"></i>创建任务</button>
                    </div>

                    <div class="layui-card batch-progress" id="batch-progress" style="display:none;">
                        <div class="layui-card-header">当前启动进度</div>
                        <div class="layui-card-body">
                            <div class="app-metrics">
                                <div class="app-metric"><span>状态</span><strong data-field="state">-</strong></div>
                                <div class="app-metric"><span>目标终端数</span><strong data-field="targetTasks">0</strong></div>
                                <div class="app-metric"><span>已启动任务</span><strong data-field="startedTasks">0</strong></div>
                                <div class="app-metric"><span>启动窗口</span><strong data-field="windowProgress">0 / 0</strong></div>
                                <div class="app-metric"><span>自动停止</span><strong data-field="autoStopScheduled">否</strong></div>
                                <div class="app-metric"><span>停止结果</span><strong data-field="stopResult">0 / 0</strong></div>
                            </div>
                            <div class="layui-bg-red batch-progress__failure" data-field="failureReason" style="display:none;"></div>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
</body>
<#include "inc/footer.ftlh">
<style>
    .batch-form { max-width: 980px; }
    .batch__route-transfer { padding: 8px 0; }
    .batch__route-transfer .layui-transfer { display: flex; align-items: center; gap: 14px; width: 100%; }
    .batch__route-transfer .layui-transfer-box { flex: 1 1 0; width: auto !important; height: 320px !important; }
    .batch__route-transfer .layui-transfer-active { flex: 0 0 52px; margin: 0; }
    .batch__route-transfer .layui-transfer-data { height: 225px !important; }
    .batch__pattern-row { display: flex; align-items: center; gap: 10px; }
    .batch__pattern-row .layui-input { flex: 1; }
    .batch-progress { margin-top: 16px; }
    .batch-progress__failure { margin-top: 10px; padding: 10px 12px; }
    @media (max-width: 768px) {
        .batch__route-transfer .layui-transfer { display: block; }
        .batch__route-transfer .layui-transfer-box { display: block; width: 100% !important; }
        .batch__route-transfer .layui-transfer-active { display: flex; justify-content: center; margin: 12px 0; }
        .batch__route-transfer .layui-transfer-active .layui-btn { display: inline-block; margin: 0 6px; }
    }
</style>
<script type="text/javascript">
    var routeTransferData = [
        <#list routes as item>
        { value: '${item.id}', title: '${item.name?js_string} - ${(item.kilometers!'NaN')?js_string}公里' }<#sep>,
        </#list>
    ];

    layui.use(['form', 'transfer'], function()
    {
        var form = layui.form;
        var transfer = layui.transfer;
        function decorateRouteTransferFields()
        {
            $('#route-transfer .layui-transfer-box').each(function(index)
            {
                var input = $(this).find('.layui-transfer-search input');
                var suffix = index === 0 ? 'all' : 'selected';
                input.attr({
                    id: 'route-transfer-search-' + suffix,
                    name: 'routeTransferSearch' + (index === 0 ? 'All' : 'Selected'),
                    'aria-label': index === 0 ? '搜索全部线路' : '搜索已选线路'
                });
            });
        }

        transfer.render({
            elem: '#route-transfer',
            id: 'route-transfer',
            title: ['全部线路', '已选线路'],
            data: routeTransferData,
            value: [],
            showSearch: true,
            onchange: function() { if (window.updateCount) window.updateCount(); }
        });
        decorateRouteTransferFields();
        form.render();
        window.decorateRouteTransferFields = decorateRouteTransferFields;
    });

    $(document).ready(function()
    {
        setCurrentMenu('batch');
        var progressTimer = null;

        function updateCount()
        {
            var n = layui.transfer.getData('route-transfer').length;
            $('#selected-count').text('已选 ' + n + ' 条');
        }
        window.updateCount = updateCount;
        updateCount();

        $('#btn-select-all').click(function()
        {
            layui.transfer.reload('route-transfer', {
                value: routeTransferData.map(function(item) { return item.value; })
            });
            if (window.decorateRouteTransferFields) window.decorateRouteTransferFields();
            updateCount();
        });
        $('#btn-clear-all').click(function()
        {
            layui.transfer.reload('route-transfer', { value: [] });
            if (window.decorateRouteTransferFields) window.decorateRouteTransferFields();
            updateCount();
        });

        function applyPattern(pattern, index)
        {
            if (!pattern) return '';
            return pattern.replace(/%0*(\d+)d/g, function(_, width) {
                return String(index).padStart(parseInt(width), '0');
            });
        }
        function refreshPreview(inputId, previewId)
        {
            $('#' + previewId).text(applyPattern($('#' + inputId).val(), 1));
        }
        $('#vehicleNumberPattern').on('input', function() { refreshPreview('vehicleNumberPattern', 'preview-plate'); });
        $('#deviceSnPattern').on('input', function() { refreshPreview('deviceSnPattern', 'preview-sn'); });
        $('#simNumberPattern').on('input', function() { refreshPreview('simNumberPattern', 'preview-sim'); });

        function startProgressPolling()
        {
            loadProgress();
            if (progressTimer) clearInterval(progressTimer);
            progressTimer = setInterval(loadProgress, 1000);
        }

        function stopProgressPollingIfFinished(data)
        {
            if (!data || data.empty) return;
            if ((data.state === 'running' && !data.autoStopScheduled) || data.state === 'completed' || data.state === 'failed')
            {
                clearInterval(progressTimer);
                progressTimer = null;
            }
        }

        function loadProgress()
        {
            $.post('./progress', function(result)
            {
                if (result.error && result.error.code) return appNotify('error', result.error.reason);
                renderProgress(result.data || {});
                stopProgressPollingIfFinished(result.data || {});
            });
        }

        function renderProgress(data)
        {
            if (data.empty) return;
            $('#batch-progress').show();
            $('#batch-progress [data-field="state"]').text(formatState(data.state));
            $('#batch-progress [data-field="targetTasks"]').text(data.targetTasks || 0);
            $('#batch-progress [data-field="startedTasks"]').text(data.startedTasks || 0);
            $('#batch-progress [data-field="windowProgress"]').text((data.executedWindowCount || 0) + ' / ' + (data.rampUpWindowCount || 0));
            $('#batch-progress [data-field="autoStopScheduled"]').text(data.autoStopScheduled ? '是' : '否');
            $('#batch-progress [data-field="stopResult"]').text((data.stopSucceeded || 0) + ' / ' + (data.stopFailed || 0));
            if (data.failureReason) $('#batch-progress [data-field="failureReason"]').text(data.failureReason).show();
            else $('#batch-progress [data-field="failureReason"]').hide();
        }

        function formatState(state)
        {
            if (state === 'launching') return '启动中';
            if (state === 'running') return '运行中';
            if (state === 'stopping') return '停止中';
            if (state === 'completed') return '已完成';
            if (state === 'failed') return '失败';
            return '-';
        }

        $('#btn-run').click(function()
        {
            var idList = layui.transfer.getData('route-transfer').map(function(item) { return item.value; });
            if (idList.length === 0) idList = ['0'];

            var params = {
                routeIdList : idList,
                vehicleCount : $('#vehicleCount').val(),
                vehicleNumberPattern : $('#vehicleNumberPattern').val(),
                deviceSnPattern : $('#deviceSnPattern').val(),
                simNumberPattern : $('#simNumberPattern').val(),
                serverAddress : $('#serverAddress').val(),
                serverPort : $('#serverPort').val(),
                reportIntervalSeconds : $('#reportIntervalSeconds').val(),
                runDurationSeconds : $('#runDurationSeconds').val(),
                rampUpBatchSize : $('#rampUpBatchSize').val(),
                rampUpIntervalMillis : $('#rampUpIntervalMillis').val()
            };
            $.post('./run', params, function(result)
            {
                if (result.error && result.error.code) return appNotify('error', result.error.reason);
                window.location.href = '/task-groups/monitor?taskGroupId=' + result.data.taskGroupId;
            });
        });
    });
</script>
</html>
