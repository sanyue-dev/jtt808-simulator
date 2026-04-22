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
                <div class="task__dropdown" id="routeDropdown" data-name="routeId">
                    <div class="task__dropdown-trigger" id="routeDropdownTrigger">
                        <span class="task__dropdown-text">请选择行驶线路</span>
                        <span class="task__dropdown-arrow"><i class="fa fa-angle-down"></i></span>
                    </div>
                    <div class="task__dropdown-menu" id="routeDropdownMenu">
                        <#list routes as item>
                        <div class="task__dropdown-option" data-value="${item.id}">${item.name} - ${item.kilometers!'NaN'}公里</div>
                        </#list>
                    </div>
                    <input type="hidden" name="routeId" id="routeId" value="0" />
                </div>
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
    /* ── custom dropdown ── */
    .task__dropdown { position: relative; width: 100%; }
    .task__dropdown-trigger {
        height: 40px; width: 100%; padding: 0 36px 0 14px;
        box-sizing: border-box;
        border: 1px solid var(--color-border); border-radius: var(--radius-sm);
        background: var(--color-bg-panel); color: var(--color-text-primary);
        font-size: 13px; cursor: pointer;
        display: flex; align-items: center;
        transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
    }
    .task__dropdown-trigger:hover { border-color: var(--color-border-strong); }
    .task__dropdown.is-open .task__dropdown-trigger {
        border-color: var(--color-text-primary);
        border-radius: var(--radius-sm) var(--radius-sm) 0 0;
    }
    .task__dropdown-text {
        flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
    }
    .task__dropdown.is-unselected .task__dropdown-text { color: var(--color-text-secondary); opacity: .6; }
    .task__dropdown-arrow {
        position: absolute; right: 12px; top: 50%; transform: translateY(-50%);
        color: var(--color-text-secondary); font-size: 12px;
        transition: transform var(--transition-fast);
        pointer-events: none;
    }
    .task__dropdown.is-open .task__dropdown-arrow { transform: translateY(-50%) rotate(180deg); }

    .task__dropdown-menu {
        position: absolute; left: 0; right: 0; top: 100%;
        opacity: 0; visibility: hidden;
        max-height: 240px; overflow-y: auto;
        background: var(--color-bg-panel);
        border: 1px solid var(--color-text-primary); border-top: none;
        box-sizing: border-box;
        border-radius: 0 0 var(--radius-sm) var(--radius-sm);
        z-index: 100;
        box-shadow: 0 4px 12px rgba(0,0,0,.08);
        transition: opacity .15s ease, visibility .15s ease;
    }
    .task__dropdown.is-open .task__dropdown-menu {
        opacity: 1; visibility: visible;
    }
    .task__dropdown-option {
        padding: 9px 14px; font-size: 13px; color: var(--color-text-primary);
        cursor: pointer; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
        transition: background var(--transition-fast), color var(--transition-fast);
    }
    .task__dropdown-option:hover { background: var(--color-bg-sidebar-hover); }
    .task__dropdown-option.is-selected {
        background: var(--color-bg-sidebar-active); color: var(--color-text-inverse);
    }
    .task__dropdown-option.is-selected:hover { background: var(--color-primary-hover); }

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

        // ── custom dropdown logic ──
        (function()
        {
            var $dd = $('#routeDropdown');
            var $trigger = $('#routeDropdownTrigger');
            var $menu = $('#routeDropdownMenu');
            var $input = $dd.find('input[name="routeId"]');
            var $text = $dd.find('.task__dropdown-text');

            $dd.addClass('is-unselected');

            function open()
            {
                $dd.addClass('is-open');
                $menu.find('.task__dropdown-option').first().focus();
            }
            function close()
            {
                $dd.removeClass('is-open');
            }
            function selectOption($opt)
            {
                $menu.find('.task__dropdown-option').removeClass('is-selected');
                $opt.addClass('is-selected');
                $input.val($opt.data('value'));
                $text.text($opt.text());
                $dd.removeClass('is-unselected');
                close();
            }

            $trigger.on('click', function(e)
            {
                e.stopPropagation();
                if ($dd.hasClass('is-open')) close(); else open();
            });

            $menu.on('click', '.task__dropdown-option', function(e)
            {
                e.stopPropagation();
                selectOption($(this));
            });

            $(document).on('click', function() { close(); });

            // keyboard support
            $dd.on('keydown', function(e)
            {
                var $opts = $menu.find('.task__dropdown-option:visible');
                var idx = $opts.index($opts.filter('.is-selected'));
                if (e.key === 'Escape') { close(); return; }
                if (!$dd.hasClass('is-open'))
                {
                    if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); open(); }
                    return;
                }
                if (e.key === 'ArrowDown')
                {
                    e.preventDefault();
                    idx = idx < $opts.length - 1 ? idx + 1 : 0;
                    selectOption($opts.eq(idx));
                }
                else if (e.key === 'ArrowUp')
                {
                    e.preventDefault();
                    idx = idx > 0 ? idx - 1 : $opts.length - 1;
                    selectOption($opts.eq(idx));
                }
                else if (e.key === 'Enter' || e.key === ' ')
                {
                    e.preventDefault();
                    close();
                }
            });

            // make dropdown focusable
            $dd.attr('tabindex', '0');
        })();

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
