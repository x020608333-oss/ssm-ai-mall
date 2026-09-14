package com.shop.dao;

import com.shop.entity.Order;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderMapper {

    int insert(Order order);

    int updateStatus(@Param("orderNo") String orderNo, @Param("from") int from, @Param("to") int to);

    /** 支付成功：置为已支付并记录支付时间 */
    int paySuccess(@Param("orderNo") String orderNo);

    Order findById(@Param("id") Long id);

    Order findByOrderNo(@Param("orderNo") String orderNo);

    Order findByOrderNoAndUser(@Param("orderNo") String orderNo, @Param("userId") Long userId);

    /** 用户订单列表，配合 PageHelper 分页 */
    List<Order> findByUser(@Param("userId") Long userId, @Param("status") Integer status);

    /** 管理端订单列表（联查用户名） */
    List<Order> findList(@Param("status") Integer status, @Param("orderNo") String orderNo);

    int countByStatus(@Param("status") Integer status);
}
