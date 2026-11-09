<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="pay-box">
    <h2 style="color:#fa8c16">⏳ 等待支付</h2>
    <p style="color:#999;margin-top:8px">订单号：<span id="orderNoText">${fn:escapeXml(order.orderNo)}</span></p>
    <div class="amount">¥<c:choose><c:when test="${not empty order.totalAmount}">${order.totalAmount}</c:when><c:otherwise>0.00</c:otherwise></c:choose></div>
    <div class="pay-qrcode" title="模拟二维码"></div>
    <p style="color:#999;font-size:13px">（演示环境：无法真实调起微信/支付宝，点击下方按钮模拟支付成功回调）</p>
    <c:if test="${order.status == 0}">
        <button class="btn btn-primary" style="width:100%;margin-top:16px" onclick="mockPay()">模拟支付成功</button>
    </c:if>
    <c:if test="${order.status != 0}">
        <p style="color:#52c41a;margin-top:14px">该订单已支付 ✅ <a href="${ctx}/order/${fn:escapeXml(order.orderNo)}">查看订单</a></p>
    </c:if>
    <a href="${ctx}/order/${fn:escapeXml(order.orderNo)}" style="display:block;margin-top:14px;color:#999">先不付了，返回订单列表</a>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
<script>
    // 订单号从已转义的 DOM 文本读取，避免拼进 JS 字符串
    var ORDER_NO = document.getElementById('orderNoText').textContent;
    function mockPay() {
        fetch(CTX + '/api/pay/notify', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({orderNo: ORDER_NO})
        }).then(function (r) { return r.json(); }).then(function (res) {
            if (res.code === 200) {
                location.href = CTX + '/order/' + encodeURIComponent(ORDER_NO) + '?paid=1';
            } else {
                alert(res.msg);
                location.href = CTX + '/orders';
            }
        });
    }
</script>
