package com.shop.web.admin;

import com.shop.common.PageResult;
import com.shop.common.Result;
import com.shop.entity.Product;
import com.shop.service.CategoryService;
import com.shop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 管理后台：商品管理（CRUD + 图片上传 + 分页搜索）
 */
@Controller
@RequestMapping("/admin")
public class AdminProductController {

    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/products")
    public String page(String keyword, Long categoryId, Integer page, Model model) {
        PageResult<Product> result = productService.page(keyword, categoryId, null, null,
                null, page == null ? 1 : page, 10, false);
        model.addAttribute("result", result);
        model.addAttribute("categories", categoryService.listAll());
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("categoryId", categoryId);
        return "admin/products";
    }

    @PostMapping("/api/products/add")
    @ResponseBody
    public Result<Void> add(@RequestBody Product product) {
        productService.add(product);
        return Result.ok();
    }

    @PostMapping("/api/products/update")
    @ResponseBody
    public Result<Void> update(@RequestBody Product product) {
        productService.update(product);
        return Result.ok();
    }

    @PostMapping("/api/products/delete")
    @ResponseBody
    public Result<Void> delete(@RequestBody Map<String, Long> body) {
        productService.delete(body.get("id"));
        return Result.ok();
    }

    /** 商品图上传：返回可访问URL */
    @PostMapping("/api/products/upload")
    @ResponseBody
    public Result<String> upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        return Result.ok(productService.uploadImage(file, request));
    }
}
