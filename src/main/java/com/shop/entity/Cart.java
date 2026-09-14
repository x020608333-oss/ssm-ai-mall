package com.shop.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class Cart {
    private Long id;
    private Long userId;
    private Long productId;
    private Integer quantity;
    private Integer checked;
    private Date createdAt;
    private Date updatedAt;

    /** 以下字段来自与商品表连表查询 */
    private String productName;
    private BigDecimal price;
    private Integer stock;
    private String mainImage;
    private Integer productStatus;
}
