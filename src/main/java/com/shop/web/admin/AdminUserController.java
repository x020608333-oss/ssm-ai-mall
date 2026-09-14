package com.shop.web.admin;

import com.shop.common.PageResult;
import com.shop.common.Result;
import com.shop.entity.User;
import com.shop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理后台：用户管理（禁用/启用、角色调整）
 */
@Controller
@RequestMapping("/admin")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public String page(String keyword, Integer page, Model model) {
        PageResult<User> result = userService.pageForAdmin(keyword, page == null ? 1 : page, 10);
        model.addAttribute("result", result);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        return "admin/users";
    }

    @PostMapping("/api/users/status")
    @ResponseBody
    public Result<Void> status(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(String.valueOf(body.get("id")));
        int status = Integer.parseInt(String.valueOf(body.get("status")));
        userService.updateStatus(id, status);
        return Result.ok();
    }

    @PostMapping("/api/users/role")
    @ResponseBody
    public Result<Void> role(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(String.valueOf(body.get("id")));
        String role = String.valueOf(body.get("role"));
        userService.updateRole(id, role);
        return Result.ok();
    }
}
