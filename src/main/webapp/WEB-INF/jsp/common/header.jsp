<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" scope="request"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>智选商城 - AI 让购物更简单</title>
    <link rel="stylesheet" href="${ctx}/static/css/main.css">
</head>
<body>
<header class="topbar">
    <div class="container">
        <a class="logo" href="${ctx}/">智选<span class="ai">AI</span>商城</a>
        <form class="search-box" action="${ctx}/search" method="get">
            <input type="text" name="keyword" placeholder="搜索商品，如：手机 / 跑鞋 / 坚果" value="${param.keyword}">
            <button type="submit">搜索</button>
        </form>
        <nav class="nav-links">
            <a href="${ctx}/cart">🛒 购物车<span class="badge" id="cartBadge" style="display:none">0</span></a>
            <a href="${ctx}/orders">📦 我的订单</a>
            <a href="${ctx}/chat">🤖 AI客服</a>
            <c:choose>
                <c:when test="${not empty sessionScope.loginUser}">
                    <c:choose>
                        <c:when test="${sessionScope.loginUser.role == 'admin'}">
                            <a href="${ctx}/admin" style="color:#6a5cff;font-weight:600">⚙️ 管理后台</a>
                        </c:when>
                    </c:choose>
                    <a href="${ctx}/#" onclick="return false" title="欢迎回来">👤 ${sessionScope.loginUser.nickname}</a>
                    <a href="javascript:void(0)" onclick="logout()">退出</a>
                </c:when>
                <c:otherwise>
                    <a href="${ctx}/login">登录</a>
                    <a href="${ctx}/register" style="color:#ff5000;font-weight:600">注册</a>
                </c:otherwise>
            </c:choose>
        </nav>
    </div>
</header>
<c:if test="${not empty showCategories}">
    <nav class="cat-nav">
        <div class="container">
            <a href="${ctx}/search" class="${empty categoryId ? 'active' : ''}">全部</a>
            <c:forEach items="${categories}" var="cat">
                <a href="${ctx}/search?categoryId=${cat.id}"
                   class="${categoryId == cat.id ? 'active' : ''}">${cat.icon} ${cat.name}</a>
            </c:forEach>
        </div>
    </nav>
</c:if>
<main class="container" style="padding-top:16px">
