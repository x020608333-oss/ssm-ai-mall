package com.shop.dao;

import com.shop.entity.Cart;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CartMapper {

    /** 同一用户同一商品只保留一条记录，重复添加则累加数量 */
    int insertOrUpdate(@Param("userId") Long userId,
                       @Param("productId") Long productId,
                       @Param("quantity") int quantity);

    List<Cart> findByUser(@Param("userId") Long userId);

    Cart findByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);

    Cart findByUserAndProduct(@Param("userId") Long userId, @Param("productId") Long productId);

    int updateQuantity(@Param("id") Long id, @Param("userId") Long userId, @Param("quantity") int quantity);

    int updateChecked(@Param("id") Long id, @Param("userId") Long userId, @Param("checked") int checked);

    int checkAll(@Param("userId") Long userId, @Param("checked") int checked);

    int deleteByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);

    int deleteCheckedByUser(@Param("userId") Long userId);

    int countByUser(@Param("userId") Long userId);
}
