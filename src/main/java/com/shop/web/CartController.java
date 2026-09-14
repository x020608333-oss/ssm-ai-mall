package com.shop.web;

import com.shop.common.Result;
import com.shop.common.WebUtil;
import com.shop.entity.User;
import com.shop.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 购物车：页面 + API
 */
@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/cart")
    public String page(Model model, HttpServletRequest request) {
        User user = WebUtil.getLoginUser(request);
        model.addAttribute("items", cartService.list(user.getId()));
        return "front/cart";
    }

    @PostMapping("/api/cart/add")
    @ResponseBody
    public Result<Void> add(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = WebUtil.getLoginUser(request);
        Long productId = Long.valueOf(String.valueOf(body.get("productId")));
        int quantity = body.get("quantity") == null ? 1 : Integer.parseInt(String.valueOf(body.get("quantity")));
        cartService.add(user.getId(), productId, quantity);
        return Result.ok();
    }

    @PostMapping("/api/cart/quantity")
    @ResponseBody
    public Result<Void> quantity(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = WebUtil.getLoginUser(request);
        Long id = Long.valueOf(String.valueOf(body.get("id")));
        int quantity = Integer.parseInt(String.valueOf(body.get("quantity")));
        cartService.updateQuantity(user.getId(), id, quantity);
        return Result.ok();
    }

    @PostMapping("/api/cart/checked")
    @ResponseBody
    public Result<Void> checked(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = WebUtil.getLoginUser(request);
        Long id = Long.valueOf(String.valueOf(body.get("id")));
        boolean checked = Boolean.parseBoolean(String.valueOf(body.get("checked")));
        cartService.updateChecked(user.getId(), id, checked);
        return Result.ok();
    }

    @PostMapping("/api/cart/remove")
    @ResponseBody
    public Result<Void> remove(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = WebUtil.getLoginUser(request);
        Long id = Long.valueOf(String.valueOf(body.get("id")));
        cartService.remove(user.getId(), id);
        return Result.ok();
    }

    @GetMapping("/api/cart/count")
    @ResponseBody
    public Result<Integer> count(HttpServletRequest request) {
        User user = WebUtil.getLoginUser(request);
        return Result.ok(cartService.count(user.getId()));
    }
}
