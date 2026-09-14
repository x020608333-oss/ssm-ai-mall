package com.shop.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.shop.common.BizException;
import com.shop.common.PageResult;
import com.shop.common.UploadUtil;
import com.shop.dao.ProductMapper;
import com.shop.entity.Product;
import com.shop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Value("${upload.dir}")
    private String uploadDir;

    @Override
    public PageResult<Product> page(String keyword, Long categoryId, Integer status, Integer hot,
                                    String sort, int page, int size, boolean onlyOnSale) {
        // sort 只允许白名单值：default/priceAsc/priceDesc/salesDesc，防止SQL注入
        String orderBy = "priceAsc".equals(sort) || "priceDesc".equals(sort) || "salesDesc".equals(sort) ? sort : null;
        PageHelper.startPage(page, size);
        List<Product> list = productMapper.findList(
                StringUtils.hasText(keyword) ? keyword : null,
                categoryId, status, hot, orderBy);
        PageInfo<Product> info = new PageInfo<>(list);
        return PageResult.of(info.getTotal(), info.getPageNum(), info.getPageSize(), list);
    }

    @Override
    public Product detail(Long id) {
        Product p = productMapper.findById(id);
        if (p == null) {
            throw new BizException("商品不存在");
        }
        return p;
    }

    @Override
    public List<Product> hotList(int limit) {
        return productMapper.findHot(limit);
    }

    @Override
    public List<Product> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return productMapper.findByIds(ids);
    }

    @Override
    public void add(Product product) {
        validate(product);
        if (product.getStatus() == null) {
            product.setStatus(1);
        }
        if (product.getSales() == null) {
            product.setSales(0);
        }
        productMapper.insert(product);
    }

    @Override
    public void update(Product product) {
        if (product.getId() == null) {
            throw new BizException("缺少商品ID");
        }
        validate(product);
        productMapper.update(product);
    }

    @Override
    public void delete(Long id) {
        productMapper.deleteById(id);
    }

    @Override
    public String uploadImage(MultipartFile file, HttpServletRequest request) {
        return UploadUtil.saveImage(file, request, uploadDir);
    }

    private void validate(Product product) {
        if (!StringUtils.hasText(product.getName())) {
            throw new BizException("商品名称不能为空");
        }
        if (product.getCategoryId() == null) {
            throw new BizException("请选择商品分类");
        }
        if (product.getPrice() == null || product.getPrice().doubleValue() < 0) {
            throw new BizException("请填写正确的价格");
        }
        if (product.getStock() == null || product.getStock() < 0) {
            throw new BizException("请填写正确的库存");
        }
    }
}
