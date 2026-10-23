package com.shop.service;

import com.shop.entity.Order;

public interface OrderService {

    /**
     * 从购物车勾选的商品创建订单（声明式事务）：
     * 校验地址 -> 锁定校验库存 -> 生成订单+明细 -> 条件更新扣减库存（防超卖）-> 清空已勾选购物车
     */
    Order createFromCart(Long userId, Long addressId, String remark);

    /** 立即购买（跳过购物车直接下单） */
    Order createBuyNow(Long userId, Long productId, int quantity, Long addressId, String remark);

    /** 支付成功回调（模拟第三方支付网关通知） */
    void paySuccess(String orderNo);

    /** 模拟支付回调：校验订单归属（前端登录用户）后再置为已支付。 */
    boolean paySuccessForUser(String orderNo, Long userId);

    /** 用户取消待支付订单：归还库存 */
    void cancel(Long userId, String orderNo);

    /** 管理员发货 */
    void ship(String orderNo);

    /** 用户确认收货 */
    void confirm(Long userId, String orderNo);
}
