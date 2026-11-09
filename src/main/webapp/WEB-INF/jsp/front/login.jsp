<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="form-page">
    <h2>登录智选商城</h2>
    <div class="err-tip" id="errTip"></div>
    <input type="hidden" id="redirect" value="${fn:escapeXml(param.redirect)}">
    <div class="form-item">
        <label>用户名</label>
        <input id="username" type="text" placeholder="3~20位用户名">
    </div>
    <div class="form-item">
        <label>密码</label>
        <input id="password" type="password" placeholder="至少6位">
    </div>
    <button class="btn btn-primary" onclick="doLogin()">登 录</button>
    <div class="form-tip">还没有账号？<a href="${ctx}/register">立即注册</a></div>
    <div class="form-tip" style="background:#f7f8fc;border-radius:8px;padding:8px;line-height:1.8">
        演示账号：user1 / 123456　管理员：admin / 123456
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
<script>
    function doLogin() {
        var err = document.getElementById('errTip');
        err.style.display = 'none';
        fetch(CTX + '/api/auth/login', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({
                username: document.getElementById('username').value.trim(),
                password: document.getElementById('password').value,
                redirect: document.getElementById('redirect').value
            })
        }).then(function (r) { return r.json(); }).then(function (res) {
            if (res.code === 200) {
                location.href = CTX + res.data.redirect;
            } else {
                err.textContent = res.msg;
                err.style.display = 'block';
            }
        }).catch(function () {
            err.textContent = '网络异常，请稍后重试';
            err.style.display = 'block';
        });
    }
    document.addEventListener('keydown', function (e) { if (e.key === 'Enter') doLogin(); });
</script>
