package com.shop.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItem {
    private Long id;
    private Long orderId;
    private String orderNo;
    private Long productId;
    /** 商品信息快照：商品改名/涨价不影响历史订单 */
    private String productName;
    private String productImage;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
}
