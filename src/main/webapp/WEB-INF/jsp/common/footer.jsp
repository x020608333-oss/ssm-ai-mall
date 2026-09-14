<%@ page contentType="text/html;charset=UTF-8" %>
</main>
<footer class="footer">
    智选AI商城 · SSM（Spring+SpringMVC+MyBatis）+ AI 智能推荐/AI客服 课程设计项目 · 仅供学习
</footer>
<script>
    var CTX = '${ctx}';
    var IS_LOGIN = ${not empty sessionScope.loginUser ? 'true' : 'false'};
    function logout() {
        fetch(CTX + '/api/auth/logout', {method: 'POST'}).finally(function () { location.href = CTX + '/'; });
    }
    // 顶部购物车数量
    if (IS_LOGIN) {
        fetch(CTX + '/api/cart/count').then(function (r) { return r.json(); }).then(function (res) {
            if (res.data > 0) {
                var b = document.getElementById('cartBadge');
                b.textContent = res.data;
                b.style.display = 'inline-block';
            }
        }).catch(function(){});
    }
</script>
</body>
</html>
