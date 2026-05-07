Date.prototype.format = function (fmt) {
    var o = {
        "M+": this.getMonth() + 1,
        "d+": this.getDate(),
        "h+": this.getHours(),
        "m+": this.getMinutes(),
        "s+": this.getSeconds(),
        "q+": Math.floor((this.getMonth() + 3) / 3),
        "S": this.getMilliseconds()
    };
    fmt = fmt || 'yyyy-MM-dd hh:mm:ss';
    if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    for (var k in o)
        if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
    return fmt;
};

function escapeHtml(value)
{
    return $('<div>').text(value == null ? '' : String(value)).html();
}

function appNotify(type, text, timeout)
{
    layui.use('layer', function()
    {
        var icon = 0;
        if (type === 'success') icon = 1;
        else if (type === 'error') icon = 2;
        else if (type === 'warning') icon = 0;
        layui.layer.msg(text == null ? '' : String(text), { icon: icon, time: timeout || 5000 });
    });
}

function appConfirm(text, onOk, onCancel)
{
    layui.use('layer', function()
    {
        layui.layer.confirm(text == null ? '' : String(text), { title: '提示', icon: 3 }, function(index)
        {
            layui.layer.close(index);
            if (typeof(onOk) == 'function') onOk();
        }, function(index)
        {
            layui.layer.close(index);
            if (typeof(onCancel) == 'function') onCancel();
        });
    });
}
