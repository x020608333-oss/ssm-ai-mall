<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>

<div class="chat-shell">
    <div class="chat-window">
        <div class="chat-head">
            <div class="bot-avatar">🤖</div>
            <div>
                <b>智选 AI 客服</b>
                <small>大模型 + 商品库检索 + 规则引擎 · 全天候在线</small>
            </div>
        </div>
        <div class="chat-body" id="chatBody"></div>
        <div class="chat-foot">
            <input id="msgInput" type="text" maxlength="500" placeholder="输入问题，如：有什么手机推荐？什么时候发货？"
                   onkeydown="if(event.key==='Enter')send()">
            <button class="send" onclick="send()">发送</button>
        </div>
    </div>
    <div class="quick-asks">
        <span onclick="quickAsk(this)">有什么值得买的推荐？</span>
        <span onclick="quickAsk(this)">什么时候发货？</span>
        <span onclick="quickAsk(this)">小米14 Pro多少钱？</span>
        <span onclick="quickAsk(this)">怎么退款退货？</span>
        <span onclick="quickAsk(this)">有优惠活动吗？</span>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
<script>
    var body = document.getElementById('chatBody');
    var historyLoaded = false;

    function addMsg(role, text, source) {
        var div = document.createElement('div');
        div.className = 'msg ' + (role === 'user' ? 'me' : 'bot');
        var avatar = document.createElement('div');
        avatar.className = 'avatar';
        avatar.textContent = role === 'user' ? '我' : '🤖';
        var bubble = document.createElement('div');
        bubble.className = 'bubble';
        bubble.textContent = text;
        if (source && role !== 'user') {
            var tag = document.createElement('span');
            tag.className = 'src';
            tag.textContent = '来源：' + ({llm: '大模型', db: '商品库实时检索', rule: '规则引擎', fallback: '兜底话术'}[source] || source);
            bubble.appendChild(document.createElement('br'));
            bubble.appendChild(tag);
        }
        div.appendChild(avatar);
        div.appendChild(bubble);
        body.appendChild(div);
        body.scrollTop = body.scrollHeight;
    }

    function showTyping() {
        var div = document.createElement('div');
        div.className = 'msg bot';
        div.id = 'typing';
        div.innerHTML = '<div class="avatar">🤖</div><div class="bubble typing">AI客服正在思考…</div>';
        body.appendChild(div);
        body.scrollTop = body.scrollHeight;
    }
    function hideTyping() {
        var t = document.getElementById('typing');
        if (t) t.remove();
    }

    function send() {
        var input = document.getElementById('msgInput');
        var text = input.value.trim();
        if (!text) return;
        input.value = '';
        addMsg('user', text);
        showTyping();
        fetch(CTX + '/api/chat/send', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({message: text})
        }).then(function (r) { return r.json(); }).then(function (res) {
            hideTyping();
            if (res.code === 200) {
                addMsg('assistant', res.data.content, res.data.source);
            } else {
                addMsg('assistant', '抱歉，系统开小差了：' + res.msg);
            }
        }).catch(function () {
            hideTyping();
            addMsg('assistant', '网络异常，请稍后重试');
        });
    }

    function quickAsk(el) {
        document.getElementById('msgInput').value = el.textContent;
        send();
    }

    // 载入历史会话
    fetch(CTX + '/api/chat/history').then(function (r) { return r.json(); }).then(function (res) {
        if (res.data && res.data.length) {
            res.data.forEach(function (m) { addMsg(m.role, m.content, m.role === 'assistant' ? m.source : null); });
        } else {
            addMsg('assistant', '您好，我是智选商城AI客服助手 🤖\n可以帮您：查商品、比价格、讲政策（物流/退换/支付）。\n试试点击下方快捷提问吧~');
        }
        historyLoaded = true;
    }).catch(function () {
        addMsg('assistant', '您好，我是智选商城AI客服助手 🤖\n请问有什么可以帮您？');
    });
</script>
