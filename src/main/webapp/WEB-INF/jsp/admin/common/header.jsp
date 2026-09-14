<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" scope="request"/>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>管理后台 - 智选AI商城</title>
    <link rel="stylesheet" href="${ctx}/static/css/admin.css">
</head>
<body>
<div class="admin-layout">
    <aside class="sidebar">
        <div class="brand">智选<span class="ai">AI</span>商城 · 后台</div>
        <nav>
            <a href="${ctx}/admin" class="${pageName == 'dashboard' ? 'active' : ''}">📊 仪表盘</a>
            <a href="${ctx}/admin/products" class="${pageName == 'products' ? 'active' : ''}">📦 商品管理</a>
            <a href="${ctx}/admin/categories" class="${pageName == 'categories' ? 'active' : ''}">🗂️ 分类管理</a>
            <a href="${ctx}/admin/orders" class="${pageName == 'orders' ? 'active' : ''}">🧾 订单管理</a>
            <a href="${ctx}/admin/users" class="${pageName == 'users' ? 'active' : ''}">👥 用户管理</a>
        </nav>
        <div class="bottom">
            <a href="${ctx}/" style="color:#a6adb4">← 返回商城前台</a>
        </div>
        <div style="height:56px"></div>
    </aside>
    <div class="admin-main">
        <header class="admin-top">
            <span class="title">${pageTitle}</span>
            <div class="right">
                <span>👤 ${sessionScope.loginUser.nickname}（管理员）</span>
            </div>
        </header>
        <div class="admin-content">
