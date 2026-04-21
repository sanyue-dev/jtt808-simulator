<!DOCTYPE html>
<html lang="en">
<head>
<#include "inc/resource.ftlh">
    <title>行程任务详情</title>
    <style type="text/css">
        body { overflow: hidden; }
        .layout__content
        {
            padding: 0px 350px 300px 0px;
            position: relative;
        }
        .monitor__map
        {
            background-color: var(--color-bg-page);
            width: 100%;
            height: 100%;
        }
        .monitor__log-panel
        {
            position: absolute;
            left: 0px;
            bottom: 0px;
            width: 100%;
            height: 300px;
            border-top: solid 1px var(--color-border);
            z-index: 200;
            background-color: var(--color-bg-panel);
            box-shadow: 0 -8px 24px rgba(15, 23, 42, 0.08);
        }
        .monitor__info-panel
        {
            position: absolute;
            right: 0px;
            top: 0px;
            width: 350px;
            height: 100%;
            border-left: solid 1px var(--color-border);
            z-index: 100;
            background-color: var(--color-bg-panel);
            padding-bottom: 300px;
            box-shadow: -8px 0 24px rgba(15, 23, 42, 0.08);
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
        .monitor__panel-body .grid__col--3, .monitor__panel-body .grid__col--7 { height: 24px; line-height: 24px; color: var(--color-text-primary); }
        .monitor__panel-body .grid__col--3
        {
            color: var(--color-text-secondary);
        }
        .anchorBL { display: none !important; }
        .autocar-icon { background: none !important; border: none !important; }
        .autocar-label { background-color: var(--color-primary) !important; color: var(--color-text-inverse) !important; border: none !important; padding: 2px 10px !important; text-align: center !important; font-size: 12px !important; box-shadow: 0 6px 16px rgba(47, 128, 237, 0.22); }
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
        .monitor__grids div
        {
            display: block;
            width: 100%;
            height: 24px;
            line-height: 24px;
            font-size: 10px;
            font-family: consolas;
            text-align: left;
            background-color: var(--color-bg-muted);
            border: solid 1px var(--color-border-strong);
            margin: 0px 4px 4px 0px;
            padding-left: 4px;
            color: var(--color-text-primary);
            border-radius: 4px;
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
        }
        .monitor__grids div span
        {
            margin-left: 4px;
        }
    </style>
</head>
<body>
<div class="layout">
<#include "inc/sidebar.ftlh">
    <div class="layout__content">
        <div class="monitor__map" id="map"></div>
        <div class="monitor__log-panel">
            <div class="monitor__panel">
                <h3>行程任务日志</h3>
                <div class="monitor__panel-body">
                    <table id="logs" class="table table-bordered table-striped table-condensed table-hover">
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
                <h3>行程任务详情<button class="btn btn--sm btn--blue" id="btn-stop">终止</button></h3>
                <div class="monitor__panel-body">
                    <div class="grid__row">
                        <div class="grid__col--3 text-right">线路：</div>
                        <div class="grid__col--7" id="routeName">--</div>
                    </div>
                    <div class="grid__row">
                        <div class="grid__col--3 text-right">线路里程：</div>
                        <div class="grid__col--7" id="routeMileages">--</div>
                    </div>
                    <div class="grid__row">
                        <div class="grid__col--3 text-right">车牌号：</div>
                        <div class="grid__col--7" id="vehicleNumber">--</div>
                    </div>
                    <div class="grid__row">
                        <div class="grid__col--3 text-right">终端ID：</div>
                        <div class="grid__col--7" id="deviceSn">--</div>
                    </div>
                    <div class="grid__row">
                        <div class="grid__col--3 text-right">SIM卡号：</div>
                        <div class="grid__col--7" id="simNumber">--</div>
                    </div>
                    <div class="grid__row">
                        <div class="grid__col--3 text-right">启动时间：</div>
                        <div class="grid__col--7" id="startTime">--</div>
                    </div>
                    <div class="grid__row">
                        <div class="grid__col--3 text-right">最后上报：</div>
                        <div class="grid__col--7" id="reportTime">--</div>
                    </div>
                    <div class="grid__row">
                        <div class="grid__col--3 text-right">当前位置：</div>
                        <div class="grid__col--7" id="lng">--</div>
                    </div>
                    <div class="grid__row">
                        <div class="grid__col--3 text-right">&nbsp;</div>
                        <div class="grid__col--7" id="lat">--</div>
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
<link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" crossorigin="" />
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

            $.post('/monitor/bit/set', { id : taskId, type : type, bitIndex : bitIndex, on : on }, function(result)
            {
                if (result.error && result.error.code) return console.error(result);
                if (on) el.addClass('active');
                else el.removeClass('active');
            });
        });
    });

    function terminate()
    {
        $.post('/monitor/terminate', { id : taskId }, function(result)
        {
            if (result.error && result.error.code) return toastr('error', result.error.reason);
            toastr('success', '已成功终止任务');
        });
    }

    function onVehicleClick()
    {

    }

    function loadTaskInfo()
    {
        $.post('/monitor/info', { id : taskId }, function(result)
        {
            if (result.error && result.error.code) return toastr('error', result.error.reason);
            var info = result.data;
            $('#routeName').html(info.routeName);
            $('#routeMileages').html((info.routeMileages / 1000).toFixed(2) + ' km');
            $('#vehiclenumber').html(info.vehicleNumber);
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
        $.post('/monitor/logs', { id : taskId, timeAfter : lastLogTime }, function(result)
        {
            if (result.error && result.error.code) return console.error(result), setTimeout(loadLogs, 2000);
            var logs = result.data;
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
        });
    }

    function trace()
    {
        $.post('/monitor/position', { id : taskId, time : lastPositionTime }, function(result)
        {
            if (result.error && result.error.code) return console.error(result), setTimeout(trace, 2000);

            if (result.data)
            {
                lastPositionTime = result.data.reportTime;
                showCurrentPosition(result.data.longitude, result.data.latitude, result.data.reportTime);
                if (vehicle) {
                    var target = L.latLng(result.data.latitude, result.data.longitude);
                    target.arrivalTime = result.data.reportTime;
                    vehicle.moveTo(target, result.data.reportTime);
                }
            }

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
