<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="detail-wrap">
    <img class="main-img" src="${ctx}${fn:escapeXml(product.mainImage)}" alt="${fn:escapeXml(product.name)}"
         onerror="this.src='${ctx}/static/img/placeholder.jpg'">
    <div class="detail-info">
        <h1>${fn:escapeXml(product.name)}</h1>
        <div class="sub">✨ ${fn:escapeXml(product.subtitle)}</div>
        <div class="detail-price">
            <span class="price">${product.price}</span>
            <span style="color:#999;margin-left:16px;font-size:13px">销量 ${product.sales} 件</span>
        </div>
        <div class="meta">📦 库存：<b id="stockNum">${product.stock}</b> 件</div>
        <div class="meta">🏷️ 分类：${fn:escapeXml(product.categoryName)}</div>
        <div class="meta">🤖 AI客服：<a href="${ctx}/chat" style="color:#6a5cff">有问题？问我就对了</a></div>
        <div class="buy-row">
            <span class="qty-ctrl">
                <button type="button" onclick="qty(-1)">-</button>
                <input id="qtyInput" type="text" value="1" readonly>
                <button type="button" onclick="qty(1)">+</button>
            </span>
        </div>
        <div class="buy-row">
            <c:choose>
                <c:when test="${product.status == 1}">
                    <button class="btn btn-ghost" onclick="addCart(false)">加入购物车</button>
                    <button class="btn btn-primary" onclick="addCart(true)">立即购买</button>
                </c:when>
                <c:otherwise>
                    <span class="btn btn-disabled">商品已下架</span>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<div class="detail-desc">
    <h3 style="margin-bottom:12px">商品详情</h3>
    ${fn:escapeXml(product.detail)}
</div>

<c:if test="${not empty related}">
    <section class="section" style="margin-top:20px">
        <div class="section-title"><span>🤖 买了又买（AI关联推荐）</span></div>
        <div class="grid">
            <c:forEach items="${related}" var="product">
                <%@ include file="/WEB-INF/jsp/common/product-card.jsp" %>
            </c:forEach>
        </div>
    </section>
</c:if>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
<script>
    var PRODUCT_ID = ${product.id};
    var QTY = 1;
    function qty(d) {
        QTY = Math.max(1, Math.min(99, QTY + d));
        document.getElementById('qtyInput').value = QTY;
    }
    function addCart(buyNow) {
        if (!IS_LOGIN) { location.href = CTX + '/login?redirect=' + encodeURIComponent(location.pathname); return; }
        fetch(CTX + '/api/cart/add', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({productId: PRODUCT_ID, quantity: QTY})
        }).then(function (r) { return r.json(); }).then(function (res) {
            if (res.code === 200) {
                if (buyNow) {
                    location.href = CTX + '/checkout?buyProductId=' + PRODUCT_ID + '&buyQuantity=' + QTY;
                } else {
                    location.href = CTX + '/cart';
                }
            } else { alert(res.msg); }
        });
    }
</script>
