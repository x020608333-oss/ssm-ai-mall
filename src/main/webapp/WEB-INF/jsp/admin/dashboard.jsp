<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageName" value="dashboard" scope="request"/>
<c:set var="pageTitle" value="仪表盘" scope="request"/>
<jsp:include page="common/header.jsp"/>

<div class="stat-cards">
    <div class="stat-card">
        <div class="ico bg-1">🧾</div>
        <div><div class="val">${todayOrderCount}</div><div class="lbl">今日订单数</div></div>
    </div>
    <div class="stat-card">
        <div class="ico bg-3">💰</div>
        <div><div class="val" style="color:#52c41a">¥${todaySales}</div><div class="lbl">今日销售额（已支付订单）</div></div>
    </div>
    <div class="stat-card">
        <div class="ico bg-2">👥</div>
        <div><div class="val">${userCount}</div><div class="lbl">注册用户数</div></div>
    </div>
    <div class="stat-card">
        <div class="ico bg-4">⏳</div>
        <div><div class="val">${unpaidCount}</div><div class="lbl">待支付订单</div></div>
    </div>
</div>

<div class="panel-grid">
    <div class="panel">
        <h3>📈 近7天销售额趋势</h3>
        <canvas id="salesChart" height="150"></canvas>
    </div>
    <div class="panel">
        <h3>🧭 商品分类销售占比</h3>
        <c:choose>
            <c:when test="${empty categoryStats}">
                <div class="empty">暂无销售数据</div>
            </c:when>
            <c:otherwise>
                <c:forEach items="${categoryStats}" var="cat" varStatus="st">
                    <div style="display:flex;align-items:center;gap:10px;margin-bottom:10px">
                        <span style="width:70px;color:#666;font-size:13px">${cat.name}</span>
                        <div style="flex:1;background:#f5f5f5;border-radius:6px;height:14px;overflow:hidden">
                            <div style="width:${cat.cnt * 100 / categoryStats[0].cnt}%;height:100%;
                                background:linear-gradient(90deg,#6a5cff,#00c6a0)"></div>
                        </div>
                        <b style="font-size:13px">${cat.cnt}</b>
                    </div>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<div class="panel">
    <h3>🤖 AI 能力一览</h3>
    <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:14px">
        <div style="border:1px solid #f0f0f0;border-radius:10px;padding:14px">
            <b>个性化推荐引擎</b>
            <p style="color:#999;font-size:12px;margin-top:6px">ItemCF 协同过滤：基于 behavior_log 行为数据（浏览/加购/购买加权），为用户计算"猜你喜欢"，冷启动自动降级热门榜。</p>
        </div>
        <div style="border:1px solid #f0f0f0;border-radius:10px;padding:14px">
            <b>AI 智能客服</b>
            <p style="color:#999;font-size:12px;margin-top:6px">OpenAI 兼容大模型 + 商品库实时检索（简易RAG）+ 规则引擎三级兜底，对话全量入库可复盘。</p>
        </div>
        <div style="border:1px solid #f0f0f0;border-radius:10px;padding:14px">
            <b>行为数据采集</b>
            <p style="color:#999;font-size:12px;margin-top:6px">用户浏览、加购、购买行为实时写入 behavior_log，是推荐与客服的数据底座。</p>
        </div>
    </div>
</div>

<jsp:include page="common/footer.jsp"/>
<script src="${ctx}/static/js/chart.min.js"></script>
<script>
    var labels = [<c:forEach items="${dailyStats}" var="d" varStatus="s">'${d.date}'<c:if test="${not s.last}">,</c:if></c:forEach>];
    var amounts = [<c:forEach items="${dailyStats}" var="d" varStatus="s">${d.amount}<c:if test="${not s.last}">,</c:if></c:forEach>];
    var cnts = [<c:forEach items="${dailyStats}" var="d" varStatus="s">${d.cnt}<c:if test="${not s.last}">,</c:if></c:forEach>];
    new Chart(document.getElementById('salesChart'), {
        type: 'line',
        data: {
            labels: labels,
            datasets: [
                {label: '销售额(元)', data: amounts, borderColor: '#ff5000', backgroundColor: 'rgba(255,80,0,.08)', fill: true, tension: .35, yAxisID: 'y'},
                {label: '订单数', data: cnts, borderColor: '#6a5cff', backgroundColor: 'rgba(106,92,255,.08)', fill: true, tension: .35, yAxisID: 'y1'}
            ]
        },
        options: {
            responsive: true,
            interaction: {mode: 'index', intersect: false},
            scales: {
                y: {position: 'left', beginAtZero: true},
                y1: {position: 'right', beginAtZero: true, grid: {drawOnChartArea: false}}
            }
        }
    });
</script>
