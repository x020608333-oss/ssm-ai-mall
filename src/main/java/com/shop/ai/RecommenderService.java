package com.shop.ai;

import com.shop.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 智能推荐引擎（ItemCF 基于物品的协同过滤）
 *
 * 原理：
 * 1. 从 behavior_log 取出目标用户最近交互过的商品（浏览1分/加购3分/购买5分）
 * 2. 在全量行为表中寻找与这些商品存在"共同用户"的其他商品（共现），累加协同分
 * 3. 协同分降序取 Top N，过滤已看过的和已下架的
 * 4. 冷启动（新用户无行为）或结果不足时，用近7天热门商品兜底
 *
 * 该实现全部基于 MySQL 聚合查询，无外部依赖；数据量大时可平滑升级为
 * Redis 缓存相似度矩阵或离线 Spark 计算。
 */
@Service
public class RecommenderService {

    /** 用户行为权重：购买 > 加购 > 浏览 */
    private static final int RECENT_BEHAVIOR_LIMIT = 60;

    @Autowired
    private com.shop.dao.BehaviorLogMapper behaviorLogMapper;
    @Autowired
    private com.shop.dao.ProductMapper productMapper;
    @Autowired
    private com.shop.service.BehaviorService behaviorService;

    /**
     * 给指定用户推荐商品
     */
    public List<Product> recommendForUser(Long userId, int limit) {
        // 1. 用户画像：最近交互过的商品
        List<com.shop.entity.BehaviorLog> recent =
                behaviorLogMapper.findRecentByUser(userId, RECENT_BEHAVIOR_LIMIT);
        if (recent.isEmpty()) {
            return coldStart(limit);
        }

        // 已交互商品集合（推荐结果中排除）
        Set<Long> interacted = new HashSet<>();
        for (com.shop.entity.BehaviorLog b : recent) {
            interacted.add(b.getProductId());
        }

        // 2. ItemCF：找与用户交互商品共现的其他商品
        List<com.shop.entity.BehaviorLog> candidates =
                behaviorLogMapper.findCoOccurrence(new ArrayList<>(interacted), userId, limit * 4);

        List<Product> result = new ArrayList<>();
        if (!candidates.isEmpty()) {
            List<Long> ids = new ArrayList<>();
            for (com.shop.entity.BehaviorLog c : candidates) {
                ids.add(c.getProductId());
            }
            Map<Long, Product> productMap = new HashMap<>();
            for (Product p : productMapper.findByIds(ids)) {
                productMap.put(p.getId(), p);
            }
            // 3. 按协同分排序，过滤已交互/下架商品
            candidates.sort(Comparator.comparingInt(
                    (com.shop.entity.BehaviorLog c) -> c.getAggScore() == null ? 0 : c.getAggScore()).reversed());
            for (com.shop.entity.BehaviorLog c : candidates) {
                if (result.size() >= limit) {
                    break;
                }
                if (interacted.contains(c.getProductId())) {
                    continue;
                }
                Product p = productMap.get(c.getProductId());
                if (p != null && p.getStatus() == 1) {
                    result.add(p);
                }
            }
        }

        // 4. 结果不足时用热门商品补齐
        if (result.size() < limit) {
            for (Product hot : coldStart(limit * 2)) {
                if (result.size() >= limit) {
                    break;
                }
                if (!interacted.contains(hot.getId()) && !containsId(result, hot.getId())) {
                    result.add(hot);
                }
            }
        }
        return result;
    }

    /**
     * 商品详情页"买了又买"：与该商品有共同用户的其他商品
     */
    public List<Product> relatedProducts(Long productId, int limit) {
        List<com.shop.entity.BehaviorLog> candidates =
                behaviorLogMapper.findCoOccurrence(Collections.singletonList(productId), 0L, limit * 3);
        List<Product> result = new ArrayList<>();
        if (!candidates.isEmpty()) {
            List<Long> ids = new ArrayList<>();
            for (com.shop.entity.BehaviorLog c : candidates) {
                ids.add(c.getProductId());
            }
            Map<Long, Product> productMap = new HashMap<>();
            for (Product p : productMapper.findByIds(ids)) {
                productMap.put(p.getId(), p);
            }
            candidates.sort(Comparator.comparingInt(
                    (com.shop.entity.BehaviorLog c) -> c.getAggScore() == null ? 0 : c.getAggScore()).reversed());
            for (com.shop.entity.BehaviorLog c : candidates) {
                if (result.size() >= limit) {
                    break;
                }
                if (c.getProductId().equals(productId)) {
                    continue;
                }
                Product p = productMap.get(c.getProductId());
                if (p != null && p.getStatus() == 1) {
                    result.add(p);
                }
            }
        }
        return result;
    }

    /** 冷启动：近7天热门行为商品 -> 运营标记热卖商品 */
    private List<Product> coldStart(int limit) {
        List<Product> hot = behaviorService.topHot(7, limit);
        if (hot.size() < limit) {
            for (Product p : productMapper.findHot(limit)) {
                if (hot.size() >= limit) {
                    break;
                }
                if (!containsId(hot, p.getId())) {
                    hot.add(p);
                }
            }
        }
        return hot;
    }

    private boolean containsId(List<Product> list, Long id) {
        for (Product p : list) {
            if (p.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }
}
