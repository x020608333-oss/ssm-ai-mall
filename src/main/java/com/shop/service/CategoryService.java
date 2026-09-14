package com.shop.service;

import com.shop.entity.Category;

import java.util.List;

public interface CategoryService {

    List<Category> listAll();

    void add(Category category);

    void update(Category category);

    void delete(Long id);
}
