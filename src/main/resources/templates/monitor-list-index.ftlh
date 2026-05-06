<!DOCTYPE html>
<html lang="en">
<head>
<#include "inc/resource.ftlh">
    <title>行程任务</title>
</head>
<body>
<div class="layout">
<#include "inc/sidebar.ftlh">
    <div class="layout__content">
        <div class="layout__page">
            <div class="page-header">
                <h2>行程任务</h2>
            </div>
            <div class="page-body">
                <div class="monitor-summary" id="monitor-summary">
                    <div class="monitor-summary__item"><span>总任务数</span><strong data-field="totalTasks">0</strong></div>
                    <div class="monitor-summary__item"><span>未终止</span><strong data-field="activeTasks">0</strong></div>
                    <div class="monitor-summary__item"><span>停车中</span><strong data-field="parkingTasks">0</strong></div>
                    <div class="monitor-summary__item"><span>已终止</span><strong data-field="terminatedTasks">0</strong></div>
                    <div class="monitor-summary__item"><span>连接成功</span><strong data-field="connectionSucceeded">0</strong></div>
                    <div class="monitor-summary__item"><span>连接失败</span><strong data-field="connectionFailed">0</strong></div>
                    <div class="monitor-summary__item"><span>注册成功</span><strong data-field="registrationSucceeded">0</strong></div>
                    <div class="monitor-summary__item"><span>注册失败</span><strong data-field="registrationFailed">0</strong></div>
                    <div class="monitor-summary__item"><span>鉴权成功</span><strong data-field="authenticationSucceeded">0</strong></div>
                    <div class="monitor-summary__item"><span>鉴权失败</span><strong data-field="authenticationFailed">0</strong></div>
                    <div class="monitor-summary__item"><span>位置上报</span><strong data-field="locationReportSent">0</strong></div>
                    <div class="monitor-summary__item"><span>上报速率/s</span><strong data-field="locationReportRate">0</strong></div>
                    <div class="monitor-summary__item"><span>断连</span><strong data-field="disconnected">0</strong></div>
                    <div class="monitor-summary__item"><span>终止事件</span><strong data-field="terminated">0</strong></div>
                    <div class="monitor-summary__item"><span>发送失败</span><strong data-field="sendFailed">0</strong></div>
                    <div class="monitor-summary__item"><span>协议异常</span><strong data-field="protocolExceptions">0</strong></div>
                    <div class="monitor-summary__item"><span>Heap 使用</span><strong data-field="runtimeResources.heapUsedBytes" data-format="bytes">0</strong></div>
                    <div class="monitor-summary__item"><span>Heap 上限</span><strong data-field="runtimeResources.heapMaxBytes" data-format="bytes">0</strong></div>
                    <div class="monitor-summary__item"><span>线程数</span><strong data-field="runtimeResources.threadCount">0</strong></div>
                    <div class="monitor-summary__item"><span>FD 使用</span><strong data-field="runtimeResources.openFileDescriptorCount">0</strong></div>
                    <div class="monitor-summary__item"><span>FD 上限</span><strong data-field="runtimeResources.maxFileDescriptorCount">0</strong></div>
                    <div class="monitor-summary__item"><span>调度延迟均值</span><strong data-field="schedulerDelay.averageDelayMillis" data-format="millis">0</strong></div>
                    <div class="monitor-summary__item"><span>调度延迟最大</span><strong data-field="schedulerDelay.maxDelayMillis" data-format="millis">0</strong></div>
                </div>

                <form class="monitor-toolbar" id="monitor-filter">
                    <div class="task__dropdown monitor-toolbar__dropdown is-unselected" id="stateDropdown">
                        <input type="hidden" name="state" value="" />
                        <div class="task__dropdown-trigger" id="stateDropdownTrigger">
                            <span class="task__dropdown-text">全部状态</span>
                            <span class="task__dropdown-arrow"><i class="fa fa-angle-down"></i></span>
                        </div>
                        <div class="task__dropdown-menu" id="stateDropdownMenu">
                            <div class="task__dropdown-option is-selected" data-value="">全部状态</div>
                            <div class="task__dropdown-option" data-value="空闲">空闲</div>
                            <div class="task__dropdown-option" data-value="行驶中">行驶中</div>
                            <div class="task__dropdown-option" data-value="停车中">停车中</div>
                            <div class="task__dropdown-option" data-value="行驶终止">行驶终止</div>
                        </div>
                    </div>
                    <input class="monitor-toolbar__input" type="text" name="keyword" placeholder="车牌号 / SIM / 终端ID" />
                    <button class="btn btn--sm btn--blue" type="submit">筛选</button>
                    <button class="btn btn--sm btn--gray" type="button" id="btn-clear-filter">清空</button>
                    <button class="btn btn--sm btn--red" type="button" id="btn-terminate-all">停止全部未终止任务</button>
                </form>
                <div id="route-table"></div>
                <ul class="pagination"></ul>
            </div>
        </div>
    </div>
</div>
</body>
<#include "inc/footer.ftlh">
<style>
    .monitor-summary {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
        gap: 8px;
        margin-bottom: 14px;
    }
    .monitor-summary__item {
        border: 1px solid var(--color-border);
        background: var(--color-bg-panel);
        padding: 10px 12px;
    }
    .monitor-summary__item span {
        display: block;
        color: var(--color-text-secondary);
        font-size: 12px;
        margin-bottom: 4px;
    }
    .monitor-summary__item strong {
        color: var(--color-text-primary);
        font-size: 20px;
        font-weight: 600;
    }
    .monitor-toolbar {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 12px;
        flex-wrap: wrap;
    }
    .monitor-toolbar__input {
        height: 32px;
        border: 1px solid var(--color-border);
        background: var(--color-bg-panel);
        color: var(--color-text-primary);
        padding: 0 10px;
        font-size: 13px;
    }
    .monitor-toolbar__dropdown {
        position: relative;
        width: 140px;
    }
    .monitor-toolbar__dropdown .task__dropdown-trigger {
        height: 32px;
        width: 100%;
        padding: 0 32px 0 10px;
        box-sizing: border-box;
        border: 1px solid var(--color-border);
        border-radius: var(--radius-sm);
        background: var(--color-bg-panel);
        color: var(--color-text-primary);
        font-size: 13px;
        cursor: pointer;
        display: flex;
        align-items: center;
        transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
    }
    .monitor-toolbar__dropdown .task__dropdown-trigger:hover { border-color: var(--color-border-strong); }
    .monitor-toolbar__dropdown.is-open .task__dropdown-trigger {
        border-color: var(--color-text-primary);
        border-radius: var(--radius-sm) var(--radius-sm) 0 0;
    }
    .monitor-toolbar__dropdown .task__dropdown-text {
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }
    .monitor-toolbar__dropdown.is-unselected .task__dropdown-text {
        color: var(--color-text-secondary);
        opacity: .8;
    }
    .monitor-toolbar__dropdown .task__dropdown-arrow {
        position: absolute;
        right: 10px;
        top: 50%;
        transform: translateY(-50%);
        color: var(--color-text-secondary);
        font-size: 12px;
        transition: transform var(--transition-fast);
        pointer-events: none;
    }
    .monitor-toolbar__dropdown.is-open .task__dropdown-arrow { transform: translateY(-50%) rotate(180deg); }
    .monitor-toolbar__dropdown .task__dropdown-menu {
        position: absolute;
        left: 0;
        right: 0;
        top: 100%;
        opacity: 0;
        visibility: hidden;
        max-height: 220px;
        overflow-y: auto;
        background: var(--color-bg-panel);
        border: 1px solid var(--color-text-primary);
        border-top: none;
        box-sizing: border-box;
        border-radius: 0 0 var(--radius-sm) var(--radius-sm);
        z-index: 100;
        box-shadow: 0 4px 12px rgba(0,0,0,.08);
        transition: opacity .15s ease, visibility .15s ease;
    }
    .monitor-toolbar__dropdown.is-open .task__dropdown-menu {
        opacity: 1;
        visibility: visible;
    }
    .monitor-toolbar__dropdown .task__dropdown-option {
        padding: 8px 10px;
        font-size: 13px;
        color: var(--color-text-primary);
        cursor: pointer;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        transition: background var(--transition-fast), color var(--transition-fast);
    }
    .monitor-toolbar__dropdown .task__dropdown-option:hover { background: var(--color-bg-sidebar-hover); }
    .monitor-toolbar__dropdown .task__dropdown-option.is-selected {
        background: var(--color-bg-sidebar-active);
        color: var(--color-text-inverse);
    }
    .monitor-toolbar__dropdown .task__dropdown-option.is-selected:hover { background: var(--color-primary-hover); }
    .monitor-toolbar__input {
        width: 220px;
    }
</style>
<script type="text/javascript">
    $(document).ready(function()
    {
        setCurrentMenu('list-monitor');

        function loadSummary()
        {
            $.post('/monitor/list/summary', function(result)
            {
                if (result.error && result.error.code) return toastr('error', result.error.reason);
                var data = result.data || {};
                $('#monitor-summary [data-field]').each(function()
                {
                    var field = $(this).attr('data-field');
                    var value = getValue(data, field);
                    if (field === 'locationReportRate') value = Number(value).toFixed(2);
                    if ($(this).attr('data-format') === 'bytes') value = formatBytes(value);
                    if ($(this).attr('data-format') === 'millis') value = Number(value || 0).toFixed(2) + ' ms';
                    $(this).text(value);
                });
            });
        }

        function getValue(data, path)
        {
            var node = data;
            var parts = path.split('.');
            for (var i = 0; i < parts.length; i++)
            {
                if (node == null) return 0;
                node = node[parts[i]];
            }
            return node == null ? 0 : node;
        }

        function formatBytes(value)
        {
            var n = Number(value || 0);
            if (n < 0) return 'N/A';
            if (n < 1024) return n + ' B';
            if (n < 1024 * 1024) return (n / 1024).toFixed(1) + ' KB';
            if (n < 1024 * 1024 * 1024) return (n / 1024 / 1024).toFixed(1) + ' MB';
            return (n / 1024 / 1024 / 1024).toFixed(1) + ' GB';
        }

        function reloadTable()
        {
            $('#route-table').paginate('reload');
            loadSummary();
        }

        function escapeHtml(value)
        {
            return $('<div>').text(value == null ? '' : String(value)).html();
        }

        function selectStateOption($opt)
        {
            var $dd = $('#stateDropdown');
            var value = $opt.attr('data-value') || '';
            $('#stateDropdownMenu .task__dropdown-option').removeClass('is-selected');
            $opt.addClass('is-selected');
            $dd.find('input[name="state"]').val(value);
            $dd.find('.task__dropdown-text').text($opt.text());
            $dd.toggleClass('is-unselected', value === '');
            $dd.removeClass('is-open');
        }

        function resetStateDropdown()
        {
            selectStateOption($('#stateDropdownMenu .task__dropdown-option').first());
        }

        $('#stateDropdown').attr('tabindex', '0');
        $('#stateDropdownTrigger').click(function(e)
        {
            e.stopPropagation();
            $('#stateDropdown').toggleClass('is-open');
        });
        $('#stateDropdownMenu').on('click', '.task__dropdown-option', function(e)
        {
            e.stopPropagation();
            selectStateOption($(this));
        });
        $('#stateDropdown').on('keydown', function(e)
        {
            var $dd = $('#stateDropdown');
            var $opts = $('#stateDropdownMenu .task__dropdown-option:visible');
            var idx = $opts.index($opts.filter('.is-selected'));
            if (e.key === 'Escape') { $dd.removeClass('is-open'); return; }
            if (!$dd.hasClass('is-open'))
            {
                if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); $dd.addClass('is-open'); }
                return;
            }
            if (e.key === 'ArrowDown')
            {
                e.preventDefault();
                idx = idx < $opts.length - 1 ? idx + 1 : 0;
                selectStateOption($opts.eq(idx));
            }
            else if (e.key === 'ArrowUp')
            {
                e.preventDefault();
                idx = idx > 0 ? idx - 1 : $opts.length - 1;
                selectStateOption($opts.eq(idx));
            }
            else if (e.key === 'Enter' || e.key === ' ')
            {
                e.preventDefault();
                $dd.removeClass('is-open');
            }
        });
        $(document).click(function() { $('#stateDropdown').removeClass('is-open'); });

        loadSummary();

        $('#route-table').paginate({
            url: '/monitor/list/json',
            form: $('#monitor-filter'),
            fields: [
                {
                    name: 'state',
                    title: '状态',
                    align: 'center',
                },
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
                    name: 'failureStage',
                    title: '失败阶段',
                    align: 'center',
                    formatter: function(i, v, r)
                    {
                        return v ? escapeHtml(v) : '--';
                    }
                },
                {
                    name: 'failureReason',
                    title: '失败原因',
                    align: 'left',
                    formatter: function(i, v, r)
                    {
                        return v ? escapeHtml(v) : '--';
                    }
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
                        html += '<a href="/monitor/view?id=' + v + '" target="_blank" class="btn btn--sm btn--blue">详情</a>';
                        if (r.state !== '行驶终止') html += ' <a href="javascript:;" data-id="' + v + '" class="btn btn--sm btn--gray btn-terminate">停止</a>';
                        return html;
                    }
                }
            ]
        });

        $('#btn-clear-filter').click(function()
        {
            $('#monitor-filter').get(0).reset();
            resetStateDropdown();
            reloadTable();
        });

        $('#route-table').on('click', '.btn-terminate', function()
        {
            var id = $(this).attr('data-id');
            confirmDialog('确定停止这个行程任务？', function()
            {
                $.post('/monitor/list/terminate', { id: id }, function(result)
                {
                    if (result.error && result.error.code) return toastr('error', result.error.reason);
                    toastr('success', '已提交停止任务');
                    reloadTable();
                });
            });
        });

        $('#btn-terminate-all').click(function()
        {
            confirmDialog('确定停止全部未终止的行程任务？', function()
            {
                $.post('/monitor/list/terminate-all', {}, function(result)
                {
                    if (result.error && result.error.code) return toastr('error', result.error.reason);
                    var data = result.data || {};
                    var message = '停止提交完成：成功 ' + (data.succeeded || 0) + ' 个，失败 ' + (data.failed || 0) + ' 个';
                    toastr(data.failed ? 'error' : 'success', message);
                    reloadTable();
                });
            });
        });
    });
</script>
</html>
