package com.shop.dao;

import com.shop.entity.Category;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CategoryMapper {

    int insert(Category category);

    int update(Category category);

    int deleteById(@Param("id") Long id);

    List<Category> findAll();

    Category findById(@Param("id") Long id);

    int countProduct(@Param("id") Long id);
}
