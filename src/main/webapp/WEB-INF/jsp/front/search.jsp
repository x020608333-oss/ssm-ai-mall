<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="showCategories" value="true" scope="request"/>
<jsp:include page="/WEB-INF/jsp/common/header.jsp">
    <jsp:param name="keyword" value="${keyword}"/>
</jsp:include>

<section class="section">
    <div class="section-title">
        <span>
            <c:choose>
                <c:when test="${not empty keyword}">“${keyword}” 的搜索结果</c:when>
                <c:when test="${not empty categoryId}">分类商品</c:when>
                <c:otherwise>全部商品</c:otherwise>
            </c:choose>
            <small style="color:#999;font-weight:400">（共 ${result.total} 件）</small>
        </span>
        <span>
            排序：
            <a href="${ctx}/search?keyword=${keyword}&categoryId=${categoryId}&sort=default" style="${empty sort or sort=='default'?'color:#ff5000;font-weight:600':'color:#666'}">综合</a> ·
            <a href="${ctx}/search?keyword=${keyword}&categoryId=${categoryId}&sort=salesDesc" style="${sort=='salesDesc'?'color:#ff5000;font-weight:600':'color:#666'}">销量</a> ·
            <a href="${ctx}/search?keyword=${keyword}&categoryId=${categoryId}&sort=priceAsc" style="${sort=='priceAsc'?'color:#ff5000;font-weight:600':'color:#666'}">价格↑</a> ·
            <a href="${ctx}/search?keyword=${keyword}&categoryId=${categoryId}&sort=priceDesc" style="${sort=='priceDesc'?'color:#ff5000;font-weight:600':'color:#666'}">价格↓</a>
        </span>
    </div>

    <c:choose>
        <c:when test="${empty result.list or result.total == 0}">
            <div class="empty">
                <div class="icon">🔍</div>
                <p>没有找到相关商品，换个关键词试试吧</p>
                <p><a href="${ctx}/chat" style="color:#6a5cff">问问AI客服帮你找 →</a></p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="grid">
                <c:forEach items="${result.list}" var="product">
                    <%@ include file="/WEB-INF/jsp/common/product-card.jsp" %>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>

    <c:if test="${result.total > result.pageSize}">
        <div class="pagination">
            <c:set var="base" value="${ctx}/search?keyword=${keyword}&categoryId=${categoryId}&sort=${sort}&page="/>
            <c:if test="${result.pageNum > 1}"><a href="${base}${result.pageNum - 1}">‹ 上一页</a></c:if>
            <c:forEach begin="1" end="${result.pages}" var="i">
                <c:if test="${i <= 10}">
                    <a href="${base}${i}" class="${i == result.pageNum ? 'cur' : ''}">${i}</a>
                </c:if>
            </c:forEach>
            <c:if test="${result.pageNum < result.pages}"><a href="${base}${result.pageNum + 1}">下一页 ›</a></c:if>
        </div>
    </c:if>
</section>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
