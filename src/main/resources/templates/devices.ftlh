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
                    <div id="device-table" class="layui-form"></div>
                    <div id="device-page" class="app-pagination"></div>
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

        window.deviceTable = appTable.render({
            elem: '#device-table',
            pageElem: '#device-page',
            url: '/device/list',
            form: $('#device-filter'),
            fields: [
                { name: 'name', title: '设备名称', align: 'left', formatter: function(i, v) { return escapeHtml(v); } },
                { name: 'vehicleNumber', title: '车牌号', align: 'left', width: '120px', formatter: function(i, v) { return escapeHtml(v); } },
                { name: 'deviceSn', title: '终端ID', align: 'left', formatter: function(i, v) { return escapeHtml(v); } },
                { name: 'simNumber', title: 'SIM卡号', align: 'left', formatter: function(i, v) { return escapeHtml(v); } },
                { name: 'authMode', title: '上线方式', align: 'left', width: '110px', formatter: function(i, v) { return v === 'register' ? '注册后鉴权' : '直接鉴权'; } },
                { name: 'serverAddress', title: '网关', align: 'left', formatter: function(i, v, r) { return escapeHtml(v) + ':' + escapeHtml(r.serverPort); } },
                {
                    name: 'enabled',
                    title: '状态',
                    align: 'left',
                    width: '120px',
                    formatter: function(i, v, r)
                    {
                        return '<input type="checkbox" name="enabled" title="启用|停用" lay-skin="switch" lay-filter="device-status" data-id="' + r.id + '"' + (v === 0 ? '' : ' checked') + '>';
                    }
                },
                {
                    name: 'id',
                    title: '操作',
                    align: 'left',
                    width: '96px',
                    formatter: function(i, v, r)
                    {
                        var row = encodeURIComponent(JSON.stringify(r));
                        var html = '<span class="device-actions">';
                        html += '<a href="javascript:;" data-row="' + row + '" class="device-action-link btn-edit">编辑</a>';
                        html += '<a href="javascript:;" data-id="' + v + '" class="device-action-link device-action-danger btn-remove">删除</a>';
                        html += '</span>';
                        return html;
                    }
                }
            ]
        });

        $('#btn-clear-filter').click(function()
        {
            $('#device-filter').get(0).reset();
            window.deviceTable.reload();
        });

        $('#btn-create-device').click(function()
        {
            openDeviceForm(null);
        });

        $('#device-table').on('click', '.btn-edit', function()
        {
            openDeviceForm(JSON.parse(decodeURIComponent($(this).attr('data-row'))));
        });

        layui.form.on('switch(device-status)', function(data)
        {
            var checkbox = data.elem;
            $.post('/device/status', { id: $(checkbox).attr('data-id'), enabled: checkbox.checked ? 1 : 0 }, function(result)
            {
                if (result.error && result.error.code)
                {
                    checkbox.checked = !checkbox.checked;
                    layui.form.render('checkbox');
                    return appNotify('error', result.error.reason);
                }
            });
        });

        $('#device-table').on('click', '.btn-remove', function()
        {
            var id = $(this).attr('data-id');
            appConfirm('确定删除这个设备档案？', function()
            {
                $.post('/device/remove', { id: id }, function(result)
                {
                    if (result.error && result.error.code) return appNotify('error', result.error.reason);
                    appNotify('success', '删除成功');
                    window.deviceTable.reload();
                });
            });
        });

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
                    $.post('/device/save', $('#device-form').serialize(), function(result)
                    {
                        if (result.error && result.error.code) return appNotify('error', result.error.reason);
                        layui.layer.close(index);
                        appNotify('success', '保存成功');
                        window.deviceTable.reload();
                    });
                }
            });
        }
    });
</script>
</html>
