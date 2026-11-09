<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageName" value="orders" scope="request"/>
<c:set var="pageTitle" value="订单管理" scope="request"/>
<jsp:include page="common/header.jsp"/>

<div class="toolbar">
    <input id="kwOrder" type="text" placeholder="搜索订单号" value="${fn:escapeXml(orderNo)}" style="width:240px">
    <select id="statusSel">
        <option value="">全部状态</option>
        <option value="0" ${status == 0 ? 'selected' : ''}>待支付</option>
        <option value="1" ${status == 1 ? 'selected' : ''}>已支付待发货</option>
        <option value="2" ${status == 2 ? 'selected' : ''}>已发货</option>
        <option value="3" ${status == 3 ? 'selected' : ''}>已完成</option>
        <option value="5" ${status == 5 ? 'selected' : ''}>已取消</option>
    </select>
    <button class="btn btn-primary" onclick="search()">查询</button>
</div>

<table class="table">
    <thead><tr>
        <th style="width:170px">订单号</th><th style="width:100px">下单人</th><th>商品</th>
        <th style="width:110px">金额</th><th style="width:110px">下单时间</th><th style="width:110px">状态</th><th style="width:90px">操作</th>
    </tr></thead>
    <tbody>
    <c:forEach items="${pageResult.list}" var="order">
        <tr>
            <td>${fn:escapeXml(order.orderNo)}</td>
            <td>${fn:escapeXml(order.username)}</td>
            <td>
                <c:forEach items="${order.items}" var="it" varStatus="s" end="1">
                    ${fn:escapeXml(it.productName)} × ${it.quantity}<c:if test="${not s.last}">；</c:if>
                </c:forEach>
                <c:if test="${fn:length(order.items) > 2}">
                    等${fn:length(order.items)}件
                </c:if>
            </td>
            <td style="color:#ff5000;font-weight:700">¥${order.totalAmount}</td>
            <td>${order.createdAt}</td>
            <td>
                <span class="tag ${order.status == 0 ? 'tag-orange' : order.status == 1 ? 'tag-blue' : order.status == 2 ? 'tag-purple' : order.status == 3 ? 'tag-green' : 'tag-gray'}">
                    ${order.status == 0 ? '待支付' : order.status == 1 ? '待发货' : order.status == 2 ? '已发货' : order.status == 3 ? '已完成' : '已取消'}
                </span>
            </td>
            <td class="ops">
                <c:if test="${order.status == 1}">
                    <a href="javascript:void(0)" onclick="ship('${fn:escapeXml(order.orderNo)}')" style="color:#52c41a">发货</a>
                </c:if>
                <a href="javascript:void(0)" onclick="showDetail('${fn:escapeXml(order.orderNo)}')">详情</a>
            </td>
        </tr>
    </c:forEach>
    <c:if test="${empty pageResult.list}">
        <tr><td colspan="7"><div class="empty">暂无订单</div></td></tr>
    </c:if>
    </tbody>
</table>

<c:if test="${pageResult.total > pageResult.pageSize}">
    <div class="pagination">
        <c:set var="base" value="${ctx}/admin/orders?orderNo=${fn:escapeXml(orderNo)}&status=${status}&page="/>
        <c:if test="${pageResult.pageNum > 1}"><a href="${base}${pageResult.pageNum - 1}">‹ 上一页</a></c:if>
        <span class="cur">${pageResult.pageNum} / ${pageResult.pages}</span>
        <c:if test="${pageResult.pageNum < pageResult.pages}"><a href="${base}${pageResult.pageNum + 1}">下一页 ›</a></c:if>
    </div>
</c:if>

<!-- 详情弹窗 -->
<div class="modal-mask" id="detailMask">
    <div class="modal">
        <h3>订单详情</h3>
        <div id="detailBody" style="font-size:13px;line-height:2;color:#555"></div>
        <div class="foot"><button class="btn btn-plain" onclick="closeDetail()">关闭</button></div>
    </div>
</div>

<jsp:include page="common/footer.jsp"/>
<script>
    var ORDERS = ${ordersJson};
    var ST_TXT = {0: '待支付', 1: '已支付待发货', 2: '已发货', 3: '已完成', 5: '已取消', 6: '已关闭'};
    // 所有动态字段先做 HTML 转义，再拼 innerHTML（收货人/地址等来自用户输入）
    function esc(v) {
        return String(v == null ? '' : v).replace(/[&<>"']/g, function (c) {
            return {'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'}[c];
        });
    }
    function search() {
        var url = CTX + '/admin/orders?page=1';
        if (document.getElementById('kwOrder').value) url += '&orderNo=' + encodeURIComponent(document.getElementById('kwOrder').value);
        if (document.getElementById('statusSel').value !== '') url += '&status=' + document.getElementById('statusSel').value;
        location.href = url;
    }
    function ship(no) {
        if (!confirm('确认为订单 ' + no + ' 发货？')) return;
        post('/admin/api/orders/ship', {orderNo: no}, function () { location.reload(); });
    }
    function showDetail(no) {
        var o = ORDERS[no];
        var html = '<b>订单号：</b>' + esc(o.orderNo) + '（' + esc(ST_TXT[o.status] || o.status) + '）<br>' +
            '<b>下单人：</b>' + esc(o.username) + '　<b>下单时间：</b>' + esc(o.createdAt) + '<br>' +
            (o.payTime ? '<b>支付时间：</b>' + esc(o.payTime) + '<br>' : '') +
            '<b>收货：</b>' + esc(o.receiver) + '　' + esc(o.phone) + '<br>' +
            '<b>地址：</b>' + esc(o.address) + '<hr style="border-color:#f0f0f0">';
        o.items.forEach(function (it) {
            html += esc(it.name) + '　¥' + esc(it.price) + ' × ' + esc(it.qty) + ' = <b>¥' + esc(it.subtotal) + '</b><br>';
        });
        html += '<div style="text-align:right;margin-top:8px;font-size:16px">实付款：<span style="color:#ff5000;font-weight:700">¥' + esc(o.totalAmount) + '</span></div>';
        document.getElementById('detailBody').innerHTML = html;
        document.getElementById('detailMask').classList.add('show');
    }
    function closeDetail() { document.getElementById('detailMask').classList.remove('show'); }
</script>
