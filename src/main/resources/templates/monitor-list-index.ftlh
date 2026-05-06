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
                <form class="monitor-toolbar" id="monitor-filter">
                    <input type="hidden" name="taskGroupId" value="" />
                    <div class="task__dropdown task__dropdown--compact monitor-toolbar__dropdown is-unselected" id="stateDropdown">
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
                <div class="monitor-filter-context" id="task-group-filter-context">
                    <span>当前按 Task Group 过滤：</span>
                    <strong id="task-group-filter-value"></strong>
                    <a id="task-group-filter-link" href="/task-groups/monitor">查看任务组监控</a>
                </div>
                <div id="route-table"></div>
                <ul class="pagination"></ul>
            </div>
        </div>
    </div>
</div>
</body>
<#include "inc/footer.ftlh">
<style>
    .monitor-toolbar {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 12px;
        flex-wrap: wrap;
    }
    .monitor-toolbar__input {
        width: 220px;
        height: 32px;
        border: 1px solid var(--color-border);
        background: var(--color-bg-panel);
        padding: 0 12px;
        font-size: 13px;
    }
    .monitor-toolbar__dropdown {
        width: 140px;
    }
    .monitor-filter-context {
        display: none;
        align-items: center;
        gap: 8px;
        margin: -2px 0 12px 0;
        padding: 8px 12px;
        background: var(--color-bg-panel);
        border: 1px solid var(--color-border);
        color: var(--color-text-secondary);
        font-size: 12px;
    }
    .monitor-filter-context strong {
        color: var(--color-text-primary);
        font-size: 12px;
        font-weight: 600;
    }
    .monitor-filter-context a {
        color: var(--color-primary);
        font-size: 12px;
    }
</style>
<script type="text/javascript">
    $(document).ready(function()
    {
        setCurrentMenu('list-monitor');
        var initialTaskGroupId = new URLSearchParams(window.location.search).get('taskGroupId') || '';
        $('#monitor-filter input[name="taskGroupId"]').val(initialTaskGroupId);
        if (initialTaskGroupId)
        {
            $('#task-group-filter-value').text(initialTaskGroupId);
            $('#task-group-filter-link').attr('href', '/task-groups/monitor?taskGroupId=' + encodeURIComponent(initialTaskGroupId));
            $('#task-group-filter-context').css('display', 'flex');
        }

        function reloadTable()
        {
            $('#route-table').paginate('reload');
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
                    name: 'taskGroupDisplayName',
                    title: '任务组',
                    align: 'left',
                    formatter: function(i, v, r)
                    {
                        return v ? escapeHtml(v) : '--';
                    }
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
            $('#monitor-filter input[name="taskGroupId"]').val('');
            $('#task-group-filter-context').hide();
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
