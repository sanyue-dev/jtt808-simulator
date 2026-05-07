<script type="text/javascript" src="/static/js/jquery-2.1.1.min.js"></script>
<script type="text/javascript" src="/static/vendor/layui/2.13.6/layui.js"></script>
<script type="text/javascript" src="/static/js/common.js"></script>
<script type="text/javascript">
    function setCurrentMenu(tag)
    {
        $('.app-menu > li').each(function()
        {
            var el = $(this);
            if (el.attr('x-tag') == tag)
            {
                el.addClass('layui-this');
                return false;
            }
        });
        if (window.layui && layui.element) layui.element.render('nav');
    }

    $(document).ready(function()
    {
        $('#sidebarToggle').click(function()
        {
            var isCollapsed = $(document.documentElement).toggleClass('sidebar-collapsed').hasClass('sidebar-collapsed');
            localStorage.setItem('sidebar-collapsed', isCollapsed);
            $(window).trigger('resize');
        });

        if (window.layui) layui.use(['element', 'form'], function()
        {
            layui.element.render('nav');
            layui.form.render();
        });
    });
</script>
