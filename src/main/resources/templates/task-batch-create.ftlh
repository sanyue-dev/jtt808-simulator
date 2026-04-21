<!DOCTYPE html>
<html lang="en">
<head>
    <#include "inc/resource.ftlh">
    <title>批量创建行程</title>
</head>
<body>
<div class="layout">
    <#include "inc/sidebar.ftlh">
    <div class="layout__content">
        <div class="layout__page">
            <div class="page-header">
                <h2>批量创建行程任务</h2>
            </div>
            <div class="page-body">
                <div class="batch">

            <!-- 线路选择 -->
            <div class="card-section">
                <div class="card-section-header">
                    <h3>行驶线路</h3>
                    <div class="batch__route-toolbar">
                        <button type="button" class="batch__toolbar-btn" id="btn-select-all">全选</button>
                        <button type="button" class="batch__toolbar-btn" id="btn-clear-all">清空</button>
                        <span class="batch__selected-count" id="selected-count"></span>
                    </div>
                </div>

                <p class="batch__route-hint">不选则由系统从所有线路中随机分配，选中则从所选线路中随机分配</p>

                <!-- 线路列表 -->
                <div class="batch__route-list" id="route-list">
                    <#list routes as item>
                    <label class="batch__route-item">
                        <input type="checkbox" class="batch__route-checkbox" value="${item.id}" />
                        <span class="batch__route-checkmark"></span>
                        <span class="batch__route-name">${item.name}</span>
                        <span class="batch__route-tag">${item.kilometers!'NaN'}公里</span>
                    </label>
                    </#list>
                </div>
            </div>

            <!-- 车辆配置 -->
            <div class="card-section">
                <div class="card-section-header">
                    <h3>车辆配置</h3>
                </div>
                <div class="card-section__fields">
                    <div class="card-section__field">
                        <label class="card-section__label">车辆数量</label>
                        <input type="number" name="vehicleCount" id="vehicleCount" class="card-section__input" min="1" max="100000" value="1000" />
                        <span class="card-section__field-hint">最多 100,000 辆</span>
                    </div>
                    <div class="card-section__field">
                        <label class="card-section__label">车牌号规则</label>
                        <div class="card-section__input-group">
                            <input type="text" name="vehicleNumberPattern" id="vehicleNumberPattern" class="card-section__input" value="京%06d" />
                            <span class="card-section__preview" id="preview-plate">京000001</span>
                        </div>
                    </div>
                    <div class="card-section__field">
                        <label class="card-section__label">终端ID规则</label>
                        <div class="card-section__input-group">
                            <input type="text" name="deviceSnPattern" id="deviceSnPattern" class="card-section__input" value="A%06d" />
                            <span class="card-section__preview" id="preview-sn">A000001</span>
                        </div>
                    </div>
                    <div class="card-section__field">
                        <label class="card-section__label">SIM卡号规则</label>
                        <div class="card-section__input-group">
                            <input type="text" name="simNumberPattern" id="simNumberPattern" class="card-section__input" value="013800%06d" />
                            <span class="card-section__preview" id="preview-sim">013800000001</span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 服务器配置 -->
            <div class="card-section">
                <div class="card-section-header">
                    <h3>服务器配置</h3>
                </div>
                <div class="card-section__fields">
                    <div class="card-section__field">
                        <label class="card-section__label">808 网关地址</label>
                        <input type="text" name="serverAddress" id="serverAddress" class="card-section__input" value="${vehicleServerAddr}" />
                    </div>
                    <div class="card-section__field">
                        <label class="card-section__label">端口</label>
                        <input type="text" name="serverPort" id="serverPort" class="card-section__input" style="max-width: 140px;" value="${vehicleServerPort}" />
                    </div>
                </div>
            </div>

            <!-- 提交 -->
            <div class="card-section__actions">
                <button class="btn btn--blue" id="btn-run" style="min-width: 120px; height: 38px; line-height: 38px; font-size: 14px;">创建任务</button>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
<#include "inc/footer.ftlh">
<style>
    .batch { max-width: 860px; }

    .batch__route-toolbar { display: flex; align-items: center; gap: 12px; }
    .batch__toolbar-btn {
        background: none; border: none; cursor: pointer;
        font-size: 12px; color: var(--color-text-secondary); padding: 2px 0;
        transition: color var(--transition-fast);
    }
    .batch__toolbar-btn:hover { color: var(--color-text-primary); }
    .batch__selected-count {
        font-size: 11px; color: var(--color-text-secondary);
        background: var(--color-bg-muted); padding: 2px 8px; border-radius: var(--radius-sm);
    }

    .batch__route-hint {
        font-size: 12px; color: var(--color-text-secondary); margin: 0 0 12px 0;
    }

    .batch__route-list {
        border: 1px solid var(--color-border);
        max-height: 260px; overflow-y: auto; overflow-x: hidden;
    }
    .batch__route-item {
        display: flex; align-items: center; gap: 10px;
        padding: 10px 14px; cursor: pointer;
        border-bottom: 1px solid var(--color-border);
        transition: background var(--transition-fast);
    }
    .batch__route-item:last-child { border-bottom: none; }
    .batch__route-item:hover { background: var(--color-bg-panel-muted); }
    .batch__route-item input { display: none; }
    .batch__route-checkmark {
        width: 16px; height: 16px; border-radius: 2px; flex-shrink: 0;
        border: 1.5px solid var(--color-border-strong);
        position: relative; transition: border-color var(--transition-fast), background var(--transition-fast);
    }
    .batch__route-item:hover .batch__route-checkmark { border-color: var(--color-text-primary); }
    .batch__route-item input:checked ~ .batch__route-checkmark {
        background: var(--color-primary); border-color: var(--color-primary);
    }
    .batch__route-item input:checked ~ .batch__route-checkmark::after {
        content: ''; position: absolute; top: 1px; left: 4px;
        width: 5px; height: 8px; border: solid #fff; border-width: 0 1.5px 1.5px 0;
        transform: rotate(45deg);
    }
    .batch__route-name { flex: 1; font-size: 13px; color: var(--color-text-primary); }
    .batch__route-tag {
        font-size: 11px; color: var(--color-text-secondary);
        background: var(--color-bg-muted); padding: 2px 8px; border-radius: var(--radius-sm);
        white-space: nowrap;
    }
</style>
<script type="text/javascript">
    $(document).ready(function()
    {
        setCurrentMenu('batch');

        function updateCount()
        {
            var n = $('#route-list .batch__route-checkbox:checked').length;
            $('#selected-count').text('已选 ' + n + ' 条');
        }
        updateCount();

        // 全选 / 清空
        $('#btn-select-all').click(function()
        {
            $('#route-list .batch__route-checkbox').prop('checked', true);
            updateCount();
        });
        $('#btn-clear-all').click(function()
        {
            $('#route-list .batch__route-checkbox').prop('checked', false);
            updateCount();
        });

        // 模式串预览
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

        // 提交
        $('#btn-run').click(function()
        {
            var idList = [];
            $('#route-list .batch__route-checkbox:checked').each(function()
            {
                idList.push(this.value);
            });
            if (idList.length === 0) idList = ['0'];

            var params = {
                routeIdList : idList,
                vehicleCount : $('#vehicleCount').val(),
                vehicleNumberPattern : $('#vehicleNumberPattern').val(),
                deviceSnPattern : $('#deviceSnPattern').val(),
                simNumberPattern : $('#simNumberPattern').val(),
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
