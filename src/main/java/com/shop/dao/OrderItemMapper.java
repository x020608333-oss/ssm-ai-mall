package com.shop.dao;

import com.shop.entity.OrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderItemMapper {

    int insertBatch(@Param("items") List<OrderItem> items);

    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);

    /** 批量查询多个订单的明细（订单列表页填充） */
    List<OrderItem> findByOrderIds(@Param("orderIds") List<Long> orderIds);
}
