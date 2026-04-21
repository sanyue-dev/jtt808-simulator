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
        <div class="batch">
            <h2 class="batch__title">批量创建行程任务</h2>

            <!-- 线路选择 -->
            <div class="batch__section">
                <div class="batch__section-header">
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
            <div class="batch__section">
                <div class="batch__section-header">
                    <h3>车辆配置</h3>
                </div>
                <div class="batch__fields">
                    <div class="batch__field">
                        <label class="batch__label">车辆数量</label>
                        <input type="number" name="vehicleCount" id="vehicleCount" class="batch__input" min="1" max="100000" value="1000" />
                        <span class="batch__field-hint">最多 100,000 辆</span>
                    </div>
                    <div class="batch__field">
                        <label class="batch__label">车牌号规则</label>
                        <div class="batch__input-group">
                            <input type="text" name="vehicleNumberPattern" id="vehicleNumberPattern" class="batch__input" value="京%06d" />
                            <span class="batch__preview" id="preview-plate">京000001</span>
                        </div>
                    </div>
                    <div class="batch__field">
                        <label class="batch__label">终端ID规则</label>
                        <div class="batch__input-group">
                            <input type="text" name="deviceSnPattern" id="deviceSnPattern" class="batch__input" value="A%06d" />
                            <span class="batch__preview" id="preview-sn">A000001</span>
                        </div>
                    </div>
                    <div class="batch__field">
                        <label class="batch__label">SIM卡号规则</label>
                        <div class="batch__input-group">
                            <input type="text" name="simNumberPattern" id="simNumberPattern" class="batch__input" value="013800%06d" />
                            <span class="batch__preview" id="preview-sim">013800000001</span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 服务器配置 -->
            <div class="batch__section">
                <div class="batch__section-header">
                    <h3>服务器配置</h3>
                </div>
                <div class="batch__fields">
                    <div class="batch__field">
                        <label class="batch__label">808 网关地址</label>
                        <input type="text" name="serverAddress" id="serverAddress" class="batch__input" value="${vehicleServerAddr}" />
                    </div>
                    <div class="batch__field">
                        <label class="batch__label">端口</label>
                        <input type="text" name="serverPort" id="serverPort" class="batch__input" style="max-width: 140px;" value="${vehicleServerPort}" />
                    </div>
                </div>
            </div>

            <!-- 提交 -->
            <div class="batch__actions">
                <button class="btn btn--blue" id="btn-run" style="min-width: 120px; height: 38px; line-height: 38px; font-size: 15px;">创建任务</button>
            </div>
        </div>
    </div>
</div>
</body>
<#include "inc/footer.ftlh">
<style>
    .batch { padding: 28px 32px; max-width: 860px; }
    .batch__title { font-size: 20px; font-weight: 600; margin: 0 0 24px 0; color: var(--color-text-primary); }
    .batch__section {
        background: var(--color-bg-panel);
        border: 1px solid var(--color-border);
        border-radius: 10px;
        padding: 20px 24px;
        margin-bottom: 16px;
    }
    .batch__section-header {
        display: flex; align-items: center; justify-content: space-between;
        margin-bottom: 16px;
    }
    .batch__section-header h3 {
        font-size: 15px; font-weight: 600; margin: 0; color: var(--color-text-primary);
    }

    /* 工具栏 */
    .batch__route-toolbar { display: flex; align-items: center; gap: 12px; }
    .batch__toolbar-btn {
        background: none; border: none; cursor: pointer;
        font-size: 13px; color: var(--color-primary); padding: 2px 0;
    }
    .batch__toolbar-btn:hover { text-decoration: underline; }
    .batch__selected-count {
        font-size: 12px; color: var(--color-text-secondary);
        background: var(--color-bg-muted); padding: 2px 8px; border-radius: 10px;
    }

    .batch__route-hint {
        font-size: 13px; color: var(--color-text-secondary); margin: 0 0 10px 0;
    }

    /* 线路列表 */
    .batch__route-list {
        border: 1px solid var(--color-border); border-radius: 8px;
        max-height: 260px; overflow-y: auto; overflow-x: hidden;
    }
    .batch__route-item {
        display: flex; align-items: center; gap: 10px;
        padding: 9px 14px; cursor: pointer;
        border-bottom: 1px solid var(--color-border);
        transition: background .12s;
    }
    .batch__route-item:last-child { border-bottom: none; }
    .batch__route-item:hover { background: var(--color-bg-panel-muted); }
    .batch__route-item input { display: none; }
    .batch__route-checkmark {
        width: 16px; height: 16px; border-radius: 4px; flex-shrink: 0;
        border: 2px solid var(--color-border-strong);
        position: relative; transition: border-color .12s, background .12s;
    }
    .batch__route-item:hover .batch__route-checkmark { border-color: var(--color-primary); }
    .batch__route-item input:checked ~ .batch__route-checkmark {
        background: var(--color-primary); border-color: var(--color-primary);
    }
    .batch__route-item input:checked ~ .batch__route-checkmark::after {
        content: ''; position: absolute; top: 1px; left: 4px;
        width: 5px; height: 8px; border: solid #fff; border-width: 0 2px 2px 0;
        transform: rotate(45deg);
    }
    .batch__route-name { flex: 1; font-size: 14px; color: var(--color-text-primary); }
    .batch__route-tag {
        font-size: 12px; color: var(--color-text-secondary);
        background: var(--color-bg-muted); padding: 2px 8px; border-radius: 4px;
        white-space: nowrap;
    }

    /* 字段区 */
    .batch__fields { display: flex; flex-wrap: wrap; gap: 16px 32px; }
    .batch__field { display: flex; flex-direction: column; gap: 6px; min-width: 200px; flex: 1; }
    .batch__label { font-size: 13px; font-weight: 500; color: var(--color-text-secondary); }
    .batch__input {
        height: 36px; padding: 0 12px; width: 100%;
        border: 1px solid var(--color-border); border-radius: 6px;
        background: var(--color-bg-panel); color: var(--color-text-primary);
        box-sizing: border-box; font-size: 14px;
        transition: border-color .15s, box-shadow .15s;
    }
    .batch__input:focus {
        border-color: var(--color-primary); outline: none;
        box-shadow: 0 0 0 2px rgba(47, 128, 237, 0.18);
    }
    .batch__field-hint { font-size: 12px; color: var(--color-text-secondary); margin-top: -2px; }
    .batch__input-group { display: flex; align-items: center; gap: 12px; }
    .batch__input-group .batch__input { flex: 1; }
    .batch__preview {
        font-size: 13px; color: var(--color-primary); white-space: nowrap;
        font-family: Consolas, "Courier New", monospace;
        background: rgba(47, 128, 237, 0.08);
        padding: 6px 10px; border-radius: 6px; min-width: 100px; text-align: center;
    }
    .batch__actions { margin-top: 24px; padding-left: 4px; }
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
