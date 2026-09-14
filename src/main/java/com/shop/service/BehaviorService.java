package com.shop.service;

import com.shop.entity.BehaviorLog;
import com.shop.entity.Product;

import java.util.List;

public interface BehaviorService {

    /** 记录用户行为（浏览/收藏/加购/购买），供推荐引擎使用 */
    void log(Long userId, Long productId, int type);

    List<Product> topHot(int days, int limit);
}
