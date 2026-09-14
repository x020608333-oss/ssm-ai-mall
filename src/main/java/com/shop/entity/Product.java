package com.shop.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class Product {
    private Long id;
    private Long categoryId;
    /** 关联分类名（连表查询用） */
    private String categoryName;
    private String name;
    private String subtitle;
    private String mainImage;
    private BigDecimal price;
    private Integer stock;
    private Integer sales;
    private String detail;
    private Integer isHot;
    /** 1上架 0下架 */
    private Integer status;
    private Date createdAt;
    private Date updatedAt;
}
