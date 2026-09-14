package com.shop.ai;

import com.shop.entity.ChatMessage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 规则引擎客服：关键词意图识别 + 固定话术。
 * 大模型不可用（未配Key/网络异常）时保证客服功能 100% 可用。
 */
@Component
public class RuleChatClient implements AiEngine {

    private static final Pattern LOGISTICS = Pattern.compile(".*(物流|快递|发货|什么时候到|几天到|配送|到货).*");
    private static final Pattern REFUND = Pattern.compile(".*(退款|退货|售后|换货|维修).*");
    private static final Pattern PAY = Pattern.compile(".*(支付|付款|扣款|收款).*");
    private static final Pattern COUPON = Pattern.compile(".*(优惠|折扣|便宜|券|活动|满减|包邮).*");
    private static final Pattern STOCK = Pattern.compile(".*(有货|缺货|库存|补货).*");
    private static final Pattern ACCOUNT = Pattern.compile(".*(注册|登录|密码|账号|修改).*");
    private static final Pattern RECOMMEND = Pattern.compile(".*(推荐|什么值得买|买什么|爆款|热卖).*");

    @Override
    public String chat(String systemPrompt, List<ChatMessage> history, String userMsg) {
        if (LOGISTICS.matcher(userMsg).matches()) {
            return "【物流说明】\n当天15:00前支付的订单当日发货，之后次日发货；\n普通快递约2~4天送达，偏远地区3~5天。\n您可以在「我的订单」中随时查看物流状态。";
        }
        if (REFUND.matcher(userMsg).matches()) {
            return "【退换货政策】\n签收后7天内支持无理由退货（需保持商品完好）；\n质量问题退换运费由商家承担。\n请在「我的订单」中选择对应订单发起售后。";
        }
        if (PAY.matcher(userMsg).matches()) {
            return "【支付说明】\n本商城支持模拟支付（演示环境），下单后进入支付页点击\"模拟支付成功\"即可完成付款。\n生产环境可接入微信/支付宝支付网关。";
        }
        if (COUPON.matcher(userMsg).matches()) {
            return "【优惠活动】\n全场满300减30，新用户注册即享专属礼包；\n每周五「AI猜你喜欢」推荐的商品还有额外折扣哦~";
        }
        if (STOCK.matcher(userMsg).matches()) {
            return "商品库存实时更新，下单页会提示是否充足；如遇缺货一般3~7天内补货，可以先加购并开启关注。";
        }
        if (ACCOUNT.matcher(userMsg).matches()) {
            return "【账号相关】\n注册请使用3~20位用户名；登录后可在「我的」中修改昵称、手机号与密码。遇到无法登录可先尝试清除浏览器缓存。";
        }
        if (RECOMMEND.matcher(userMsg).matches()) {
            return "可以在首页查看「AI猜你喜欢」，系统会根据您的浏览、加购与购买行为进行个性化推荐；新用户可以先看看热卖榜单哦~";
        }
        if (userMsg.contains("你好") || userMsg.contains("在吗") || userMsg.contains("hi") || userMsg.contains("hello")) {
            return "您好，我是智选商城AI客服助手 🤖\n可以帮您解答：商品咨询、物流查询、退换货政策、支付问题等。\n请问有什么可以帮您？";
        }
        // 未命中任何规则，返回 null 交给通用兜底
        return null;
    }

    @Override
    public boolean available() {
        return true;
    }

    @Override
    public String name() {
        return "rule";
    }
}
