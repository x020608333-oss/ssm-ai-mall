package com.shop.entity;

import lombok.Data;

import java.util.Date;

@Data
public class BehaviorLog {
    public static final int TYPE_VIEW = 1;
    public static final int TYPE_FAVORITE = 2;
    public static final int TYPE_CART = 3;
    public static final int TYPE_BUY = 4;

    private Long id;
    private Long userId;
    private Long productId;
    /** 1浏览 2收藏 3加购 4购买 */
    private Integer type;
    private Integer score;
    private Date createdAt;

    /** 聚合查询附加字段（协同过滤/热门榜） */
    private Integer aggScore;
    private Integer aggUsers;
}
