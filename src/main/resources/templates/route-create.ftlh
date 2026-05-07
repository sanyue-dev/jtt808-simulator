<!DOCTYPE html>
<html lang="en">
<head>
    <#include "inc/resource.ftlh">
    <title>${((pageMode!'create') == 'edit')?string('编辑线路', '创建新线路')}</title>
    <style type="text/css">
        .layout__content
        {
            position: relative;
            padding-left: 400px;
        }
        .layout__content #panel
        {
            position: absolute;
            top: 0px;
            left: 0px;
            width: 400px;
            height: 100%;
            background-color: var(--color-bg-panel);
            border-right: solid 1px solid var(--color-border);
            z-index: 20;
            overflow-y: auto;
            box-sizing: border-box;
        }
        #panel h4 { height: 30px; line-height: 30px; margin: 20px 0px 10px 0px; padding-left: 12px; font-size: 12px; font-weight: 600; text-transform: uppercase; letter-spacing: .04em; color: var(--color-text-secondary); border-bottom: solid 1px var(--color-border); }
        #panel h4:first-child { margin-top: 0px !important; height: 48px; line-height: 48px; padding-right: 12px; box-sizing: border-box; font-size: 15px; color: var(--color-text-primary); text-transform: none; letter-spacing: 0; }
        #panel h4:first-child button { margin-top: 10px; }
        #panel h4 a { float: right; margin-right: 12px; }
        #panel h4 button { float: right; }
        #panel .grid__row { min-height: 44px; line-height: 40px; font-size: 13px; }
        #panel .grid__col--7 input[type=text] { min-width: 200px; }
        #panel #btn-locate-point { margin-right: 5px; }
        #panel .route-event-dropdown { text-align: left; margin-top: 4px; }
        #panel .route-event-dropdown .task__dropdown-trigger { height: 32px; line-height: normal; }
        #panel .route-event-dropdown .task__dropdown-menu { line-height: normal; }
        #map { width: 100%; height: 100%; }
        .anchorBL { display: none !important; }

        #x-poi-list
        {
            position: absolute;
            top: 0px;
            left: 0px;
            width: 360px;
            max-height: 360px;
            overflow-y: auto;
            background-color: var(--color-bg-panel);
            border: solid 1px var(--color-border);
            border-radius: var(--radius-sm);
            padding: 4px;
            display: none;
            z-index: 9999;
            box-sizing: border-box;
        }
        #x-poi-list .poi-empty
        {
            padding: 18px 14px;
            color: var(--color-text-secondary);
            font-size: 12px;
            text-align: center;
        }
        #x-poi-list a, #x-poi-list a:visited
        {
            display: block;
            min-height: 48px;
            color: var(--color-text-primary);
            padding: 8px 12px;
            border-radius: var(--radius-sm);
            text-decoration: none;
            box-sizing: border-box;
        }
        #x-poi-list a:hover
        {
            background-color: var(--color-bg-panel-muted);
        }
        #x-poi-list .poi-text
        {
            min-width: 0px;
        }
        #x-poi-list .poi-title
        {
            font-size: 13px;
            line-height: 18px;
            color: var(--color-text-primary);
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
        #x-poi-list .poi-subtitle
        {
            margin-top: 2px;
            font-size: 11px;
            line-height: 16px;
            color: var(--color-text-secondary);
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
        }
        .route-actions
        {
            padding: 10px 0px 10px 0px;
            text-align: center;
        }
        .station
        {
            min-height: 44px;
            line-height: 40px;
        }
        .station .station-title
        {
            width: 100px;
            min-height: 44px;
            line-height: 40px;
            float: left;
        }
        .station .station-value
        {
            width: 210px;
            min-height: 44px;
            line-height: 40px;
            float: left;
        }
        .station .station-value input
        {
            width: 100%;
            height: 40px;
        }
        .station .station-action
        {
            width: 80px;
            min-height: 44px;
            line-height: 40px;
            text-align: center;
            float: left;
        }
        .station .station-action a
        {
            margin: 0px 4px 0px 4px;
        }
    </style>
</head>
<body>
<div class="layout">
    <#include "inc/sidebar.ftlh">
    <div class="layout__content">
        <div id="panel">
            <h4>
                ${((pageMode!'create') == 'edit')?string('编辑线路', '线路设置')}
                <button id="btn-save" class="btn btn--sm btn--blue">保存</button>
                <button id="btn-measure" class="btn btn--sm btn--gray">测距</button>
            </h4>
            <div class="grid__row">
                <div class="grid__col--3 text-right">线路总里程：</div>
                <div class="grid__col--7">
                    <span id="route-distance">-</span>
                </div>
            </div>
            <div class="grid__row">
                <div class="grid__col--3 text-right">行程耗时估计：</div>
                <div class="grid__col--7"><span id="route-duration">-</span></div>
            </div>
            <div class="grid__row">
                <div class="grid__col--3 text-right">名称：</div>
                <div class="grid__col--7"><input type="text" id="name" placeholder="比如：北京~上海 早班线路" value="" /></div>
            </div>
            <div class="grid__row">
                <div class="grid__col--3 text-right">最低时速：</div>
                <div class="grid__col--7"><input type="text" id="minSpeed" placeholder="公里/小时" value=""/></div>
            </div>
            <div class="grid__row">
                <div class="grid__col--3 text-right">最高时速：</div>
                <div class="grid__col--7"><input type="text" id="maxSpeed" placeholder="公里/小时" value=""/></div>
            </div>

            <h4>停留点<a href="javascript:;" id="btn-add-point"><i class="fa fa-crosshairs"></i> 添加</a></h4>
            <div class="grid__row text-center">
                <div class="grid__col--2">#</div>
                <div class="grid__col--2">最短</div>
                <div class="grid__col--2">最长</div>
                <div class="grid__col--2">概率</div>
                <div class="grid__col--2">-</div>
            </div>
            <div id="x-stay-points"></div>

            <h4>问题路段<a href="javascript:;" id="btn-add-segment"><i class="fa fa-wheelchair-alt"></i> 添加</a></h4>
            <div class="grid__row text-center">
                <div class="grid__col--2">#</div>
                <div class="grid__col--4">安全事件</div>
                <div class="grid__col--2">概率</div>
                <div class="grid__col--2">-</div>
            </div>
            <div id="x-trouble-segments"></div>

            <h4>
                行驶线路规划
                <button id="btn-add-station" class="btn btn--sm btn--blue">添加站点</button>
            </h4>
            <div class="panel-body">
                <div id="stations"></div>
                <div class="route-actions"><button id="btn-plan" class="btn btn--blue">线路规划</button></div>
            </div>
        </div>
        <div id="map"></div>
        <div id="x-poi-list"></div>
    </div>
</div>
</body>
<#include "inc/footer.ftlh">
<!-- Leaflet map and measure plugin -->
<link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" crossorigin="" />
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/leaflet.polylinemeasure@3.0.0/Leaflet.PolylineMeasure.css" crossorigin="" />
<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js" crossorigin=""></script>
<script src="https://cdn.jsdelivr.net/npm/leaflet.polylinemeasure@3.0.0/Leaflet.PolylineMeasure.min.js" crossorigin=""></script>
<script type="text/javascript">
    var pageMode = '${pageMode!"create"}';
    var initialRoute = ${(routeJson!'null')?no_esc};
    var initialStations = ${(stationsJson!'[]')?no_esc};
    var initialPoints = ${(pointsJson!'[]')?no_esc};
    var initialStayPoints = ${(stayPointsJson!'[]')?no_esc};
    var initialTroubleSegments = ${(troubleSegmentsJson!'[]')?no_esc};
    var startPoint = null;
    var endPoint = null;
    var currentStationIndex = null;
    var poiTimer = null;
    var stationComposing = false;
    var lastSegmentPickAt = 0;
    var themeStyles = getComputedStyle(document.documentElement);
    var themePrimary = (themeStyles.getPropertyValue('--color-primary') || '#2f80ed').trim();
    var themeDanger = (themeStyles.getPropertyValue('--color-danger') || '#d9485f').trim();

    function clearMarks()
    {
        if (markLayerGroup) markLayerGroup.clearLayers();
    }

    function markMode()
    {
        clearMarks();
        if (!route.points || route.points.length === 0) return;
        if (mode !== 'segment') return;
        for (var i = 0; i < route.points.length; i += 80)
        {
            var p = route.points[i];
            L.circleMarker([p.lat, p.lng], {
                radius: 3,
                color: themePrimary,
                fillColor: themePrimary,
                fillOpacity: 0.65,
                weight: 1,
                interactive: false
            }).addTo(markLayerGroup);
        }
    }

    function getNearestRoutePointIndex(latlng)
    {
        if (!route.points || route.points.length === 0 || !latlng) return null;
        var nearestIndex = null;
        var nearestDistance = Number.MAX_VALUE;
        for (var i = 0; i < route.points.length; i++)
        {
            var point = route.points[i];
            var distance = map.distance(latlng, L.latLng(point.lat, point.lng));
            if (distance < nearestDistance)
            {
                nearestDistance = distance;
                nearestIndex = i;
            }
        }
        return nearestIndex;
    }

    function chooseSegmentPoint(latlng)
    {
        var now = Date.now();
        if (now - lastSegmentPickAt < 250) return;
        lastSegmentPickAt = now;
        var index = getNearestRoutePointIndex(latlng);
        if (index == null) return;
        if (startPoint == null)
        {
            startPoint = index;
            toastr('success', '已选择起点（索引 ' + index + '）');
            return;
        }
        if (index === startPoint) return;
        endPoint = index;
        var s = Math.min(startPoint, endPoint);
        var e = Math.max(startPoint, endPoint);
        route.addTroubleSegment(s, e);
        toastr('success', '已设置问题路段：' + s + ' ~ ' + e);
        startPoint = null;
        endPoint = null;
        mode = 'plan';
        clearMarks();
    }

    function addStationRow(name, stationData)
    {
        var station = stationData || { id: id(), name: name || '', lat: null, lng: null, marker: null };
        if (typeof station.id == 'undefined' || station.id == null) station.id = id();
        if (typeof station.marker == 'undefined') station.marker = null;
        if (!stationData) route.stations.push(station);
        var index = route.stations.indexOf(station);
        var shtml = '';
        shtml += '<div class="station" x-index="' + index + '">';
        shtml += '  <div class="station-title">站点</div>';
        shtml += '  <div class="station-value"><input type="text" class="station-name" placeholder="输入地名以搜索" value="' + (station.name || '') + '" /></div>';
        shtml += '  <div class="station-action">';
        shtml += '      <a href="#" id="btn-remove-station"><i class="fa fa-times"></i></a>';
        shtml += '  </div>';
        shtml += '</div>';
        $('#stations').append(shtml);
    }

    function showPOIList(results)
    {
        var shtml = '';
        if (!results || results.length === 0)
        {
            shtml = '<div class="poi-empty">没有找到匹配地点，请换个更完整的地名试试</div>';
        }
        else for (var i = 0; i < results.length; i++)
        {
            var r = results[i];
            var displayName = r.display_name || '';
            var title = displayName.split(',')[0] || displayName;
            var subtitle = displayName.substring(title.length).replace(/^,\s*/, '');
            shtml += '<a href="#" class="poi-item" data-lat="' + r.lat + '" data-lng="' + r.lon + '" data-display="' + displayName + '">';
            shtml += '  <div class="poi-text">';
            shtml += '      <div class="poi-title">' + title + '</div>';
            shtml += '      <div class="poi-subtitle">' + (subtitle || 'OpenStreetMap 地点结果') + '</div>';
            shtml += '  </div>';
            shtml += '</a>';
        }
        var input = $('#stations .station[x-index=' + currentStationIndex + ']').find('.station-name');
        var offset = input.offset();
        if (offset)
        {
            $('#x-poi-list').css({
                top: offset.top + input.outerHeight() + 6,
                left: offset.left,
                width: Math.max(input.outerWidth() + 180, 380)
            });
        }
        $('#x-poi-list').html(shtml).show();
    }

    function searchPOI(keyword)
    {
        if (!keyword || keyword.length < 2)
        {
            $('#x-poi-list').hide();
            return;
        }
        if (poiTimer) clearTimeout(poiTimer);
        poiTimer = setTimeout(function()
        {
            var url = 'https://nominatim.openstreetmap.org/search?format=json&limit=10&q=' + encodeURIComponent(keyword);
            fetch(url, { headers: { 'Accept': 'application/json' }})
                .then(function(res) { return res.json(); })
                .then(function(json) { showPOIList(json || []); })
                .catch(function() { $('#x-poi-list').hide(); });
        }, 300);
    }

    function refreshRouteSummary()
    {
        if (!route.points || route.points.length === 0)
        {
            $('#route-distance').text('-');
            $('#route-duration').text('-');
            return;
        }
        var km = (route.mileages / 1000).toFixed(2);
        var minutes = Math.round(route.duration / 60);
        $('#route-distance').text(km + ' 公里');
        $('#route-duration').text(minutes + ' 分钟');
    }

    function redrawRoutePolyline()
    {
        if (!route.points || route.points.length === 0) return;
        var latlngs = [];
        for (var i = 0; i < route.points.length; i++)
        {
            var point = route.points[i];
            latlngs.push([point.lat, point.lng]);
        }
        if (latlngs.length === 0) return;
        var polyline = L.polyline(latlngs, { color: themePrimary, weight: 3 }).addTo(routeLayerGroup);
        polyline.on('click', function(e)
        {
            if (mode !== 'segment') return;
            if (e.originalEvent) L.DomEvent.stop(e.originalEvent);
            chooseSegmentPoint(e.latlng);
        });
        if (latlngs.length > 1) map.fitBounds(L.latLngBounds(latlngs));
        else map.setView(latlngs[0], 16);
    }

    function rebuildStationRows()
    {
        $('#stations').html('');
        for (var i = 0; i < route.stations.length; i++)
        {
            var station = route.stations[i];
            addStationRow(station.name || '', station);
        }
    }

    function rebuildStationMarkers()
    {
        for (var i = 0; i < route.stations.length; i++)
        {
            var station = route.stations[i];
            if (station.lat == null || station.lng == null) continue;
            station.marker = L.marker([station.lat, station.lng]).addTo(routeLayerGroup).bindPopup(station.name || '站点');
        }
    }

    function rebuildStayPointMarkers()
    {
        for (var i = 0; i < route.stayPoints.length; i++)
        {
            var point = route.stayPoints[i];
            point.marker = L.marker([point.latitude, point.longitude]).addTo(routeLayerGroup);
        }
        route.__generate_stay_point_rows();
    }

    function rebuildTroubleSegmentPolylines()
    {
        for (var i = 0; i < route.troubleSegments.length; i++)
        {
            var item = route.troubleSegments[i];
            var latlngs = [];
            for (var j = item.startIndex; j <= item.endIndex; j++)
            {
                var point = route.points[j];
                if (!point) continue;
                latlngs.push([point.lat, point.lng]);
            }
            item.polyline = null;
            if (latlngs.length > 1)
            {
                item.polyline = L.polyline(latlngs, {
                    color: themeDanger,
                    weight: 6,
                    opacity: 0.95
                }).addTo(routeLayerGroup);
            }
        }
        route.__generate_segment_rows();
    }

    function hydrateRouteEditor()
    {
        if (pageMode !== 'edit' || !initialRoute) return;
        $('#name').val(v(initialRoute.name));
        $('#minSpeed').val(v(initialRoute.minSpeed));
        $('#maxSpeed').val(v(initialRoute.maxSpeed));
        route.mileages = initialRoute.mileages || 0;
        route.stations = initialStations || [];
        route.points = initialPoints || [];
        route.stayPoints = initialStayPoints || [];
        route.troubleSegments = initialTroubleSegments || [];
        rebuildStationRows();
        redrawRoutePolyline();
        rebuildStationMarkers();
        rebuildStayPointMarkers();
        rebuildTroubleSegmentPolylines();
        refreshRouteSummary();
    }

    function planWithOSRM(callback)
    {
        var coords = [];
        for (var i = 0; i < route.stations.length; i++)
        {
            var s = route.stations[i];
            if (s.lat == null || s.lng == null) continue;
            coords.push([s.lng, s.lat]);
        }
        if (coords.length < 2)
        {
            toastr('error', '请至少选择两个有效站点');
            return;
        }

        route.points = [];
        route.mileages = 0;
        route.duration = 0;
        route.stayPoints = [];
        route.troubleSegments = [];
        routeLayerGroup.clearLayers();
        markLayerGroup.clearLayers();
        route.__generate_stay_point_rows();
        route.__generate_segment_rows();
        $('#route-distance').text('-');
        $('#route-duration').text('-');

        function requestPair(i)
        {
            if (i >= coords.length - 1)
            {
                refreshRouteSummary();
                for (var j = 0; j < route.stations.length; j++)
                {
                    var station = route.stations[j];
                    if (!station || station.lat == null || station.lng == null) continue;
                    if (station.marker) map.removeLayer(station.marker);
                    station.marker = L.marker([station.lat, station.lng]).addTo(routeLayerGroup).bindPopup(station.name || '站点');
                }
                if (typeof callback === 'function') callback();
                return;
            }
            var a = coords[i];
            var b = coords[i + 1];
            var url = 'https://router.project-osrm.org/route/v1/driving/' + a[0] + ',' + a[1] + ';' + b[0] + ',' + b[1] + '?overview=full&geometries=geojson';
            fetch(url)
                .then(function(res) { return res.json(); })
                .then(function(json)
                {
                    if (!json || !json.routes || json.routes.length === 0)
                    {
                        toastr('error', 'OSRM 无可用路径');
                        requestPair(i + 1);
                        return;
                    }
                    var r = json.routes[0];
                    route.mileages += (r.distance || 0);
                    route.duration += (r.duration || 0);
                    var geometryCoords = r.geometry && r.geometry.coordinates ? r.geometry.coordinates : [];
                    var latlngs = [];
                    for (var k = 0; k < geometryCoords.length; k++)
                    {
                        var lon = geometryCoords[k][0];
                        var lat = geometryCoords[k][1];
                        route.points.push({ lat: lat, lng: lon });
                        latlngs.push([lat, lon]);
                    }
                    var polyline = L.polyline(latlngs, { color: themePrimary, weight: 3 }).addTo(routeLayerGroup);
                    polyline.on('click', function(e)
                    {
                        if (mode !== 'segment') return;
                        if (e.originalEvent) L.DomEvent.stop(e.originalEvent);
                        chooseSegmentPoint(e.latlng);
                    });
                    requestPair(i + 1);
                })
                .catch(function()
                {
                    toastr('error', 'OSRM 请求失败');
                    requestPair(i + 1);
                });
        }

        requestPair(0);
    }
</script>
<script type="text/javascript">
    var map = null;
    var measureControl = null;
    var routeLayerGroup = null;
    var markLayerGroup = null;
    var stationIndex = 0;

    var eventTypes = [
        { code : 'e-illegal', name : '违规驾驶' },
        { code : 'e-danger', name : '危险驾驶' },
        { code : 'e-tire', name : '疲劳驾驶' },
        { code : 'e-adas', name : '车辆运行监测' },
        { code : 'e-vhitch', name : '车辆故障' },
        { code : 'e-dhitch', name : '车机故障' },
        { code : 'e-other', name : '其它' }
    ];

    var nid = 0x01;
    function id() { return nid++; }

    var route = {
        mileages : 0,
        duration : 0,
        points : [],
        stayPoints : [],
        troubleSegments : [],
        stations : [],

        // 添加停留点
        addStayPoint : function(lng, lat, marker)
        {
            this.stayPoints.push({ id : id(), longitude : lng, latitude : lat, minTime : 5, maxTime : 15, ratio : 20, marker : marker });
            this.__generate_stay_point_rows();
        },

        // 停留点定位
        locateStayPoint : function(id)
        {
            for (var i = 0; i < this.stayPoints.length; i++)
            {
                var item = this.stayPoints[i];
                if (item.id == id)
                {
                    map.setView([item.latitude, item.longitude], 18);
                    break;
                }
            }
        },

        // 删除停留点
        removeStayPoint : function(id)
        {
            var list = [];
            var points = this.stayPoints.length;
            var stayPoint = null;
            for (var i = 0; i < points; i++)
            {
                var item = this.stayPoints[i];
                if (item.id == id)
                {
                    stayPoint = item;
                    continue;
                }
                list.push(item);
            }
            this.stayPoints = list;

            if (points != list.length) this.__generate_stay_point_rows();
            return stayPoint;
        },

        // 修改停留点信息
        setStayPointValues : function(id, values, updateView)
        {
            for (var i = 0; i < this.stayPoints.length; i++)
            {
                var item = this.stayPoints[i];
                if (item.id == id)
                {
                    for (var j in values) item[j] = values[j];
                    break;
                }
            }
            if (updateView) this.__generate_stay_point_rows();
        },

        // 添加问题路段
        addTroubleSegment : function(sindex, eindex)
        {
            var latlngs = [];
            for (var i = sindex; i <= eindex; i++)
            {
                var point = this.points[i];
                if (!point) continue;
                latlngs.push([point.lat, point.lng]);
            }
            var polyline = null;
            if (latlngs.length > 1)
            {
                polyline = L.polyline(latlngs, {
                    color: themeDanger,
                    weight: 6,
                    opacity: 0.95
                }).addTo(routeLayerGroup);
            }
            this.troubleSegments.push({ id : id(), startIndex : sindex, endIndex : eindex, eventCode : 'e-danger', ratio : 20, polyline : polyline });
            this.__generate_segment_rows();
        },

        // 删除问题路段
        removeTroubleSegment : function(id)
        {
            var list = [];
            var polyline = null;
            for (var i = 0; i < this.troubleSegments.length; i++)
            {
                var item = this.troubleSegments[i];
                if (item.id == id)
                {
                    polyline = item.polyline;
                    continue;
                }
                list.push(item);
            }
            this.troubleSegments = list;
            this.__generate_segment_rows();

            return polyline;
        },

        // 修改问题路段信息
        setTroubleSegmentValues : function(id, values, updateView)
        {
            for (var i = 0; i < this.troubleSegments.length; i++)
            {
                var item = this.troubleSegments[i];
                if (item.id == id)
                {
                    for (var j in values) {
                        item[j] = values[j];
                    }
                    // 重新赋值
                    this.troubleSegments[i] = item;
                    break;
                }
            }
            if (updateView) this.__generate_segment_rows();
        },

        locateSegment : function(id)
        {
            for (var i = 0; i < this.troubleSegments.length; i++)
            {
                var item = this.troubleSegments[i];
                if (item.id == id)
                {
                    var latlngs = [];
                    for (var j = item.startIndex; j <= item.endIndex; j++)
                    {
                        var p = this.points[j];
                        latlngs.push([p.lat, p.lng]);
                    }
                    var bounds = L.latLngBounds(latlngs);
                    map.fitBounds(bounds);
                    return;
                }
            }
        },

        // 生成问题路段行
        __generate_segment_rows : function()
        {
            var shtml = '';
            for (var i = 0; i < this.troubleSegments.length; i++)
            {
                var item = this.troubleSegments[i];
                shtml += '<div class="grid__row text-center" x-id="' + item.id + '">';
                shtml += '  <div class="grid__col--2">' + item.id + '</div>';
                shtml += '  <div class="grid__col--4">' + this.__generate_event_types(item.eventCode) + '</div>';
                shtml += '  <div class="grid__col--2"><input type="text" id="ratio" value="' + v(item.ratio) + '" placeholder="0~100" /></div>';
                shtml += '  <div class="grid__col--2">';
                shtml += '      <a href="#" id="btn-locate-segment" x-id="' + item.id + '"><i class="fa fa-map-pin"></i></a>';
                shtml += '      <a href="#" id="btn-remove-segment" x-id="' + item.id + '"><i class="fa fa-times"></i></a>';
                shtml += '  </div>';
                shtml += '</div>';
            }
            $('#x-trouble-segments').html(shtml);
        },

        __generate_event_types : function(currentValue)
        {
            var selectedName = '请选择事件分类';
            for (var i = 0; i < eventTypes.length; i++)
            {
                var type = eventTypes[i];
                if (currentValue == type.code) selectedName = type.name;
            }
            var shtml = '<div class="task__dropdown task__dropdown--compact route-event-dropdown' + (currentValue ? '' : ' is-unselected') + '" data-role="eventCode" tabindex="0">';
            shtml += '<input type="hidden" data-role="eventCode" value="' + escapeHtml(currentValue || '') + '" />';
            shtml += '<div class="task__dropdown-trigger">';
            shtml += '<span class="task__dropdown-text">' + escapeHtml(selectedName) + '</span>';
            shtml += '<span class="task__dropdown-arrow"><i class="fa fa-angle-down"></i></span>';
            shtml += '</div>';
            shtml += '<div class="task__dropdown-menu">';
            shtml += '<div class="task__dropdown-option' + (currentValue ? '' : ' is-selected') + '" data-value="">请选择事件分类</div>';
            for (var j = 0; j < eventTypes.length; j++)
            {
                var eventType = eventTypes[j];
                shtml += '<div class="task__dropdown-option' + (currentValue == eventType.code ? ' is-selected' : '') + '" data-value="' + escapeHtml(eventType.code) + '">' + escapeHtml(eventType.name) + '</div>';
            }
            shtml += '</div></div>';
            return shtml;
        },

        // 生成停留点行
        __generate_stay_point_rows : function()
        {
            var shtml = '';
            for (var i = 0; i < this.stayPoints.length; i++)
            {
                var item = this.stayPoints[i];
                shtml += '<div class="grid__row text-center" x-id="' + item.id + '">';
                shtml += '  <div class="grid__col--2">' + item.id + '</div>';
                shtml += '  <div class="grid__col--2"><input type="text" id="minTime" value="' + v(item.minTime) + '" placeholder="分钟" /></div>';
                shtml += '  <div class="grid__col--2"><input type="text" id="maxTime" value="' + v(item.maxTime) + '" placeholder="分钟" /></div>';
                shtml += '  <div class="grid__col--2"><input type="text" id="ratio" value="' + v(item.ratio) + '" placeholder="0~100" /></div>';
                shtml += '  <div class="grid__col--2">';
                shtml += '      <a href="javascript:;" id="btn-locate-point" x-id="' + item.id + '"><i class="fa fa-map-pin"></i></a>';
                shtml += '      <a href="javascript:;" id="btn-remove-point" x-id="' + item.id + '"><i class="fa fa-times"></i></a>';
                shtml += '  </div>';
                shtml += '</div>';
            }
            $('#x-stay-points').html(shtml);
        },
    };

    var mode = 'plan';

    function v(v)
    {
        return v == null || typeof(v) == 'undefined' ? '' : v;
    }

    function escapeHtml(value)
    {
        return $('<div>').text(value == null ? '' : String(value)).html();
    }

    function selectRouteEventOption(option)
    {
        var opt = $(option);
        var dropdown = opt.closest('.route-event-dropdown');
        var value = opt.attr('data-value') || '';
        var row = opt.closest('.grid__row');
        dropdown.find('.task__dropdown-option').removeClass('is-selected');
        opt.addClass('is-selected');
        dropdown.find('input[data-role="eventCode"]').val(value);
        dropdown.find('.task__dropdown-text').text(opt.text());
        dropdown.toggleClass('is-unselected', value === '');
        dropdown.removeClass('is-open');
        route.setTroubleSegmentValues(row.attr('x-id'), { eventCode : value }, false);
    }

    $(document).ready(function()
    {
        setCurrentMenu('route');
        map = L.map('map', { zoomControl: true, attributionControl: false });
        map.setView([35.86166, 104.195397], 5);
        $(window).on('resize', function()
        {
            if (map) map.invalidateSize();
        });
        var baseLayer = L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
            maxZoom: 19
        }).addTo(map);
        routeLayerGroup = L.layerGroup().addTo(map);
        markLayerGroup = L.layerGroup().addTo(map);
        measureControl = L.control.polylineMeasure({
            position: 'topleft',
            unit: 'metres',
            showBearings: false,
            clearMeasurementsOnStop: false,
            showClearControl: true,
            showUnitControl: true
        }).addTo(map);
        hydrateRouteEditor();

        map.on('click', function(e)
        {
            if (mode == 'point')
            {
                var latlng = e.latlng;
                var marker = L.marker(latlng).addTo(routeLayerGroup);
                route.addStayPoint(latlng.lng, latlng.lat, marker);
                return;
            }
            if (mode == 'segment')
            {
                chooseSegmentPoint(e.latlng);
            }
        });

        $('#btn-measure').click(function()
        {
            try {
                var btn = document.querySelector('.leaflet-control-polyline-measure .leaflet-control-polyline-measure-toggle');
                if (btn) btn.click();
            } catch (e) { }
        });

        $(document).on('click', 'a[id=btn-locate-point]', function()
        {
            var pid = $(this).attr('x-id');
            route.locateStayPoint(pid);
        });

        $(document).on('click', 'a[id=btn-locate-segment]', function()
        {
            var pid = $(this).attr('x-id');
            route.locateSegment(pid);
        });

        $('#btn-add-point').click(function()
        {
            // 点一下，加个点，然后添加停留点
            if (route.points.length == 0) return;
            markMode();
            mode = 'point';
            toastr('success', '请在地图线路附近点击以设置停留点，尽量不超过线路小蓝点50米的距离');
        });

        // 停留点的设值
        $(document).on('change', '#x-stay-points input', function()
        {
            var el = $(this);
            var id = el.parents('.grid__row').attr('x-id');
            var val = $.trim(el.val());
            var type = el.attr('id');
            var values = { };
            values[type] = val;
            route.setStayPointValues(id, values, false);
        });

        // 删除停留点
        $(document).on('click', 'a[id=btn-remove-point]', function()
        {
            var id = $(this).attr('x-id');
            var point = route.removeStayPoint(id);
            map.removeLayer(point.marker);
        });

        // 设置问题路段
        $('#btn-add-segment').click(function()
        {
            if (route.points.length == 0) return;
            startPoint = null;
            endPoint = null;
            mode = 'segment';
            markMode();
            toastr('success', '请在线路附近依次点击起点和终点，系统会自动吸附到最近的轨迹点。');
        });

        // 设置路段值
        $(document).on('change', '#x-trouble-segments input[type=text]', function()
        {
            var el = $(this);
            var type = el.attr('id');
            var val = $.trim(el.val());
            var id = el.parents('.grid__row').attr('x-id');
            var values = {};
            values[type] = val;
            route.setTroubleSegmentValues(id, values, false);
        });

        $(document).on('click', '.route-event-dropdown .task__dropdown-trigger', function(e)
        {
            e.stopPropagation();
            var dropdown = $(this).closest('.route-event-dropdown');
            $('.route-event-dropdown').not(dropdown).removeClass('is-open');
            dropdown.toggleClass('is-open');
        });

        $(document).on('click', '.route-event-dropdown .task__dropdown-option', function(e)
        {
            e.stopPropagation();
            selectRouteEventOption(this);
        });

        $(document).on('keydown', '.route-event-dropdown', function(e)
        {
            var dropdown = $(this);
            var options = dropdown.find('.task__dropdown-option:visible');
            var idx = options.index(options.filter('.is-selected'));
            if (e.key === 'Escape') { dropdown.removeClass('is-open'); return; }
            if (!dropdown.hasClass('is-open'))
            {
                if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); dropdown.addClass('is-open'); }
                return;
            }
            if (e.key === 'ArrowDown')
            {
                e.preventDefault();
                idx = idx < options.length - 1 ? idx + 1 : 0;
                selectRouteEventOption(options.eq(idx));
            }
            else if (e.key === 'ArrowUp')
            {
                e.preventDefault();
                idx = idx > 0 ? idx - 1 : options.length - 1;
                selectRouteEventOption(options.eq(idx));
            }
            else if (e.key === 'Enter' || e.key === ' ')
            {
                e.preventDefault();
                dropdown.removeClass('is-open');
            }
        });

        $(document).on('click', function()
        {
            $('.route-event-dropdown').removeClass('is-open');
        });

        // 删除路段
        $(document).on('click', 'a[id=btn-remove-segment]', function()
        {
            var id = $(this).attr('x-id');
            var polyline = route.removeTroubleSegment(id);
            map.removeLayer(polyline);
        });

        // 保存线路信息
        $('#btn-save').click(function()
        {
            var name = $.trim($('#name').val());
            var minSpeed = $.trim($('#minSpeed').val());
            var maxSpeed = $.trim($('#maxSpeed').val());
            var points = getRoutePoints();
            var stationsJson = toJson(route.stations);
            var pointsJson = toJson(points);
            var stayPointsJson = toJson(route.stayPoints);
            var segmentsJson = toJson(route.troubleSegments);

            if (name.length == 0) return toastr('warning', '请输入线路名称');
            if (isNaN(minSpeed) || isNaN(maxSpeed) || minSpeed.length == 0 || maxSpeed.length == 0) return toastr('warning', '请输入速度区间'), false;
            if (points.length == 0) return toastr('warning', '你尚未设定线路轨迹'), false;

            var saveUrl = pageMode === 'edit' ? '/route/edit/save' : '/route/create/save';
            var payload = { name : name, minSpeed : minSpeed, maxSpeed : maxSpeed,
                mileages : parseInt(route.mileages), stationsJsonText : stationsJson, pointsJsonText : pointsJson, stayPointsJsonText : stayPointsJson, segmentsJsonText : segmentsJson };
            if (pageMode === 'edit' && initialRoute && initialRoute.id) payload.id = initialRoute.id;

            var submitSave = function()
            {
                $.post(saveUrl, payload, function(result)
                {
                    if (result.error && result.error.code) return toastr('error', result.error.reason);
                    toastr('success', '保存成功', 1200);
                    setTimeout(function()
                    {
                        location.href = '/route/index';
                    }, 300);
                });
            };

            if (route.stayPoints.length == 0)
            {
                confirmDialog('你未设定停留点，是否继续保存？', function()
                {
                    if (route.troubleSegments.length == 0)
                    {
                        confirmDialog('你未设定问题路段，是否继续保存？', submitSave);
                        return;
                    }
                    submitSave();
                });
                return false;
            }
            if (route.troubleSegments.length == 0)
            {
                confirmDialog('你未设定问题路段，是否继续保存？', submitSave);
                return false;
            }

            submitSave();
        });

        $('#btn-add-station').off('click').on('click', function()
        {
            addStationRow('');
        });

        $(document).on('focus', '.station-name', function()
        {
            currentStationIndex = parseInt($(this).parents('.station').attr('x-index'));
        });

        $(document).on('compositionstart', '.station-name', function()
        {
            stationComposing = true;
        });

        $(document).on('compositionend', '.station-name', function()
        {
            stationComposing = false;
            var kw = $.trim($(this).val());
            currentStationIndex = parseInt($(this).parents('.station').attr('x-index'));
            searchPOI(kw);
        });

        $(document).on('input', '.station-name', function()
        {
            if (stationComposing) return;
            var kw = $.trim($(this).val());
            currentStationIndex = parseInt($(this).parents('.station').attr('x-index'));
            searchPOI(kw);
        });

        $(document).on('click', '.poi-item', function(e)
        {
            e.preventDefault();
            var lat = parseFloat($(this).attr('data-lat'));
            var lng = parseFloat($(this).attr('data-lng'));
            var name = $(this).attr('data-display');
            $('#x-poi-list').hide();
            if (currentStationIndex == null) return;
            var station = route.stations[currentStationIndex];
            if (!station) return;
            station.name = name;
            station.lat = lat;
            station.lng = lng;
            if (station.marker) map.removeLayer(station.marker);
            station.marker = L.marker([lat, lng]).addTo(routeLayerGroup).bindPopup(name);
            map.setView([lat, lng], 14);
            $('#stations .station[x-index=' + currentStationIndex + ']').find('.station-name').val(name);
        });

        $(document).on('click', '#btn-remove-station', function(e)
        {
            e.preventDefault();
            var idx = parseInt($(this).parents('.station').attr('x-index'));
            var station = route.stations[idx];
            if (station && station.marker) map.removeLayer(station.marker);
            route.stations.splice(idx, 1);
            $(this).parents('.station').remove();
            $('#stations .station').each(function(i)
            {
                $(this).attr('x-index', i);
            });
            if (currentStationIndex === idx) currentStationIndex = null;
        });

        $('#btn-plan').off('click').on('click', function()
        {
            var resetTargets = [];
            if (route.stayPoints.length > 0) resetTargets.push('停留点');
            if (route.troubleSegments.length > 0) resetTargets.push('问题路段');
            if (resetTargets.length > 0)
            {
                confirmDialog('重新规划线路会清空当前' + resetTargets.join('和') + '，确认继续吗？', function()
                {
                    planWithOSRM(function()
                    {
                        toastr('success', '线路规划完成');
                    });
                });
                return;
            }
            planWithOSRM(function()
            {
                toastr('success', '线路规划完成');
            });
        });
    });

    function getRoutePoints()
    {
        return route.points || [];
    }

    function toJson(list)
    {
        var xx = [];
        for (var i = 0; i < list.length; i++)
        {
            var obj = list[i];
            var oo = { };
            for (var j in obj)
            {
                if (j.match(/^(index|marker|polyline)$/gi)) continue;
                oo[j] = obj[j];
            }
            xx.push(oo);
        }
        return JSON.stringify(xx);
    }

</script>

</html>
