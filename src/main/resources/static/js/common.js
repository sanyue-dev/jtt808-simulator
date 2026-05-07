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

var appTable = (function()
{
    function concatParam(param1, param2)
    {
        if (param1 == '') return param2;
        if (param1.charAt(param1.length - 1) == '&') return param1 + param2;
        return param1 + '&' + param2;
    }

    function create(options)
    {
        var instance = {
            options: $.extend({
                form : null,
                pageIndex : 1,
                nothead : false,
                pageIndexName : 'pageIndex',
                pageElem : null,
                loading : '正在载入，请稍候...',
                onBefore : null,
                limit : 20
            }, options),

            reload: function()
            {
                load(instance);
            }
        };

        if (instance.options.form)
        {
            instance.options.form.submit(function()
            {
                instance.options.pageIndex = 1;
                load(instance);
                return false;
            });
        }

        load(instance);
        return instance;
    }

    function load(instance)
    {
        var param = instance.options;
        var container = $(param.elem);
        var pageContainer = param.pageElem ? $(param.pageElem) : $();
        var urlParameters = param.form && param.form.serialize ? param.form.serialize() : '';
        urlParameters = concatParam(urlParameters, param.pageIndexName + '=' + param.pageIndex);

        if (param.onBefore && typeof(param.onBefore) == 'function') param.onBefore();
        if (param.loading) container.html('<div class="layui-card"><div class="layui-card-body">' + escapeHtml(param.loading) + '</div></div>');
        pageContainer.html('');

        $.post(param.url, urlParameters, function(result)
        {
            if (result.error && result.error.code)
            {
                if (param.error) param.error(result, param.url, urlParameters);
                else console.error(result, param.url, urlParameters);
                container.html('<div class="table-error">'+escapeHtml(result.error.reason)+'</div>');
                return;
            }

            var shtml = '';
            if (param.render && typeof(param.render) == 'function')
            {
                shtml = param.render(result.data.result);
            }
            else
            {
                shtml = '<table class="layui-table" lay-skin="line">';
                if (param.nothead == false) shtml += '<thead><tr>';
                for (var i = 0; param.nothead == false && i < param.fields.length; i++)
                {
                    var field = param.fields[i];
                    field.align = field.align == null ? 'left' : field.align;
                    shtml += '<th ' + (field.width == null ? '' : 'style="width:' + field.width + '"') + ' class="text-' + field.align + '">' + field.title + '</th>';
                }
                if (param.nothead == false) shtml += '</tr></thead>';

                shtml += '<tbody>';
                for (var j = 0; result.data.list && j < result.data.list.length; j++)
                {
                    var row = result.data.list[j];
                    shtml += '<tr>';
                    for (var k = 0; k < param.fields.length; k++)
                    {
                        var f = param.fields[k];
                        f.align = f.align == null ? 'left' : f.align;
                        var content = row[f.name];
                        if (typeof(f.formatter) == 'function') content = f.formatter(j, content, row);
                        shtml += '<td ' + (f.width == null ? '' : 'style="width:' + f.width + '"') + ' class="text-' + f.align + '">' + (content == null ? '' : content) + '</td>';
                    }
                    shtml += '</tr>';
                }
                shtml += '</tbody></table>';
            }

            container.html(shtml);
            if (typeof(param.onComplete) == 'function') param.onComplete();
            layui.use(['form', 'laypage'], function()
            {
                layui.form.render();
                renderPage(instance, result.data);
            });
            if (param.load) param.load();
        });
    }

    function renderPage(instance, data)
    {
        var param = instance.options;
        var pageContainer = param.pageElem ? $(param.pageElem) : $();
        if (!pageContainer.length) return;

        var totalPage = data.pageCount == undefined ? 1 : data.pageCount;
        var totalCount = data.totalCount || data.total || totalPage * param.limit;
        layui.laypage.render({
            elem: pageContainer.get(0),
            curr: Number(data.pageIndex || param.pageIndex || 1),
            count: totalCount,
            limit: param.limit,
            groups: 5,
            layout: ['prev', 'page', 'next', 'skip'],
            jump: function(obj, first)
            {
                if (first) return;
                instance.options.pageIndex = obj.curr;
                load(instance);
            }
        });
    }

    return {
        render: create
    };
})();
