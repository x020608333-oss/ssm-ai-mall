<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="section-title" style="margin:6px 0 14px">📦 我的订单</div>

<div class="cat-nav" style="border-radius:10px">
    <div class="container" style="padding:0 12px">
        <a href="${ctx}/orders" class="${empty status ? 'active' : ''}">全部</a>
        <a href="${ctx}/orders?status=0" class="${status == 0 ? 'active' : ''}">待支付</a>
        <a href="${ctx}/orders?status=1" class="${status == 1 ? 'active' : ''}">待发货</a>
        <a href="${ctx}/orders?status=2" class="${status == 2 ? 'active' : ''}">已发货</a>
        <a href="${ctx}/orders?status=3" class="${status == 3 ? 'active' : ''}">已完成</a>
        <a href="${ctx}/orders?status=5" class="${status == 5 ? 'active' : ''}">已取消</a>
    </div>
</div>

<c:choose>
    <c:when test="${empty pageResult.list}">
        <div class="section"><div class="empty">
            <div class="icon">📦</div><p>暂无相关订单</p>
            <a class="btn btn-primary btn-sm" href="${ctx}/search" style="margin-top:12px">去下单</a>
        </div></div>
    </c:when>
    <c:otherwise>
        <c:forEach items="${pageResult.list}" var="order">
            <div class="order-card">
                <div class="head">
                    <span>订单号：${order.orderNo}　下单时间：${order.createdAt}</span>
                    <span class="st st-${order.status}">
                        <c:choose>
                            <c:when test="${order.status == 0}">待支付</c:when>
                            <c:when test="${order.status == 1}">已支付，待发货</c:when>
                            <c:when test="${order.status == 2}">已发货</c:when>
                            <c:when test="${order.status == 3}">已完成</c:when>
                            <c:when test="${order.status == 5}">已取消</c:when>
                            <c:otherwise>已关闭</c:otherwise>
                        </c:choose>
                    </span>
                </div>
                <c:forEach items="${order.items}" var="item" varStatus="s">
                    <c:if test="${s.index < 3}">
                        <div class="body" style="padding:10px 20px 0">
                            <img src="${ctx}${item.productImage}" alt="">
                            <div class="oitems">
                                <div>${item.productName}</div>
                                <small style="color:#999">¥${item.price} × ${item.quantity}</small>
                            </div>
                        </div>
                    </c:if>
                </c:forEach>
                <c:if test="${fn:length(order.items) > 3}">
                    <div style="padding:8px 20px 0;color:#999;font-size:12px">… 还有 ${fn:length(order.items) - 3} 件商品</div>
                </c:if>
                <div class="body" style="padding:12px 20px 16px">
                    <div class="oamount">
                        <span style="color:#999;font-size:12px">共 <c:forEach items="${order.items}" var="it" varStatus="s">${it.quantity}<c:if test="${not s.last}">+</c:if></c:forEach> 件</span><br>
                        <span class="price">${order.totalAmount}</span>
                    </div>
                    <div class="ops">
                        <a class="btn btn-ghost btn-sm" href="${ctx}/order/${order.orderNo}">订单详情</a>
                        <c:choose>
                            <c:when test="${order.status == 0}">
                                <a class="btn btn-primary btn-sm" href="${ctx}/pay?orderNo=${order.orderNo}">去支付</a>
                                <a class="btn btn-danger btn-sm" href="javascript:void(0)" onclick="cancelOrder('${order.orderNo}')">取消订单</a>
                            </c:when>
                            <c:when test="${order.status == 2}">
                                <a class="btn btn-ai btn-sm" href="javascript:void(0)" onclick="confirmOrder('${order.orderNo}')">确认收货</a>
                            </c:when>
                        </c:choose>
                    </div>
                </div>
            </div>
        </c:forEach>

        <c:if test="${pageResult.total > pageResult.pageSize}">
            <div class="pagination">
                <c:set var="base" value="${ctx}/orders?page="/>
                <c:if test="${pageResult.pageNum > 1}">
                    <a href="${base}${pageResult.pageNum - 1}<c:if test="${not empty status}">&status=${status}</c:if>">‹ 上一页</a>
                </c:if>
                <c:forEach begin="1" end="${pageResult.total / pageResult.pageSize + (pageResult.total % pageResult.pageSize > 0 ? 1 : 0)}" var="i">
                    <a href="${base}${i}<c:if test="${not empty status}">&status=${status}</c:if>" class="${i == pageResult.pageNum ? 'cur' : ''}">${i}</a>
                </c:forEach>
                <c:if test="${pageResult.pageNum < pageResult.total / pageResult.pageSize + (pageResult.total % pageResult.pageSize > 0 ? 1 : 0)}">
                    <a href="${base}${pageResult.pageNum + 1}<c:if test="${not empty status}">&status=${status}</c:if>">下一页 ›</a>
                </c:if>
            </div>
        </c:if>
    </c:otherwise>
</c:choose>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
<script>
    function post(url, data, done) {
        fetch(CTX + url, {method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(data)})
            .then(function (r) { return r.json(); })
            .then(function (res) { res.code === 200 ? done(res) : alert(res.msg); });
    }
    function cancelOrder(no) {
        if (!confirm('确定取消该订单？已锁定库存将归还。')) return;
        post('/api/order/cancel', {orderNo: no}, function () { location.reload(); });
    }
    function confirmOrder(no) {
        if (!confirm('确认已收到货？')) return;
        post('/api/order/confirm', {orderNo: no}, function () { location.reload(); });
    }
</script>
