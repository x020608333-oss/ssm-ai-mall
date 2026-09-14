package com.shop.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class Order {
    public static final int STATUS_UNPAID = 0;
    public static final int STATUS_PAID = 1;
    public static final int STATUS_SHIPPED = 2;
    public static final int STATUS_FINISHED = 3;
    public static final int STATUS_CANCELED = 5;
    public static final int STATUS_CLOSED = 6;

    private Long id;
    private String orderNo;
    private Long userId;
    /** 下单人昵称（管理端连表查询用） */
    private String username;
    private BigDecimal totalAmount;
    private Integer status;
    private String receiver;
    private String phone;
    private String addressSnapshot;
    private String remark;
    private Date payTime;
    private Date createdAt;
    private Date updatedAt;

    /** 明细（连表查询填充） */
    private List<OrderItem> items;
}
