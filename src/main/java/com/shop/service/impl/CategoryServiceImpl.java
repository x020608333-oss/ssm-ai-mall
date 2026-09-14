package com.shop.service.impl;

import com.shop.common.BizException;
import com.shop.dao.CategoryMapper;
import com.shop.entity.Category;
import com.shop.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<Category> listAll() {
        return categoryMapper.findAll();
    }

    @Override
    public void add(Category category) {
        if (!StringUtils.hasText(category.getName())) {
            throw new BizException("分类名称不能为空");
        }
        if (category.getSort() == null) {
            category.setSort(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        categoryMapper.insert(category);
    }

    @Override
    public void update(Category category) {
        if (category.getId() == null) {
            throw new BizException("缺少分类ID");
        }
        categoryMapper.update(category);
    }

    @Override
    public void delete(Long id) {
        int cnt = categoryMapper.countProduct(id);
        if (cnt > 0) {
            throw new BizException("该分类下还有 " + cnt + " 件商品，无法删除");
        }
        categoryMapper.deleteById(id);
    }
}
