# Frontend UI Components

- `static/js/common.js` 含全局函数：`confirmDialog(text, onOk, onCancel)`、`toastr(type, msg)`、`setCurrentMenu(id)`、`$.fn.paginate()`；弹窗/提示 UI 统一走这些函数，JS 选择器必须与现有 BEM class 精确匹配
- CSS 共享样式（`.card-section__*`、`.page-header`、`.page-body`）在 `static/css/common.css`，页面特有样式写在各 `.ftlh` 的内联 `<style>` 块
- 全站表格使用 `.data-table` class（定义在 `common.css`），不要使用旧 Bootstrap class（`table-bordered` / `table-striped` 等）
- `$.fn.paginate`（`common.js`）是列表页的表格+分页渲染插件，默认生成 `.data-table` 表格，表头用 `<th>` 标签；新列表页直接调用即可
- `monitor.ftlh` 日志表格不走 paginate 插件，是手写 `<table class="data-table">` + JS prepend 行
- 自定义下拉框（`.task__dropdown`）替代原生 `<select>`，使用 `opacity`+`visibility` 动画（避免 `max-height` 收缩卡顿），面板需 `box-sizing: border-box` 确保与触发器等宽
- 侧边栏收起/展开由 `html.sidebar-collapsed` 类控制（JS 在 footer.ftlh），所有过渡属性需同步 0.2s ease；文字隐藏用 `opacity: 0; max-width: 0` 过渡，不能用 `display: none`
- 侧边栏菜单图标（FontAwesome 4）须设固定 `width` + `text-align: center`，收起态图标居中靠 `padding-left: 14px` 而非 `text-align: center`，菜单 `<li>` 须 `white-space: nowrap; overflow: hidden`
