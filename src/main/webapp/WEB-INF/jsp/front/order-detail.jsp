<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="pay-box" style="max-width:720px;text-align:left">
    <div style="display:flex;justify-content:space-between;align-items:center">
        <h2>订单详情</h2>
        <span class="st st-${order.status}" style="font-size:18px;font-weight:700">
            <c:choose>
                <c:when test="${order.status == 0}">⏳ 待支付</c:when>
                <c:when test="${order.status == 1}">✅ 已支付，待发货</c:when>
                <c:when test="${order.status == 2}">🚚 已发货</c:when>
                <c:when test="${order.status == 3}">🎉 已完成</c:when>
                <c:when test="${order.status == 5}">已取消</c:when>
                <c:otherwise>已关闭</c:otherwise>
            </c:choose>
        </span>
    </div>
    <c:if test="${param.paid == '1'}">
        <div style="background:#f6ffed;border:1px solid #b7eb8f;color:#52c41a;border-radius:8px;padding:10px 14px;margin-top:12px">
            支付成功！商家将尽快为您发货 🎊
        </div>
    </c:if>

    <div style="background:#fafafa;border-radius:10px;padding:14px 18px;margin:16px 0;font-size:13px;color:#555;line-height:2">
        <div>订单编号：<b>${fn:escapeXml(order.orderNo)}</b></div>
        <div>下单时间：${order.createdAt}　<c:if test="${not empty order.payTime}">支付时间：${order.payTime}</c:if></div>
        <div>收货信息：${fn:escapeXml(order.receiver)}　${fn:escapeXml(order.phone)}　${fn:escapeXml(order.addressSnapshot)}</div>
        <c:if test="${not empty order.remark}"><div>订单备注：${fn:escapeXml(order.remark)}</div></c:if>
    </div>

    <c:forEach items="${order.items}" var="item">
        <div class="cart-item">
            <img src="${ctx}${fn:escapeXml(item.productImage)}" alt="">
            <div class="cname">
                <a href="${ctx}/product/${item.productId}">${fn:escapeXml(item.productName)}</a>
                <div><small style="color:#999">单价 ¥${item.price} × ${item.quantity}</small></div>
            </div>
            <div class="csum">¥${item.subtotal}</div>
        </div>
    </c:forEach>

    <div style="text-align:right;margin-top:16px;font-size:16px">
        实付款：<span class="price" style="font-size:26px">${order.totalAmount}</span>
    </div>

    <div class="buy-row" style="justify-content:flex-end">
        <c:choose>
            <c:when test="${order.status == 0}">
                <a class="btn btn-danger" href="javascript:void(0)" onclick="cancelOrder('${fn:escapeXml(order.orderNo)}')">取消订单</a>
                <a class="btn btn-primary" href="${ctx}/pay?orderNo=${fn:escapeXml(order.orderNo)}">去支付</a>
            </c:when>
            <c:when test="${order.status == 2}">
                <a class="btn btn-ai" href="javascript:void(0)" onclick="confirmOrder('${fn:escapeXml(order.orderNo)}')">确认收货</a>
            </c:when>
        </c:choose>
        <a class="btn btn-ghost" href="${ctx}/orders">返回订单列表</a>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
<script>
    function post(url, data, done) {
        fetch(CTX + url, {method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(data)})
            .then(function (r) { return r.json(); })
            .then(function (res) { res.code === 200 ? done(res) : alert(res.msg); });
    }
    function cancelOrder(no) {
        if (!confirm('确定取消该订单？')) return;
        post('/api/order/cancel', {orderNo: no}, function () { location.reload(); });
    }
    function confirmOrder(no) {
        if (!confirm('确认已收到货？')) return;
        post('/api/order/confirm', {orderNo: no}, function () { location.reload(); });
    }
</script>
