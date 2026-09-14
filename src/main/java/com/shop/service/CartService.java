package com.shop.service;

import com.shop.entity.Cart;

import java.util.List;

public interface CartService {

    void add(Long userId, Long productId, int quantity);

    List<Cart> list(Long userId);

    void updateQuantity(Long userId, Long cartId, int quantity);

    void updateChecked(Long userId, Long cartId, boolean checked);

    void remove(Long userId, Long cartId);

    int count(Long userId);
}
