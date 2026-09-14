package com.shop.dao;

import com.shop.entity.Product;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ProductMapper {

    int insert(Product product);

    int update(Product product);

    int deleteById(@Param("id") Long id);

    Product findById(@Param("id") Long id);

    /** 商品列表（关键词/分类/状态/排序多条件动态查询），配合 PageHelper 分页 */
    List<Product> findList(@Param("keyword") String keyword,
                           @Param("categoryId") Long categoryId,
                           @Param("status") Integer status,
                           @Param("hot") Integer hot,
                           @Param("orderBy") String orderBy);

    List<Product> findHot(@Param("limit") int limit);

    List<Product> findByIds(@Param("ids") List<Long> ids);

    int countAll();

    /**
     * 扣减库存（防超卖核心）：仅当剩余库存足够时才更新，
     * 返回受影响行数=0 表示库存不足或商品下架，由事务回滚整笔订单
     */
    int reduceStock(@Param("id") Long id, @Param("quantity") int quantity);

    int addStock(@Param("id") Long id, @Param("quantity") int quantity);

    int increaseSales(@Param("id") Long id, @Param("quantity") int quantity);

    int decreaseSales(@Param("id") Long id, @Param("quantity") int quantity);

    /** 管理端导出/报表用 */
    List<Product> findLowStock(@Param("threshold") int threshold);

    List<Product> searchByName(@Param("keyword") String keyword, @Param("limit") int limit);

    /** AI客服查询用 */
    List<Product> findCheaper(@Param("price") BigDecimal price, @Param("keyword") String keyword, @Param("limit") int limit);
}
