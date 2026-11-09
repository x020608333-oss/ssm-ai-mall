<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%-- 商品卡片（商品网格复用）：变量 product --%>
<a class="p-card" href="${ctx}/product/${product.id}">
    <img class="img" src="${ctx}${fn:escapeXml(product.mainImage)}" alt="${fn:escapeXml(product.name)}" onerror="this.src='${ctx}/static/img/placeholder.jpg'">
    <div class="info">
        <div class="name">${fn:escapeXml(product.name)}<c:if test="${product.isHot == 1}"><span class="ai-tag">HOT</span></c:if></div>
        <div class="sub">${fn:escapeXml(product.subtitle)}</div>
        <div class="row">
            <span class="price">${product.price}</span>
            <span class="sales">已售${product.sales}</span>
        </div>
    </div>
</a>
