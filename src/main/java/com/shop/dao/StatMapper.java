package com.shop.dao;

import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 统计查询（管理端仪表盘）
 */
public interface StatMapper {

    int todayOrderCount();

    BigDecimal todaySales();

    int userCount();

    int unpaidCount();

    /** 近N天每日销售额与订单数：[{date:'09-07', amount: 1234.00, cnt: 12}, ...] */
    List<Map<String, Object>> dailyStats(@Param("days") int days);

    /** 商品分类销售占比：[{name:'手机数码', cnt: 20}, ...] */
    List<Map<String, Object>> categoryStats();
}
