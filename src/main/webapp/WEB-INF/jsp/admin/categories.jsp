<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageName" value="categories" scope="request"/>
<c:set var="pageTitle" value="分类管理" scope="request"/>
<jsp:include page="common/header.jsp"/>

<div class="toolbar">
    <span style="color:#999">商品分类展示在商城首页与搜索页顶部导航</span>
    <span style="flex:1"></span>
    <button class="btn btn-ai" onclick="openEdit(null)">+ 新增分类</button>
</div>

<table class="table">
    <thead><tr><th style="width:70px">ID</th><th style="width:80px">图标</th><th>分类名称</th>
        <th style="width:100px">排序</th><th style="width:100px">状态</th><th style="width:150px">操作</th></tr></thead>
    <tbody>
    <c:forEach items="${categories}" var="cat">
        <tr>
            <td>${cat.id}</td>
            <td style="font-size:20px">${cat.icon}</td>
            <td>${cat.name}</td>
            <td>${cat.sort}</td>
            <td><span class="tag ${cat.status == 1 ? 'tag-green' : 'tag-gray'}">${cat.status == 1 ? '启用' : '禁用'}</span></td>
            <td class="ops">
                <a href="javascript:void(0)" onclick="openEdit(${cat.id})">编辑</a>
                <a class="danger" href="javascript:void(0)" onclick="del(${cat.id})">删除</a>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>

<div class="modal-mask" id="editMask">
    <div class="modal" style="width:420px">
        <h3 id="editTitle">新增分类</h3>
        <input type="hidden" id="f_id">
        <div class="form-item"><label>分类名称 *</label><input id="f_name" placeholder="如：数码配件"></div>
        <div class="form-row">
            <div class="form-item"><label>图标（emoji）</label><input id="f_icon" placeholder="如：🎧"></div>
            <div class="form-item"><label>排序（越小越靠前）</label><input id="f_sort" type="number" value="0"></div>
        </div>
        <div class="form-item"><label>状态</label>
            <select id="f_status"><option value="1">启用</option><option value="0">禁用</option></select>
        </div>
        <div class="foot">
            <button class="btn btn-plain" onclick="closeEdit()">取消</button>
            <button class="btn btn-primary" onclick="save()">保存</button>
        </div>
    </div>
</div>

<jsp:include page="common/footer.jsp"/>
<script>
    var CATS = {};
    <c:forEach items="${categories}" var="cat">
    CATS['${cat.id}'] = {id: '${cat.id}', name: '${cat.name}', icon: '${cat.icon}', sort: '${cat.sort}', status: '${cat.status}'};
    </c:forEach>
    function openEdit(id) {
        if (id) {
            var c = CATS[id];
            document.getElementById('editTitle').textContent = '编辑分类 #' + c.id;
            document.getElementById('f_id').value = c.id;
            document.getElementById('f_name').value = c.name;
            document.getElementById('f_icon').value = c.icon;
            document.getElementById('f_sort').value = c.sort;
            document.getElementById('f_status').value = c.status;
        } else {
            document.getElementById('editTitle').textContent = '新增分类';
            document.getElementById('f_id').value = '';
            document.getElementById('f_name').value = '';
            document.getElementById('f_icon').value = '';
            document.getElementById('f_sort').value = 0;
            document.getElementById('f_status').value = 1;
        }
        document.getElementById('editMask').classList.add('show');
    }
    function closeEdit() { document.getElementById('editMask').classList.remove('show'); }
    function save() {
        var data = {
            name: document.getElementById('f_name').value.trim(),
            icon: document.getElementById('f_icon').value.trim(),
            sort: Number(document.getElementById('f_sort').value),
            status: Number(document.getElementById('f_status').value)
        };
        if (!data.name) { alert('请填写分类名称'); return; }
        var id = document.getElementById('f_id').value;
        if (id) { data.id = Number(id); post('/admin/api/categories/update', data, ok); }
        else { post('/admin/api/categories/add', data, ok); }
        function ok() { location.reload(); }
    }
    function del(id) {
        if (!confirm('确定删除该分类？（分类下有商品时会被拒绝）')) return;
        post('/admin/api/categories/delete', {id: id}, function () { location.reload(); });
    }
</script>
