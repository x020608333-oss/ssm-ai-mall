<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageName" value="users" scope="request"/>
<c:set var="pageTitle" value="用户管理" scope="request"/>
<jsp:include page="common/header.jsp"/>

<div class="toolbar">
    <input id="kw" type="text" placeholder="搜索用户名/昵称/手机号" value="${fn:escapeXml(keyword)}" style="width:240px">
    <button class="btn btn-primary" onclick="search()">查询</button>
</div>

<table class="table">
    <thead><tr>
        <th style="width:70px">ID</th><th style="width:140px">用户名</th><th style="width:140px">昵称</th>
        <th style="width:140px">手机号</th><th style="width:170px">邮箱</th><th style="width:100px">角色</th>
        <th style="width:160px">注册时间</th><th style="width:100px">状态</th><th style="width:150px">操作</th>
    </tr></thead>
    <tbody>
    <c:forEach items="${result.list}" var="u">
        <tr>
            <td>${u.id}</td>
            <td>${fn:escapeXml(u.username)}</td>
            <td>${fn:escapeXml(u.nickname)}</td>
            <td>${fn:escapeXml(u.phone)}</td>
            <td>${fn:escapeXml(u.email)}</td>
            <td><span class="tag ${u.role == 'admin' ? 'tag-purple' : 'tag-blue'}">${u.role == 'admin' ? '管理员' : '普通用户'}</span></td>
            <td>${u.createdAt}</td>
            <td><span class="tag ${u.status == 1 ? 'tag-green' : 'tag-red'}">${u.status == 1 ? '正常' : '禁用'}</span></td>
            <td class="ops">
                <a href="javascript:void(0)" onclick="toggleStatus(${u.id}, ${u.status == 1 ? 0 : 1})">${u.status == 1 ? '禁用' : '启用'}</a>
                <a href="javascript:void(0)" onclick="toggleRole(${u.id}, '${u.role == 'admin' ? 'user' : 'admin'}')">改为${u.role == 'admin' ? '用户' : '管理员'}</a>
            </td>
        </tr>
    </c:forEach>
    <c:if test="${empty result.list}">
        <tr><td colspan="9"><div class="empty">暂无用户</div></td></tr>
    </c:if>
    </tbody>
</table>

<c:if test="${result.total > result.pageSize}">
    <div class="pagination">
        <c:set var="base" value="${ctx}/admin/users?keyword=${fn:escapeXml(keyword)}&page="/>
        <c:if test="${result.pageNum > 1}"><a href="${base}${result.pageNum - 1}">‹ 上一页</a></c:if>
        <span class="cur">${result.pageNum} / ${result.pages}</span>
        <c:if test="${result.pageNum < result.pages}"><a href="${base}${result.pageNum + 1}">下一页 ›</a></c:if>
    </div>
</c:if>

<jsp:include page="common/footer.jsp"/>
<script>
    function search() {
        var url = CTX + '/admin/users?page=1';
        if (document.getElementById('kw').value) url += '&keyword=' + encodeURIComponent(document.getElementById('kw').value);
        location.href = url;
    }
    function toggleStatus(id, status) {
        post('/admin/api/users/status', {id: id, status: status}, function () { location.reload(); });
    }
    function toggleRole(id, role) {
        if (!confirm('确定将该用户角色改为「' + (role === 'admin' ? '管理员' : '普通用户') + '」？')) return;
        post('/admin/api/users/role', {id: id, role: role}, function () { location.reload(); });
    }
</script>
