<!DOCTYPE html>
<html lang="en">
<head>
<#include "inc/resource.ftlh">
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" crossorigin="" />
    <title>行程任务详情</title>
    <style type="text/css">
        body { overflow: hidden; }
        .app-body
        {
            position: relative;
            padding: 0 !important;
        }
        .monitor__map
        {
            position: absolute;
            left: 0px;
            top: 0px;
            right: 350px;
            bottom: 300px;
            background-color: var(--color-bg-page);
            width: auto;
            height: auto;
            z-index: 0;
        }
        .monitor__log-panel
        {
            position: absolute;
            left: 0px;
            bottom: 0px;
            width: 100%;
            height: 300px;
            border-top: solid 1px var(--color-border);
            z-index: 3;
            background-color: var(--color-bg-panel);
            box-shadow: none;
        }
        .monitor__info-panel
        {
            position: absolute;
            right: 0px;
            top: 0px;
            width: 350px;
            height: 100%;
            border-left: solid 1px var(--color-border);
            z-index: 2;
            background-color: var(--color-bg-panel);
            padding-bottom: 300px;
            box-shadow: none;
        }
        .monitor__panel
        {
            position: relative;
            padding: 50px 10px 10px 10px;
            width: 100%;
            height: 100%;
        }
        .monitor__panel > h3
        {
            height: 40px;
            border-bottom: solid 1px var(--color-border);
            line-height: 40px;
            font-size: 18px;
            margin: 0px;
            padding: 0px 0px 0px 10px;
            position: absolute;
            top: 0px;
            left: 0px;
            width: 100%;
            background-color: var(--color-bg-panel-muted);
            color: var(--color-text-primary);
        }
        .monitor__panel > h3 > button
        {
            float: right;
            margin: 8px 18px 0px 0px;
        }
        .monitor__panel-body
        {
            width: 100%;
            height: 100%;
            overflow-y: auto;
            overflow-x: hidden;
        }
        .monitor__panel-body hr
        {
            border-width: 1px 0px 0px 0px;
            border-color: var(--color-border);
        }
        .monitor-detail-row {
            display: grid;
            grid-template-columns: 30% 70%;
            min-height: 24px;
            line-height: 24px;
            color: var(--color-text-primary);
        }
        .monitor-detail-label
        {
            color: var(--color-text-secondary);
            text-align: right;
            padding-right: 6px;
        }
        .monitor-detail-value { padding-left: 6px; }
        .anchorBL { display: none !important; }
        .autocar-icon { background: none !important; border: none !important; }
        .autocar-label { background-color: var(--color-primary) !important; color: var(--color-text-inverse) !important; border: none !important; padding: 2px 10px !important; text-align: center !important; font-size: 12px !important; }
        .leaflet-tooltip.autocar-label::before { border-top-color: var(--color-primary) !important; }
        .monitor__panel-body h4
        {
            height: 30px;
            line-height: 30px;
            padding: 0px;
            margin: 0px;
            font-size: 16px;
        }
        .monitor__panel-body h4 span
        {
            font-size: 12px;
            font-weight: normal;
            margin-left: 4px;
            color: var(--color-text-secondary);
        }
        .monitor__grids
        {
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 6px;
            margin-bottom: 14px;
        }
        .monitor__grids div
        {
            display: flex;
            align-items: center;
            width: 100%;
            min-height: 30px;
            line-height: 1.3;
            font-size: 13px;
            font-family: inherit;
            font-variant-numeric: tabular-nums;
            text-align: left;
            background-color: var(--color-bg-panel);
            border: solid 1px var(--color-border);
            margin: 0px;
            padding: 5px 8px;
            color: var(--color-text-primary);
            border-radius: 4px;
            overflow: hidden;
            transition: border-color var(--transition-fast), background-color var(--transition-fast), color var(--transition-fast);
        }
        .monitor__grids div.active
        {
            background-color: var(--color-primary);
            border-color: var(--color-primary);
            color: var(--color-text-inverse);
        }
        .monitor__grids div:hover
        {
            cursor: pointer;
            background-color: var(--color-bg-sidebar-hover);
            border-color: var(--color-primary);
            color: var(--color-text-primary);
        }
        .monitor__grids div.active:hover
        {
            background-color: var(--color-primary);
            color: var(--color-text-inverse);
        }
        .monitor__grids div span
        {
            min-width: 0;
            margin-left: 6px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }
    </style>
</head>
<body>
<div class="layui-layout layui-layout-admin app-layout">
<#include "inc/sidebar.ftlh">
    <div class="layui-body app-body">
        <div class="monitor__map" id="map"></div>
        <div class="monitor__log-panel">
            <div class="monitor__panel">
                <h3>行程任务日志</h3>
                <div class="monitor__panel-body">
                    <table id="logs" class="layui-table" lay-skin="line">
                        <thead>
                        <tr>
                            <th align="center">类型</th>
                            <th align="center">时间</th>
                            <th align="left">内容</th>
                        </tr>
                        </thead>
                        <tbody>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
        <div class="monitor__info-panel">
            <div class="monitor__panel">
                <h3>行程任务详情<button class="layui-btn layui-btn-xs" id="btn-stop">终止</button></h3>
                <div class="monitor__panel-body">
                    <div class="monitor-detail-row">
                        <div class="monitor-detail-label">线路：</div>
                        <div class="monitor-detail-value" id="routeName">--</div>
                    </div>
                    <div class="monitor-detail-row">
                        <div class="monitor-detail-label">线路里程：</div>
                        <div class="monitor-detail-value" id="routeMileages">--</div>
                    </div>
                    <div class="monitor-detail-row">
                        <div class="monitor-detail-label">车牌号：</div>
                        <div class="monitor-detail-value" id="vehicleNumber">--</div>
                    </div>
                    <div class="monitor-detail-row">
                        <div class="monitor-detail-label">终端ID：</div>
                        <div class="monitor-detail-value" id="deviceSn">--</div>
                    </div>
                    <div class="monitor-detail-row">
                        <div class="monitor-detail-label">SIM卡号：</div>
                        <div class="monitor-detail-value" id="simNumber">--</div>
                    </div>
                    <div class="monitor-detail-row">
                        <div class="monitor-detail-label">启动时间：</div>
                        <div class="monitor-detail-value" id="startTime">--</div>
                    </div>
                    <div class="monitor-detail-row">
                        <div class="monitor-detail-label">最后上报：</div>
                        <div class="monitor-detail-value" id="reportTime">--</div>
                    </div>
                    <div class="monitor-detail-row">
                        <div class="monitor-detail-label">当前位置：</div>
                        <div class="monitor-detail-value" id="lng">--</div>
                    </div>
                    <div class="monitor-detail-row">
                        <div class="monitor-detail-label">&nbsp;</div>
                        <div class="monitor-detail-value" id="lat">--</div>
                    </div>
                    <hr />
                    <h4>报警标志<span>点击置位，高亮表示相应位为1</span></h4>
                    <div class="monitor__grids" id="warning-flags">
                        <div>00<span>紧急报警</span></div>
                        <div>01<span>超速报警</span></div>
                        <div>02<span>疲劳驾驶</span></div>
                        <div>03<span>危险预警</span></div>
                        <div>04<span>GNSS模块发生故障</span></div>
                        <div>05<span>GNSS天线未接或被剪断</span></div>
                        <div>06<span>GNSS天线短路</span></div>
                        <div>07<span>终端主电源欠压</span></div>
                        <div>08<span>终端主电源掉电</span></div>
                        <div>09<span>终端LCD或显示器故障</span></div>
                        <div>10<span>TTS模块故障</span></div>
                        <div>11<span>摄像头故障</span></div>
                        <div>12<span>道路运输证IC卡模块故障</span></div>
                        <div>13<span>超速预警</span></div>
                        <div>14<span>疲劳驾驶预警</span></div>
                        <div>15<span>保留</span></div>
                        <div>16<span>保留</span></div>
                        <div>17<span>保留</span></div>
                        <div>18<span>当天累计驾驶超时</span></div>
                        <div>19<span>超时停车</span></div>
                        <div>20<span>进出区域</span></div>
                        <div>21<span>进出路线</span></div>
                        <div>22<span>路段行驶时间不足/过长</span></div>
                        <div>23<span>路线偏离报警</span></div>
                        <div>24<span>车辆VSS故障</span></div>
                        <div>25<span>车辆油量异常</span></div>
                        <div>26<span>车辆被盗（通过车辆防盗器）</span></div>
                        <div>27<span>车辆非法点火</span></div>
                        <div>28<span>车辆非法位移</span></div>
                        <div>29<span>碰撞预警</span></div>
                        <div>30<span>侧翻预警</span></div>
                        <div>31<span>非法开门报警</span></div>
                    </div>
                    <h4>状态位<span>点击置位，高亮表示相应位为1</span></h4>
                    <div class="monitor__grids" id="state-flags">
                        <div>00<span>ACC状态</span></div>
                        <div>01<span>定位</span></div>
                        <div>02<span>纬度</span></div>
                        <div>03<span>经度</span></div>
                        <div>04<span>运营状态</span></div>
                        <div>05<span>加密</span></div>
                        <div>06<span>保留</span></div>
                        <div>07<span>保留</span></div>
                        <div>08<span>车载状态(高位)</span></div>
                        <div>09<span>车载状态(低位)</span></div>
                        <div>10<span>车辆油路</span></div>
                        <div>11<span>车辆电路</span></div>
                        <div>12<span>车锁状态</span></div>
                        <div>13<span>前门</span></div>
                        <div>14<span>中门</span></div>
                        <div>15<span>后门</span></div>
                        <div>16<span>驾驶席门</span></div>
                        <div>17<span>自定义车门</span></div>
                        <div>18<span>GPS定位</span></div>
                        <div>19<span>北斗定位</span></div>
                        <div>20<span>GLONASS定位</span></div>
                        <div>21<span>Galileo定位</span></div>
                        <div>22<span>保留</span></div>
                        <div>23<span>保留</span></div>
                        <div>24<span>保留</span></div>
                        <div>25<span>保留</span></div>
                        <div>26<span>保留</span></div>
                        <div>27<span>保留</span></div>
                        <div>28<span>保留</span></div>
                        <div>29<span>保留</span></div>
                        <div>30<span>保留</span></div>
                        <div>31<span>保留</span></div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
<#include "inc/footer.ftlh">
<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js" crossorigin=""></script>
<script type="text/javascript" src="/static/js/LeafletAutoCar.js"></script>
<script type="text/javascript">

    var taskId = '${id}';
    var map = null;
    var vehicle = null;
    var lastPositionTime = 0;
    var lastLogTime = 0;
    var themeStyles = getComputedStyle(document.documentElement);
    var themePrimary = (themeStyles.getPropertyValue('--color-primary') || '#2f80ed').trim();

    $(document).ready(function()
    {
        setCurrentMenu('list-monitor');

        map = L.map('map', { zoomControl: true, attributionControl: false });
        L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
            maxZoom: 19
        }).addTo(map);
        map.setView([39.9042, 116.4074], 16);

        loadTaskInfo();

        $('#btn-stop').click(terminate);
        $('#warning-flags div, #state-flags div').click(function()
        {
            var el = $(this);
            var type = el.parent().attr('id');
            var on = !(el.hasClass('active'));
            var bitIndex = el.index();

            request.post('/monitor/bit/set', { id : taskId, type : type, bitIndex : bitIndex, on : on }, { silentError: true }).done(function()
            {
                if (on) el.addClass('active');
                else el.removeClass('active');
            }).fail(function(xhr)
            {
                console.error(xhr.responseJSON || xhr.responseText);
            });
        });
    });

    function terminate()
    {
        request.post('/monitor/terminate', { id : taskId }).done(function()
        {
            appNotify('success', '已成功终止任务');
        });
    }

    function onVehicleClick()
    {

    }

    function loadTaskInfo()
    {
        request.post('/monitor/info', { id : taskId }).done(function(info)
        {
            $('#routeName').html(info.routeName);
            $('#routeMileages').html((info.routeMileages / 1000).toFixed(2) + ' km');
            $('#vehicleNumber').html(info.vehicleNumber);
            $('#deviceSn').html(info.deviceSn);
            $('#simNumber').html(info.simNumber);
            $('#startTime').html(new Date(info.startTime).format('yyyy-MM-dd hh:mm:ss'));

            var startPoint = L.latLng(info.latitude, info.longitude);
            startPoint.arrivalTime = info.reportTime;
            map.setView(startPoint, 16);

            vehicle = new LeafletAutoCar(map, startPoint, {
                label : info.vehicleNumber,
                labelColor : themePrimary,
                enableRotation : true,
                autoView : true,
                time : info.reportTime
            });

            var elsa = $('#state-flags div');
            var elsb = $('#warning-flags div');
            for (var i = 0; i < 32; i++)
            {
                var a = (info.stateFlags >> i) & 0x01;
                var b = (info.warningFlags >> i) & 0x01;
                if (a) elsa.eq(i).addClass('active');
                if (b) elsb.eq(i).addClass('active');
            }

            showCurrentPosition(info.longitude, info.latitude, info.reportTime);

            vehicle.show();

            loadLogs();
        });

        setTimeout(trace, 2000);
    }

    function loadLogs()
    {
        request.post('/monitor/logs', { id : taskId, timeAfter : lastLogTime }, { silentError: true }).done(function(logs)
        {
            var table = $('#logs');
            for (var i = 0; logs && i < logs.length; i++)
            {
                var item = logs[i];
                var type = item.type;
                if (item.type == 'MESSAGE_IN') type = '下行消息';
                else if (item.type == 'MESSAGE_OUT') type = '上行消息';
                else if (item.type == 'INFO') type = '信息';
                else if (item.type == 'EXCEPTION') type = '异常';
                else if (item.type == 'USER_TRIGGER') type = '用户触发';

                var shtml = '';
                shtml += '<tr>';
                shtml += '  <td align="center">' + type + '</td>';
                shtml += '  <td align="center">' + new Date(item.time).format('yyyy-MM-dd hh:mm:ss') + '</td>';
                shtml += '  <td align="left">' + (item.attachment || '--') + '</td>';
                shtml += '</tr>';

                table.prepend(shtml);
                lastLogTime = item.time;
            }

            setTimeout(loadLogs, 2000);
        }).fail(function(xhr)
        {
            console.error(xhr.responseJSON || xhr.responseText);
            setTimeout(loadLogs, 2000);
        });
    }

    function trace()
    {
        request.post('/monitor/position', { id : taskId, time : lastPositionTime }, { silentError: true }).done(function(result)
        {
            if (result)
            {
                lastPositionTime = result.reportTime;
                showCurrentPosition(result.longitude, result.latitude, result.reportTime);
                if (vehicle) {
                    var target = L.latLng(result.latitude, result.longitude);
                    target.arrivalTime = result.reportTime;
                    vehicle.moveTo(target, result.reportTime, result.direction);
                }
            }

            setTimeout(trace, 2000);
        }).fail(function(xhr)
        {
            console.error(xhr.responseJSON || xhr.responseText);
            setTimeout(trace, 2000);
        });
    }

    function showCurrentPosition(lng, lat, reportTime)
    {
        $('#lng').html('经度：' + String(lng).replace(/^(\d+\.\d{6})\d+$/gi, '$1'));
        $('#lat').html('纬度：' + String(lat).replace(/^(\d+\.\d{6})\d+$/gi, '$1'));
        $('#reportTime').html(new Date(reportTime).format());
    }

</script>
</html>
