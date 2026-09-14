<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageName" value="products" scope="request"/>
<c:set var="pageTitle" value="商品管理" scope="request"/>
<jsp:include page="common/header.jsp"/>

<div class="toolbar">
    <input id="kw" type="text" placeholder="搜索商品名称" value="${keyword}" style="width:220px">
    <select id="catSel">
        <option value="">全部分类</option>
        <c:forEach items="${categories}" var="cat">
            <option value="${cat.id}" ${categoryId == cat.id ? 'selected' : ''}>${cat.icon} ${cat.name}</option>
        </c:forEach>
    </select>
    <button class="btn btn-primary" onclick="search()">查询</button>
    <span style="flex:1"></span>
    <button class="btn btn-ai" onclick="openEdit(null)">+ 新增商品</button>
</div>

<table class="table">
    <thead>
    <tr>
        <th style="width:60px">ID</th><th style="width:70px">图片</th><th>商品名称</th>
        <th style="width:90px">分类</th><th style="width:90px">价格</th><th style="width:70px">库存</th>
        <th style="width:70px">销量</th><th style="width:80px">状态</th><th style="width:150px">操作</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${result.list}" var="p">
        <tr>
            <td>${p.id}</td>
            <td><img class="thumb" src="${ctx}${p.mainImage}" onerror="this.src='${ctx}/static/img/placeholder.jpg'"></td>
            <td>
                ${p.name}
                <c:if test="${p.isHot == 1}"><span class="tag tag-orange">热卖</span></c:if>
            </td>
            <td>${p.categoryName}</td>
            <td style="color:#ff5000;font-weight:700">¥${p.price}</td>
            <td>${p.stock}</td>
            <td>${p.sales}</td>
            <td>
                <span class="tag ${p.status == 1 ? 'tag-green' : 'tag-gray'}">${p.status == 1 ? '上架' : '下架'}</span>
            </td>
            <td class="ops">
                <a href="javascript:void(0)" onclick="toggleStatus(${p.id}, ${p.status == 1 ? 0 : 1})">${p.status == 1 ? '下架' : '上架'}</a>
                <a href="javascript:void(0)" onclick="openEdit(${p.id})">编辑</a>
                <a class="danger" href="javascript:void(0)" onclick="del(${p.id})">删除</a>
            </td>
        </tr>
    </c:forEach>
    <c:if test="${empty result.list}">
        <tr><td colspan="9"><div class="empty">暂无商品</div></td></tr>
    </c:if>
    </tbody>
</table>

<c:if test="${result.total > result.pageSize}">
    <div class="pagination">
        <c:set var="base" value="${ctx}/admin/products?keyword=${keyword}&categoryId=${categoryId}&page="/>
        <c:if test="${result.pageNum > 1}"><a href="${base}${result.pageNum - 1}">‹ 上一页</a></c:if>
        <span class="cur">${result.pageNum} / ${result.pages}</span>
        <c:if test="${result.pageNum < result.pages}"><a href="${base}${result.pageNum + 1}">下一页 ›</a></c:if>
    </div>
</c:if>

<!-- 新增/编辑弹窗 -->
<div class="modal-mask" id="editMask">
    <div class="modal">
        <h3 id="editTitle">新增商品</h3>
        <input type="hidden" id="f_id">
        <div class="form-row">
            <div class="form-item"><label>商品名称 *</label><input id="f_name"></div>
            <div class="form-item"><label>分类 *</label>
                <select id="f_category">
                    <c:forEach items="${categories}" var="cat">
                        <option value="${cat.id}">${cat.icon} ${cat.name}</option>
                    </c:forEach>
                </select>
            </div>
        </div>
        <div class="form-item"><label>副标题/卖点</label><input id="f_subtitle"></div>
        <div class="form-row">
            <div class="form-item"><label>价格（元）*</label><input id="f_price" type="number" step="0.01" min="0"></div>
            <div class="form-item"><label>库存 *</label><input id="f_stock" type="number" min="0"></div>
        </div>
        <div class="form-row">
            <div class="form-item"><label>热卖推荐</label>
                <select id="f_hot"><option value="0">否</option><option value="1">是</option></select>
            </div>
            <div class="form-item"><label>上架状态</label>
                <select id="f_status"><option value="1">上架</option><option value="0">下架</option></select>
            </div>
        </div>
        <div class="form-item"><label>商品图（JPG/PNG，≤5MB）</label>
            <input id="f_file" type="file" accept="image/*">
            <img id="f_preview" style="width:80px;height:80px;border-radius:8px;margin-top:8px;display:none" alt="">
            <input type="hidden" id="f_image">
        </div>
        <div class="form-item"><label>商品详情</label><textarea id="f_detail" rows="3"></textarea></div>
        <div class="foot">
            <button class="btn btn-plain" onclick="closeEdit()">取消</button>
            <button class="btn btn-primary" onclick="save()">保存</button>
        </div>
    </div>
</div>

<jsp:include page="common/footer.jsp"/>
<script>
    // 保留商品数据供编辑回填
    var PRODUCTS = {};
    <c:forEach items="${result.list}" var="p">
    PRODUCTS['${p.id}'] = {
        id: '${p.id}', name: '${p.name}', categoryId: '${p.categoryId}', subtitle: '${p.subtitle}',
        price: '${p.price}', stock: '${p.stock}', isHot: '${p.isHot}', status: '${p.status}',
        mainImage: '${p.mainImage}', detail: '${p.detail}'
    };
    </c:forEach>

    function search() {
        var url = CTX + '/admin/products?page=1';
        if (document.getElementById('kw').value) url += '&keyword=' + encodeURIComponent(document.getElementById('kw').value);
        if (document.getElementById('catSel').value) url += '&categoryId=' + document.getElementById('catSel').value;
        location.href = url;
    }
    function toggleStatus(id, status) { post('/admin/api/products/update', {id: id, status: status}, function () { location.reload(); }); }
    function del(id) {
        if (!confirm('确定删除该商品？')) return;
        post('/admin/api/products/delete', {id: id}, function () { location.reload(); });
    }
    function openEdit(id) {
        var m = document.getElementById('editMask');
        document.getElementById('f_file').value = '';
        document.getElementById('f_preview').style.display = 'none';
        if (id) {
            var p = PRODUCTS[id];
            document.getElementById('editTitle').textContent = '编辑商品 #' + p.id;
            document.getElementById('f_id').value = p.id;
            document.getElementById('f_name').value = p.name;
            document.getElementById('f_category').value = p.categoryId;
            document.getElementById('f_subtitle').value = p.subtitle;
            document.getElementById('f_price').value = p.price;
            document.getElementById('f_stock').value = p.stock;
            document.getElementById('f_hot').value = p.isHot;
            document.getElementById('f_status').value = p.status;
            document.getElementById('f_detail').value = p.detail || '';
            document.getElementById('f_image').value = p.mainImage;
            if (p.mainImage) {
                var pv = document.getElementById('f_preview');
                pv.src = CTX + p.mainImage;
                pv.style.display = 'block';
            }
        } else {
            document.getElementById('editTitle').textContent = '新增商品';
            document.getElementById('f_id').value = '';
            ['f_name','f_subtitle','f_price','f_stock','f_detail'].forEach(function (x) { document.getElementById(x).value = ''; });
            document.getElementById('f_image').value = '/static/img/placeholder.jpg';
        }
        m.classList.add('show');
    }
    function closeEdit() { document.getElementById('editMask').classList.remove('show'); }

    // 上传图片
    document.getElementById('f_file').addEventListener('change', function () {
        var file = this.files[0];
        if (!file) return;
        var fd = new FormData();
        fd.append('file', file);
        fetch(CTX + '/admin/api/products/upload', {method: 'POST', body: fd})
            .then(function (r) { return r.json(); })
            .then(function (res) {
                if (res.code === 200) {
                    document.getElementById('f_image').value = res.data;
                    var pv = document.getElementById('f_preview');
                    pv.src = CTX + res.data;
                    pv.style.display = 'block';
                } else { alert(res.msg); }
            });
    });

    function save() {
        var data = {
            name: document.getElementById('f_name').value.trim(),
            categoryId: Number(document.getElementById('f_category').value),
            subtitle: document.getElementById('f_subtitle').value.trim(),
            price: Number(document.getElementById('f_price').value),
            stock: Number(document.getElementById('f_stock').value),
            isHot: Number(document.getElementById('f_hot').value),
            status: Number(document.getElementById('f_status').value),
            mainImage: document.getElementById('f_image').value,
            detail: document.getElementById('f_detail').value.trim()
        };
        if (!data.name) { alert('请填写商品名称'); return; }
        if (!data.price && data.price !== 0) { alert('请填写价格'); return; }
        var id = document.getElementById('f_id').value;
        var url = id ? '/admin/api/products/update' : '/admin/api/products/add';
        if (id) data.id = Number(id);
        post(url, data, function () { location.reload(); });
    }
</script>
