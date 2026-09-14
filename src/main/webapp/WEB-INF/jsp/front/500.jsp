<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
<div class="section">
    <div class="empty">
        <div class="icon">🔧</div>
        <h2>500 · 服务器开小差了</h2>
        <p>页面出了点问题，请稍后再试</p>
        <a class="btn btn-primary btn-sm" href="${ctx}/" style="margin-top:14px">回首页</a>
    </div>
</div>
<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
