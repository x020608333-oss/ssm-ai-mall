package com.shop.service.impl;

import com.shop.dao.BehaviorLogMapper;
import com.shop.dao.ProductMapper;
import com.shop.entity.BehaviorLog;
import com.shop.entity.Product;
import com.shop.service.BehaviorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BehaviorServiceImpl implements BehaviorService {

    @Autowired
    private BehaviorLogMapper behaviorLogMapper;
    @Autowired
    private ProductMapper productMapper;

    @Override
    public void log(Long userId, Long productId, int type) {
        BehaviorLog log = new BehaviorLog();
        log.setUserId(userId);
        log.setProductId(productId);
        log.setType(type);
        log.setScore(type == BehaviorLog.TYPE_BUY ? 5 : type == BehaviorLog.TYPE_CART ? 3 : 1);
        behaviorLogMapper.insert(log);
    }

    @Override
    public List<Product> topHot(int days, int limit) {
        List<BehaviorLog> tops = behaviorLogMapper.findTopProducts(days, limit);
        List<Long> ids = new ArrayList<>();
        for (BehaviorLog t : tops) {
            ids.add(t.getProductId());
        }
        List<Product> products = productMapper.findByIds(ids);
        // 按热度分排序返回，并过滤下架商品
        List<Product> result = new ArrayList<>();
        for (BehaviorLog t : tops) {
            for (Product p : products) {
                if (p.getId().equals(t.getProductId()) && p.getStatus() == 1) {
                    result.add(p);
                }
            }
        }
        return result;
    }
}
