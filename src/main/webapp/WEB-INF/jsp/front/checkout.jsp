<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="section-title" style="margin:6px 0 14px">✅ 确认订单</div>

<div class="checkout-grid">
    <div>
        <section class="section">
            <div class="section-title"><span>收货地址</span>
                <a href="javascript:void(0)" onclick="toggleAddrForm()">+ 新增地址</a>
            </div>
            <div id="addrForm" style="display:none;margin-bottom:14px;border:1px dashed #ddd;border-radius:10px;padding:14px">
                <div style="display:grid;grid-template-columns:1fr 1fr;gap:10px">
                    <input id="f_receiver" placeholder="收货人姓名" style="border:1px solid #ddd;border-radius:8px;padding:9px 12px">
                    <input id="f_phone" placeholder="手机号" style="border:1px solid #ddd;border-radius:8px;padding:9px 12px">
                    <input id="f_region" placeholder="省 市 区，如：广东省 深圳市 南山区" style="border:1px solid #ddd;border-radius:8px;padding:9px 12px">
                    <input id="f_detail" placeholder="详细地址（街道、门牌号）" style="border:1px solid #ddd;border-radius:8px;padding:9px 12px">
                </div>
                <button class="btn btn-primary btn-sm" style="margin-top:10px" onclick="saveAddr()">保存地址</button>
            </div>
            <c:forEach items="${addresses}" var="addr" varStatus="st">
                <div class="addr-card ${st.first ? 'selected' : ''}" data-id="${addr.id}" onclick="pickAddr(this)">
                    <div class="ops">
                        <c:if test="${addr.isDefault == 0}">
                            <a href="javascript:void(0)" onclick="setDefault(${addr.id});event.stopPropagation()">设为默认</a>
                        </c:if>
                        <a href="javascript:void(0)" onclick="delAddr(${addr.id});event.stopPropagation()">删除</a>
                    </div>
                    <b>${fn:escapeXml(addr.receiver)}</b>　${fn:escapeXml(addr.phone)}
                    <c:if test="${addr.isDefault == 1}"><span class="def">默认</span></c:if>
                    <div style="color:#666;margin-top:4px;font-size:13px">
                        ${fn:escapeXml(addr.province)} ${fn:escapeXml(addr.city)} ${fn:escapeXml(addr.district)} ${fn:escapeXml(addr.detail)}
                    </div>
                </div>
            </c:forEach>
            <c:if test="${empty addresses}">
                <p style="color:#999">还没有收货地址，点击右上角新增 →</p>
            </c:if>
        </section>

        <section class="section">
            <div class="section-title"><span>商品清单</span></div>
            <c:forEach items="${items}" var="item">
                <div class="cart-item" style="padding:10px 0">
                    <img src="${ctx}${fn:escapeXml(item.mainImage)}" alt="">
                    <div class="cname">
                        <div>${fn:escapeXml(item.productName)}</div>
                        <small style="color:#999">数量：${item.quantity}</small>
                    </div>
                    <div class="cprice">¥${item.price} × ${item.quantity}</div>
                </div>
            </c:forEach>
            <textarea id="remarkInput" placeholder="订单备注（选填）"
                      style="width:100%;border:1px solid #ddd;border-radius:8px;padding:10px;margin-top:8px;height:64px"></textarea>
        </section>
    </div>

    <div class="summary-card">
        <div class="row"><span>商品件数</span><span><c:forEach items="${items}" var="i" varStatus="s">${i.quantity}<c:if test="${not s.last}"> + </c:if></c:forEach> 件</span></div>
        <div class="row"><span>运费</span><span style="color:#52c41a">包邮</span></div>
        <div class="row big"><span>应付总额</span><span class="price"><fmt:formatNumber value="${total}" pattern="0.00"/></span></div>
        <button class="btn btn-primary" style="width:100%;margin-top:14px" onclick="submitOrder()">提交订单</button>
        <p style="color:#bbb;font-size:12px;margin-top:10px;text-align:center">提交后进入模拟支付页</p>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
<script>
    var BUY_NOW = ${buyNow};
    var selCard = document.querySelector('.addr-card.selected');
    var SELECTED_ADDR = selCard ? selCard.dataset.id : null;
    function pickAddr(el) {
        document.querySelectorAll('.addr-card').forEach(function (a) { a.classList.remove('selected'); });
        el.classList.add('selected');
        SELECTED_ADDR = el.dataset.id;
    }
    function toggleAddrForm() {
        var f = document.getElementById('addrForm');
        f.style.display = f.style.display === 'none' ? 'block' : 'none';
    }
    function saveAddr() {
        var region = document.getElementById('f_region').value.trim().split(/\s+/);
        var data = {
            receiver: document.getElementById('f_receiver').value.trim(),
            phone: document.getElementById('f_phone').value.trim(),
            province: region[0] || '', city: region[1] || '', district: region[2] || '',
            detail: document.getElementById('f_detail').value.trim(),
            isDefault: 0
        };
        if (!data.receiver || !data.phone || !data.detail) { alert('请完整填写收货信息'); return; }
        fetch(CTX + '/api/address/add', {method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(data)})
            .then(function (r) { return r.json(); })
            .then(function (res) { res.code === 200 ? location.reload() : alert(res.msg); });
    }
    function setDefault(id) {
        fetch(CTX + '/api/address/default', {method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify({id: id})})
            .then(function (r) { return r.json(); }).then(function (res) { res.code === 200 ? location.reload() : alert(res.msg); });
    }
    function delAddr(id) {
        if (!confirm('确定删除该地址？')) return;
        fetch(CTX + '/api/address/delete', {method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify({id: id})})
            .then(function (r) { return r.json(); }).then(function (res) { res.code === 200 ? location.reload() : alert(res.msg); });
    }
    function submitOrder() {
        if (!SELECTED_ADDR) { alert('请先选择收货地址'); return; }
        var url = BUY_NOW ? '/api/order/buyNow' : '/api/order/create';
        var body = {addressId: SELECTED_ADDR, remark: document.getElementById('remarkInput').value};
        if (BUY_NOW) {
            body.productId = ${items[0].productId};
            body.quantity = ${items[0].quantity};
        }
        fetch(CTX + url, {method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(body)})
            .then(function (r) { return r.json(); })
            .then(function (res) {
                if (res.code === 200) {
                    location.href = CTX + '/pay?orderNo=' + res.data.orderNo;
                } else {
                    alert(res.msg);
                }
            });
    }
</script>
