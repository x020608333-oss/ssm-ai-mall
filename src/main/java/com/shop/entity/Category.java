package com.shop.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Category {
    private Long id;
    private String name;
    private String icon;
    private Integer sort;
    private Integer status;
    private Date createdAt;
}
