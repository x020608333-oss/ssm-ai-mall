<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
<div class="section">
    <div class="empty">
        <div class="icon">🛸</div>
        <h2>404 · 页面飞走了</h2>
        <p>您访问的页面不存在，回首页看看吧</p>
        <a class="btn btn-primary btn-sm" href="${ctx}/" style="margin-top:14px">回首页</a>
    </div>
</div>
<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
