package com.shop.service;

import com.shop.common.PageResult;
import com.shop.entity.Product;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface ProductService {

    PageResult<Product> page(String keyword, Long categoryId, Integer status, Integer hot,
                             String sort, int page, int size, boolean onlyOnSale);

    Product detail(Long id);

    List<Product> hotList(int limit);

    List<Product> listByIds(List<Long> ids);

    void add(Product product);

    void update(Product product);

    void delete(Long id);

    /** 商品图上传，返回可访问URL */
    String uploadImage(MultipartFile file, HttpServletRequest request);
}
