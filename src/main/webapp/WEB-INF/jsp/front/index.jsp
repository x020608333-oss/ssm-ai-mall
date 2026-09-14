<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<c:set var="showCategories" value="true" scope="request"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp">
    <jsp:param name="keyword" value="${keyword}"/>
</jsp:include>

<div class="hero">
    <h1>AI 让购物更简单 🤖</h1>
    <p>基于协同过滤的个性化推荐 + 大模型智能客服，帮你把想要的商品一次找齐。下单立减，全场包邮！</p>
    <a class="go" href="${ctx}/search">开始逛逛 →</a>
</div>

<section class="section">
    <div class="section-title">
        <span>${aiTitle}</span>
        <a href="${ctx}/chat">问AI客服有什么值得买 →</a>
    </div>
    <div class="grid">
        <c:forEach items="${aiProducts}" var="product">
            <%@ include file="/WEB-INF/jsp/common/product-card.jsp" %>
        </c:forEach>
    </div>
</section>

<section class="section">
    <div class="section-title"><span>🔥 热卖榜单</span><a href="${ctx}/search?sort=salesDesc">查看更多 →</a></div>
    <div class="grid">
        <c:forEach items="${hotProducts}" var="product">
            <%@ include file="/WEB-INF/jsp/common/product-card.jsp" %>
        </c:forEach>
    </div>
</section>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
