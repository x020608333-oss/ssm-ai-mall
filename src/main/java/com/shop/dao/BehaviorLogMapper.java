package com.shop.dao;

import com.shop.entity.BehaviorLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BehaviorLogMapper {

    int insert(BehaviorLog log);

    /** 某用户最近的行为记录（推荐算法取用户画像） */
    List<BehaviorLog> findRecentByUser(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 与指定商品产生过共同行为的其他用户商品ID集合（ItemCF 候选集）
     * 返回 productId -> 协同分
     */
    List<BehaviorLog> findCoOccurrence(@Param("productIds") List<Long> productIds,
                                       @Param("excludeUserId") Long excludeUserId,
                                       @Param("limit") int limit);

    /** 近N天最热门商品（兜底推荐） */
    List<BehaviorLog> findTopProducts(@Param("days") int days, @Param("limit") int limit);
}
