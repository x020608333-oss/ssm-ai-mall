<%@ page contentType="text/html;charset=UTF-8" %>
        </div><!-- /admin-content -->
    </div><!-- /admin-main -->
</div><!-- /admin-layout -->
<script>
    var CTX = '${ctx}';
    function post(url, data, done) {
        fetch(CTX + url, {method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(data)})
            .then(function (r) { return r.json(); })
            .then(function (res) { res.code === 200 ? done(res) : alert(res.msg); });
    }
</script>
</body>
</html>
