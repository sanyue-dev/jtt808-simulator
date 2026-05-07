<div class="layui-side layui-bg-black app-sidebar">
    <div class="layui-side-scroll">
        <div class="app-logo">
            <span class="app-logo__text">JT/T 808 Simulator</span>
        </div>
        <ul class="layui-nav layui-nav-tree app-menu" lay-filter="sidebar-nav">
            <li class="layui-nav-item" x-tag="route" title="线路管理"><a href="/route/index"><i class="layui-icon layui-icon-location"></i><span>线路管理</span></a></li>
            <li class="layui-nav-item" x-tag="device" title="设备库"><a href="/device/index"><i class="layui-icon layui-icon-username"></i><span>设备库</span></a></li>
            <li class="layui-nav-item" x-tag="task" title="创建行程"><a href="/task/index"><i class="layui-icon layui-icon-release"></i><span>创建行程</span></a></li>
            <li class="layui-nav-item" x-tag="batch" title="压力测试"><a href="/batch/index"><i class="layui-icon layui-icon-engine"></i><span>压力测试</span></a></li>
            <li class="layui-nav-item" x-tag="task-group-monitor" title="任务组监控"><a href="/task-groups/monitor"><i class="layui-icon layui-icon-chart-screen"></i><span>任务组监控</span></a></li>
            <li class="layui-nav-item" x-tag="list-monitor" title="实时行程监控"><a href="/monitor/list/index"><i class="layui-icon layui-icon-eye"></i><span>实时行程监控</span></a></li>
        </ul>
    </div>
    <button type="button" class="layui-btn layui-btn-primary app-sidebar-toggle" id="sidebarToggle" title="收起侧边栏">
        <i class="layui-icon layui-icon-shrink-right app-sidebar-toggle__collapse"></i>
        <i class="layui-icon layui-icon-spread-left app-sidebar-toggle__expand"></i>
    </button>
</div>
