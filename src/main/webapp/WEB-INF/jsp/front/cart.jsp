<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="section-title" style="margin:6px 0 14px">🛒 我的购物车</div>

<c:choose>
    <c:when test="${empty items}">
        <div class="section"><div class="empty">
            <div class="icon">🛒</div>
            <p>购物车还是空的</p>
            <a class="btn btn-primary btn-sm" href="${ctx}/search" style="margin-top:12px">去逛逛</a>
        </div></div>
    </c:when>
    <c:otherwise>
        <div class="cart-list">
            <c:forEach items="${items}" var="item">
                <div class="cart-item">
                    <input type="checkbox" ${item.checked == 1 ? 'checked' : ''} class="chk"
                           data-id="${item.id}" style="width:18px;height:18px">
                    <a href="${ctx}/product/${item.productId}"><img src="${ctx}${fn:escapeXml(item.mainImage)}" alt=""></a>
                    <div class="cname">
                        <div>${fn:escapeXml(item.productName)}</div>
                        <c:choose>
                            <c:when test="${item.productStatus == 1}">
                                <small style="color:#999">单价 ¥${item.price} · 库存 ${item.stock}</small>
                            </c:when>
                            <c:otherwise>
                                <small class="off-shelf">该商品已下架，结算时将自动剔除</small>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <div class="cprice">¥${item.price}</div>
                    <div class="cqty">
                        <span class="qty-ctrl">
                            <button onclick="setQty(${item.id}, ${item.quantity - 1})">-</button>
                            <input type="text" value="${item.quantity}" readonly>
                            <button onclick="setQty(${item.id}, ${item.quantity + 1})">+</button>
                        </span>
                    </div>
                    <div class="csum" id="sum${item.id}">¥<fmt:formatNumber value="${item.price * item.quantity}" pattern="0.00"/></div>
                    <a class="cdel" href="javascript:void(0)" onclick="removeItem(${item.id})">删除</a>
                </div>
            </c:forEach>
        </div>
        <div class="cart-bar">
            <span class="total">合计：<span class="price" id="totalBar">0.00</span></span>
            <a class="btn btn-primary" href="${ctx}/checkout">去结算</a>
        </div>
    </c:otherwise>
</c:choose>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
<script>
    var CART_ITEM_PRICE = {}, CART_ITEM_QTY = {};
    <c:forEach items="${items}" var="item">
    CART_ITEM_PRICE['${item.id}'] = ${item.price};
    CART_ITEM_QTY['${item.id}'] = ${item.quantity};
    </c:forEach>
    function setQty(id, q) {
        if (q < 1) q = 1; if (q > 99) q = 99;
        post('/api/cart/quantity', {id: id, quantity: q}, function () { location.reload(); });
    }
    function removeItem(id) {
        if (!confirm('确定删除该商品吗？')) return;
        post('/api/cart/remove', {id: id}, function () { location.reload(); });
    }
    document.querySelectorAll('.chk').forEach(function (cb) {
        cb.addEventListener('change', function () {
            post('/api/cart/checked', {id: cb.dataset.id, checked: cb.checked}, function () { location.reload(); });
        });
    });
    function post(url, data, done) {
        fetch(CTX + url, {method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(data)})
            .then(function (r) { return r.json(); })
            .then(function (res) { res.code === 200 ? done(res) : alert(res.msg); });
    }
    // 结算金额预估（勾选项）
    (function () {
        var total = 0;
        <c:forEach items="${items}" var="item">
        <c:if test="${item.checked == 1}">
        total += CART_ITEM_PRICE['${item.id}'] * CART_ITEM_QTY['${item.id}'];
        </c:if>
        </c:forEach>
        document.getElementById('totalBar').textContent = total.toFixed(2);
    })();
</script>
