package com.shop.web.admin;

import com.shop.common.Result;
import com.shop.entity.Category;
import com.shop.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理后台：分类管理
 */
@Controller
@RequestMapping("/admin")
public class AdminCategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/categories")
    public String page(Model model) {
        model.addAttribute("categories", categoryService.listAll());
        return "admin/categories";
    }

    @PostMapping("/api/categories/add")
    @ResponseBody
    public Result<Void> add(@RequestBody Category category) {
        categoryService.add(category);
        return Result.ok();
    }

    @PostMapping("/api/categories/update")
    @ResponseBody
    public Result<Void> update(@RequestBody Category category) {
        categoryService.update(category);
        return Result.ok();
    }

    @PostMapping("/api/categories/delete")
    @ResponseBody
    public Result<Void> delete(@RequestBody Map<String, Long> body) {
        categoryService.delete(body.get("id"));
        return Result.ok();
    }
}
