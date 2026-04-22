<div class="layout__sidebar">
    <div class="layout__logo"></div>
    <ul class="layout__menu">
        <li x-tag="route" title="线路管理"><i class="fa fa-road"></i> <a href="/route/index">线路管理</a></li>
        <!--<li x-tag="schedule"><i class="fa fa-history"></i> <a href="/schdule/index">计划任务</a></li>-->
        <li x-tag="task" title="创建行程"><i class="fa fa-location-arrow"></i> <a href="/task/index">创建行程</a></li>
        <li x-tag="batch" title="压力测试"><i class="fa fa-bolt"></i> <a href="/batch/index">压力测试</a></li>
        <li x-tag="list-monitor" title="实时行程监控"><i class="fa fa-eye"></i> <a href="/monitor/list/index">实时行程监控</a></li>
        <!--
        <li x-tag="map-monitor"><i class="fa fa-globe"></i> <a href="/monitor/map/index">地图实时监控</a></li>
        <li x-tag="setting"><i class="fa fa-gears"></i> <a href="/setting/index">系统参数</a></li>
        -->
    </ul>
    <div class="layout__toggle" id="sidebarToggle" title="收起侧边栏">
        <i class="fa fa-angle-double-left layout__toggle-icon--collapse"></i>
        <i class="fa fa-angle-double-right layout__toggle-icon--expand" style="display:none"></i>
    </div>
</div>
