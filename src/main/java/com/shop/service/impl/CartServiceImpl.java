package com.shop.service.impl;

import com.shop.common.BizException;
import com.shop.dao.BehaviorLogMapper;
import com.shop.dao.CartMapper;
import com.shop.dao.ProductMapper;
import com.shop.entity.BehaviorLog;
import com.shop.entity.Cart;
import com.shop.entity.Product;
import com.shop.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private BehaviorLogMapper behaviorLogMapper;

    @Override
    public void add(Long userId, Long productId, int quantity) {
        Product product = productMapper.findById(productId);
        if (product == null || product.getStatus() != 1) {
            throw new BizException("商品已下架");
        }
        if (quantity <= 0) {
            quantity = 1;
        }
        if (quantity > 99) {
            throw new BizException("单件商品最多购买99件");
        }
        cartMapper.insertOrUpdate(userId, productId, quantity);
        BehaviorLog log = new BehaviorLog();
        log.setUserId(userId);
        log.setProductId(productId);
        log.setType(BehaviorLog.TYPE_CART);
        log.setScore(3);
        behaviorLogMapper.insert(log);
    }

    @Override
    public List<Cart> list(Long userId) {
        return cartMapper.findByUser(userId);
    }

    @Override
    public void updateQuantity(Long userId, Long cartId, int quantity) {
        Cart cart = requireOwn(userId, cartId);
        if (quantity <= 0 || quantity > 99) {
            throw new BizException("购买数量需在1~99之间");
        }
        if (cart.getStock() != null && quantity > cart.getStock()) {
            throw new BizException("库存不足，当前库存 " + cart.getStock() + " 件");
        }
        cartMapper.updateQuantity(cartId, userId, quantity);
    }

    @Override
    public void updateChecked(Long userId, Long cartId, boolean checked) {
        requireOwn(userId, cartId);
        cartMapper.updateChecked(cartId, userId, checked ? 1 : 0);
    }

    @Override
    public void remove(Long userId, Long cartId) {
        requireOwn(userId, cartId);
        cartMapper.deleteByIdAndUser(cartId, userId);
    }

    @Override
    public int count(Long userId) {
        return cartMapper.countByUser(userId);
    }

    private Cart requireOwn(Long userId, Long cartId) {
        Cart cart = cartMapper.findByIdAndUser(cartId, userId);
        if (cart == null) {
            throw new BizException("购物车记录不存在");
        }
        return cart;
    }
}
