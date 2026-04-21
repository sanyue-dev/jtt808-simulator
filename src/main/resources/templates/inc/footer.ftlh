<script type="text/javascript" src="/static/proton/js/jquery-2.1.1.min.js"></script>
<script type="text/javascript" src="/static/js/common.js"></script>
<script type="text/javascript">
    function setCurrentMenu(tag)
    {
        $('.layout__menu > li').each(function()
        {
            var el = $(this);
            if (el.attr('x-tag') == tag)
            {
                el.addClass('layout__menu-item--active');
                return false;
            }
        });
    }

    $(document).ready(function()
    {
        $('.layout__menu > li').click(function(e)
        {
            if (e.target.tagName == 'A') return;
            var link = $(this).find('a');
            location.href = link.attr('href');
        });

        $('#sidebarToggle').click(function()
        {
            var isCollapsed = $(document.documentElement).toggleClass('sidebar-collapsed').hasClass('sidebar-collapsed');
            localStorage.setItem('sidebar-collapsed', isCollapsed);
            $(window).trigger('resize');
        });
    });
</script>
