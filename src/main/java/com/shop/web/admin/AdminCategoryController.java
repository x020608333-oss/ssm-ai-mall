package com.shop.web.admin;

import com.shop.common.Result;
import com.shop.common.ScriptJson;
import com.shop.entity.Category;
import com.shop.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        List<Category> categories = categoryService.listAll();
        model.addAttribute("categories", categories);
        // 编辑回填数据走 JSON，避免 JSP 把分类名/图标拼进 JS 字符串（存储型 XSS）
        Map<String, Object> editMap = new LinkedHashMap<>();
        for (Category c : categories) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", c.getId());
            m.put("name", Objects.toString(c.getName(), ""));
            m.put("icon", Objects.toString(c.getIcon(), ""));
            m.put("sort", c.getSort());
            m.put("status", c.getStatus());
            editMap.put(String.valueOf(c.getId()), m);
        }
        model.addAttribute("categoriesJson", ScriptJson.of(editMap));
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
