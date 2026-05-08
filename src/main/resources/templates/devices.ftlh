<!DOCTYPE html>
<html lang="en">
<head>
<#include "inc/resource.ftlh">
    <title>设备库</title>
    <style>
        .table-toolbar {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 12px;
            margin-bottom: 12px;
        }
        .table-toolbar__filters,
        .table-toolbar__actions {
            display: flex;
            align-items: center;
            gap: 8px;
        }
        .table-toolbar__filters {
            flex: 1 1 auto;
            min-width: 0;
            flex-wrap: wrap;
        }
        .table-toolbar__actions {
            flex: 0 0 auto;
            margin-left: auto;
        }
        .table-toolbar .layui-btn {
            margin-left: 0;
            transition: none;
        }
        .table-toolbar .layui-input,
        .table-toolbar .layui-btn {
            height: 32px;
            line-height: 32px;
        }
        .table-toolbar__filters .layui-input-inline { width: 300px; margin-right: 0; }
        @media (max-width: 768px) {
            .table-toolbar {
                align-items: stretch;
                flex-direction: column;
            }
            .table-toolbar__actions {
                margin-left: 0;
            }
            .table-toolbar__actions .layui-btn {
                width: 100%;
            }
        }
        .device-form { padding: 18px 22px 0 0; }
        .device-form__remark {
            grid-column: 1 / -1;
            margin-top: 6px;
            border-top: 1px solid var(--color-border);
            padding-top: 16px;
        }
        .device-actions {
            display: inline-flex;
            align-items: center;
            gap: 12px;
            white-space: nowrap;
        }
        .device-action-link {
            color: var(--color-primary);
            line-height: 22px;
        }
        .device-action-link:hover {
            color: var(--color-primary);
            text-decoration: underline;
        }
        .device-action-danger,
        .device-action-danger:hover {
            color: var(--app-danger);
        }
    </style>
</head>
<body>
<div class="layui-layout layui-layout-admin app-layout">
<#include "inc/sidebar.ftlh">
    <div class="layui-body app-body">
        <div class="app-page">
            <div class="page-header">
                <h2>设备库</h2>
            </div>
            <div class="page-body">
                <div class="table-toolbar">
                    <form class="layui-form table-toolbar__filters" id="device-filter">
                        <div class="layui-input-inline">
                            <input class="layui-input" type="text" name="keyword" placeholder="设备名称 / 车牌号 / SIM / 终端ID" />
                        </div>
                        <button class="layui-btn layui-btn-sm" type="submit"><i class="layui-icon layui-icon-search"></i>筛选</button>
                        <button class="layui-btn layui-btn-primary layui-btn-sm" type="button" id="btn-clear-filter">清空</button>
                    </form>
                    <div class="table-toolbar__actions">
                        <button type="button" class="layui-btn layui-btn-sm" id="btn-create-device"><i class="layui-icon layui-icon-add-1"></i>新增设备</button>
                    </div>
                </div>
                <div class="app-table-panel">
                    <table id="device-table" lay-filter="device-table"></table>
                </div>
            </div>
        </div>
    </div>
</div>

<div id="device-form-panel" style="display:none;">
    <form class="layui-form device-form" id="device-form" lay-filter="device-form">
        <input type="hidden" name="id" id="device-id" />
        <div class="form-grid">
            <div class="layui-form-item">
                <label class="layui-form-label">设备名称</label>
                <div class="layui-input-block"><input type="text" name="name" id="device-name" class="layui-input" /></div>
            </div>
            <div class="layui-form-item">
                <label class="layui-form-label">车牌号</label>
                <div class="layui-input-block"><input type="text" name="vehicleNumber" id="vehicle-number" class="layui-input" /></div>
            </div>
            <div class="layui-form-item">
                <label class="layui-form-label">终端ID</label>
                <div class="layui-input-block"><input type="text" name="deviceSn" id="device-sn" class="layui-input" /></div>
            </div>
            <div class="layui-form-item">
                <label class="layui-form-label">SIM卡号</label>
                <div class="layui-input-block"><input type="text" name="simNumber" id="sim-number" class="layui-input" /></div>
            </div>
            <div class="layui-form-item">
                <label class="layui-form-label">上线方式</label>
                <div class="layui-input-block">
                    <select name="authMode" id="auth-mode">
                        <option value="direct">直接鉴权</option>
                        <option value="register">注册后鉴权</option>
                    </select>
                </div>
            </div>
            <div class="layui-form-item">
                <label class="layui-form-label">鉴权码</label>
                <div class="layui-input-block"><input type="text" name="authToken" id="auth-token" class="layui-input" /></div>
            </div>
            <div class="layui-form-item">
                <label class="layui-form-label">默认线路</label>
                <div class="layui-input-block">
                    <select name="defaultRouteId" id="default-route-id" lay-search>
                        <option value="">不指定</option>
                        <#list routes as item>
                        <option value="${item.id}">${item.name} - ${item.kilometers!'NaN'}公里</option>
                        </#list>
                    </select>
                </div>
            </div>
            <div class="layui-form-item">
                <label class="layui-form-label">初始里程</label>
                <div class="layui-input-block"><input type="number" name="initialMileage" id="initial-mileage" class="layui-input" min="0" value="0" /></div>
            </div>
            <div class="layui-form-item">
                <label class="layui-form-label">网关地址</label>
                <div class="layui-input-block"><input type="text" name="serverAddress" id="server-address" class="layui-input" /></div>
            </div>
            <div class="layui-form-item">
                <label class="layui-form-label">端口</label>
                <div class="layui-input-block"><input type="text" name="serverPort" id="server-port" class="layui-input" /></div>
            </div>
            <div class="layui-form-item device-form__remark">
                <label class="layui-form-label">备注</label>
                <div class="layui-input-block"><textarea name="remark" id="remark" class="layui-textarea"></textarea></div>
            </div>
        </div>
    </form>
</div>
</body>
<#include "inc/footer.ftlh">
<script type="text/javascript">
    layui.use('form', function() { layui.form.render(); });
    $(document).ready(function()
    {
        setCurrentMenu('device');

        function filterParams()
        {
            var values = {};
            $('#device-filter').serializeArray().forEach(function(item)
            {
                values[item.name] = item.value;
            });
            return values;
        }

        function reloadTable(resetPage)
        {
            var options = { where: filterParams() };
            if (resetPage) options.page = { curr: 1 };
            layui.table.reload('device-table', options);
        }

        layui.use(['table', 'form'], function()
        {
            var table = layui.table;
            var form = layui.form;
            window.deviceTable = table.render($.extend(true, request.table('/device/list'), {
                elem: '#device-table',
                page: { layout: ['prev', 'page', 'next', 'count', 'limit'] },
                limit: 20,
                where: filterParams(),
                done: function()
                {
                    form.render('checkbox');
                },
                cols: [[
                    { field: 'name', title: '设备名称', align: 'left', templet: function(r) { return escapeHtml(r.name); } },
                    { field: 'vehicleNumber', title: '车牌号', align: 'left', width: 120, templet: function(r) { return escapeHtml(r.vehicleNumber); } },
                    { field: 'deviceSn', title: '终端ID', align: 'left', templet: function(r) { return escapeHtml(r.deviceSn); } },
                    { field: 'simNumber', title: 'SIM卡号', align: 'left', templet: function(r) { return escapeHtml(r.simNumber); } },
                    { field: 'authMode', title: '上线方式', align: 'left', width: 110, templet: function(r) { return r.authMode === 'register' ? '注册后鉴权' : '直接鉴权'; } },
                    { field: 'serverAddress', title: '网关', align: 'left', templet: function(r) { return escapeHtml(r.serverAddress) + ':' + escapeHtml(r.serverPort); } },
                    {
                        field: 'enabled',
                        title: '状态',
                        align: 'left',
                        width: 120,
                        templet: function(r)
                        {
                            return '<input type="checkbox" name="enabled" title="启用|停用" lay-skin="switch" lay-filter="device-status" data-id="' + r.id + '"' + (r.enabled === 0 ? '' : ' checked') + '>';
                        }
                    },
                    {
                        title: '操作',
                        align: 'left',
                        width: 120,
                        templet: function()
                        {
                            var html = '<span class="device-actions">';
                            html += '<a href="javascript:;" lay-event="edit" class="device-action-link">编辑</a>';
                            html += '<a href="javascript:;" lay-event="remove" class="device-action-link device-action-danger">删除</a>';
                            html += '</span>';
                            return html;
                        }
                    }
                ]]
            }));

            table.on('tool(device-table)', function(obj)
            {
                if (obj.event === 'edit') openDeviceForm(obj.data);
                if (obj.event === 'remove') removeDevice(obj.data.id);
            });
        });

        $('#device-filter').submit(function()
        {
            reloadTable(true);
            return false;
        });

        $('#btn-clear-filter').click(function()
        {
            $('#device-filter').get(0).reset();
            reloadTable(true);
        });

        $('#btn-create-device').click(function()
        {
            openDeviceForm(null);
        });

        layui.form.on('switch(device-status)', function(data)
        {
            var checkbox = data.elem;
            request.post('/device/status', { id: $(checkbox).attr('data-id'), enabled: checkbox.checked ? 1 : 0 })
                .fail(function()
            {
                checkbox.checked = !checkbox.checked;
                layui.form.render('checkbox');
            });
        });

        function removeDevice(id)
        {
            appConfirm('确定删除这个设备档案？', function()
            {
                request.post('/device/remove', { id: id }).done(function()
                {
                    appNotify('success', '删除成功');
                    reloadTable();
                });
            });
        }

        function openDeviceForm(row)
        {
            $('#device-form').get(0).reset();
            $('#device-id').val(row ? row.id : '');
            $('#device-name').val(row ? row.name : '');
            $('#vehicle-number').val(row ? row.vehicleNumber : '');
            $('#device-sn').val(row ? row.deviceSn : '');
            $('#sim-number').val(row ? row.simNumber : '');
            $('#auth-mode').val(row ? row.authMode : 'direct');
            $('#auth-token').val(row ? row.authToken : '');
            $('#default-route-id').val(row && row.defaultRouteId ? row.defaultRouteId : '');
            $('#initial-mileage').val(row && row.initialMileage != null ? row.initialMileage : 0);
            $('#server-address').val(row ? row.serverAddress : '127.0.0.1');
            $('#server-port').val(row ? row.serverPort : '20021');
            $('#remark').val(row ? row.remark : '');
            layui.form.render();

            layui.layer.open({
                type: 1,
                title: row ? '编辑设备' : '新增设备',
                area: ['760px', '620px'],
                content: $('#device-form-panel'),
                btn: ['保存', '取消'],
                yes: function(index)
                {
                    request.post('/device/save', $('#device-form').serialize()).done(function()
                    {
                        layui.layer.close(index);
                        appNotify('success', '保存成功');
                        reloadTable();
                    });
                }
            });
        }
    });
</script>
</html>
