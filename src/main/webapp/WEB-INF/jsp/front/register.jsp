<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="form-page">
    <h2>注册智选商城</h2>
    <div class="err-tip" id="errTip"></div>
    <div class="form-item">
        <label>用户名</label>
        <input id="username" type="text" placeholder="3~20位字母/数字">
    </div>
    <div class="form-item">
        <label>昵称（选填）</label>
        <input id="nickname" type="text" placeholder="展示在商城中的名字">
    </div>
    <div class="form-item">
        <label>密码</label>
        <input id="password" type="password" placeholder="至少6位">
    </div>
    <div class="form-item">
        <label>确认密码</label>
        <input id="password2" type="password" placeholder="再次输入密码">
    </div>
    <button class="btn btn-primary" onclick="doRegister()">注 册</button>
    <div class="form-tip">已有账号？<a href="${ctx}/login">直接登录</a></div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
<script>
    function doRegister() {
        var err = document.getElementById('errTip');
        err.style.display = 'none';
        var p1 = document.getElementById('password').value;
        if (p1 !== document.getElementById('password2').value) {
            err.textContent = '两次输入的密码不一致';
            err.style.display = 'block';
            return;
        }
        fetch(CTX + '/api/auth/register', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({
                username: document.getElementById('username').value.trim(),
                nickname: document.getElementById('nickname').value.trim(),
                password: p1
            })
        }).then(function (r) { return r.json(); }).then(function (res) {
            if (res.code === 200) {
                alert('注册成功，请登录');
                location.href = CTX + '/login';
            } else {
                err.textContent = res.msg;
                err.style.display = 'block';
            }
        }).catch(function () {
            err.textContent = '网络异常，请稍后重试';
            err.style.display = 'block';
        });
    }
</script>
