package com.shop.web;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.shop.common.PageResult;
import com.shop.common.Result;
import com.shop.common.WebUtil;
import com.shop.dao.AddressMapper;
import com.shop.dao.OrderItemMapper;
import com.shop.dao.OrderMapper;
import com.shop.entity.*;
import com.shop.service.CartService;
import com.shop.service.OrderService;
import com.shop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

/**
 * 结算 / 模拟支付 / 订单列表与详情
 */
@Controller
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private CartService cartService;
    @Autowired
    private AddressMapper addressMapper;
    @Autowired
    private ProductService productService;

    /** 结算页：购物车勾选项 或 立即购买单商品 */
    @GetMapping("/checkout")
    public String checkout(HttpServletRequest request, Model model,
                           @RequestParam(required = false) Long buyProductId,
                           @RequestParam(required = false) Integer buyQuantity) {
        User user = WebUtil.getLoginUser(request);
        List<Cart> items = new ArrayList<>();
        if (buyProductId != null) {
            Product p = productService.detail(buyProductId);
            Cart temp = new Cart();
            temp.setProductId(p.getId());
            temp.setProductName(p.getName());
            temp.setPrice(p.getPrice());
            temp.setMainImage(p.getMainImage());
            temp.setStock(p.getStock());
            temp.setQuantity(buyQuantity == null || buyQuantity < 1 ? 1 : Math.min(buyQuantity, 99));
            items.add(temp);
            model.addAttribute("buyNow", true);
        } else {
            for (Cart cart : cartService.list(user.getId())) {
                if (cart.getChecked() == 1) {
                    items.add(cart);
                }
            }
            model.addAttribute("buyNow", false);
        }
        BigDecimal total = BigDecimal.ZERO;
        for (Cart cart : items) {
            total = total.add(cart.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));
        }
        model.addAttribute("items", items);
        model.addAttribute("total", total);
        model.addAttribute("addresses", addressMapper.findByUser(user.getId()));
        return "front/checkout";
    }

    /** 模拟支付页 */
    @GetMapping("/pay")
    public String payPage(@RequestParam String orderNo, HttpServletRequest request, Model model) {
        User user = WebUtil.getLoginUser(request);
        Order order = orderMapper.findByOrderNoAndUser(orderNo, user.getId());
        if (order == null) {
            return "redirect:/orders";
        }
        order.setItems(orderItemMapper.findByOrderId(order.getId()));
        model.addAttribute("order", order);
        return "front/pay";
    }

    /** 我的订单列表（分页，避免JOIN嵌套集合导致的分页错乱：先查订单，再批量查明细） */
    @GetMapping("/orders")
    public String orders(@RequestParam(required = false) Integer status,
                         @RequestParam(defaultValue = "1") Integer page,
                         HttpServletRequest request, Model model) {
        User user = WebUtil.getLoginUser(request);
        PageHelper.startPage(page, 6);
        List<Order> orders = orderMapper.findByUser(user.getId(), status);
        PageInfo<Order> info = new PageInfo<>(orders);
        fillItems(orders);
        model.addAttribute("pageResult", PageResult.of(info.getTotal(), info.getPageNum(), info.getPageSize(), orders));
        model.addAttribute("status", status);
        return "front/orders";
    }

    @GetMapping("/order/{orderNo}")
    public String detail(@PathVariable String orderNo, HttpServletRequest request, Model model) {
        User user = WebUtil.getLoginUser(request);
        Order order = orderMapper.findByOrderNoAndUser(orderNo, user.getId());
        if (order == null) {
            return "redirect:/orders";
        }
        order.setItems(orderItemMapper.findByOrderId(order.getId()));
        model.addAttribute("order", order);
        return "front/order-detail";
    }

    // ================= API =================

    /** 创建订单（购物车勾选项，声明式事务：扣库存失败整体回滚） */
    @PostMapping("/api/order/create")
    @ResponseBody
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = WebUtil.getLoginUser(request);
        Long addressId = Long.valueOf(String.valueOf(body.get("addressId")));
        String remark = (String) body.getOrDefault("remark", "");
        Order order = orderService.createFromCart(user.getId(), addressId, remark);
        return Result.ok(orderData(order));
    }

    /** 立即购买下单 */
    @PostMapping("/api/order/buyNow")
    @ResponseBody
    public Result<Map<String, Object>> buyNow(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = WebUtil.getLoginUser(request);
        Long productId = Long.valueOf(String.valueOf(body.get("productId")));
        int quantity = Integer.parseInt(String.valueOf(body.getOrDefault("quantity", "1")));
        Long addressId = Long.valueOf(String.valueOf(body.get("addressId")));
        String remark = (String) body.getOrDefault("remark", "");
        Order order = orderService.createBuyNow(user.getId(), productId, quantity, addressId, remark);
        return Result.ok(orderData(order));
    }

    /**
     * 模拟第三方支付网关的支付成功回调。
     * 演示环境用登录会话 + 订单归属校验兜底；真实支付网关回调没有用户会话，
     * 上线时必须改为网关验签（金额、商户号、签名）并做幂等处理。
     */
    @PostMapping("/api/pay/notify")
    @ResponseBody
    public Result<Void> payNotify(@RequestBody Map<String, String> body, HttpServletRequest request) {
        User user = WebUtil.getLoginUser(request);
        if (user == null) {
            return Result.error(401, "请先登录");
        }
        String orderNo = body.get("orderNo");
        if (orderNo == null || orderNo.trim().isEmpty()) {
            return Result.error(400, "缺少订单号");
        }
        boolean ok = orderService.paySuccessForUser(orderNo, user.getId());
        return ok ? Result.ok() : Result.error(403, "订单不存在或无权操作");
    }

    @PostMapping("/api/order/cancel")
    @ResponseBody
    public Result<Void> cancel(@RequestBody Map<String, String> body, HttpServletRequest request) {
        User user = WebUtil.getLoginUser(request);
        orderService.cancel(user.getId(), body.get("orderNo"));
        return Result.ok();
    }

    @PostMapping("/api/order/confirm")
    @ResponseBody
    public Result<Void> confirm(@RequestBody Map<String, String> body, HttpServletRequest request) {
        User user = WebUtil.getLoginUser(request);
        orderService.confirm(user.getId(), body.get("orderNo"));
        return Result.ok();
    }

    /** 批量填充订单明细（一次IN查询，避免N+1） */
    private void fillItems(List<Order> orders) {
        if (orders.isEmpty()) {
            return;
        }
        List<Long> ids = new ArrayList<>();
        for (Order o : orders) {
            ids.add(o.getId());
        }
        Map<Long, List<OrderItem>> grouped = new HashMap<>();
        for (OrderItem item : orderItemMapper.findByOrderIds(ids)) {
            grouped.computeIfAbsent(item.getOrderId(), k -> new ArrayList<>()).add(item);
        }
        for (Order o : orders) {
            o.setItems(grouped.getOrDefault(o.getId(), Collections.emptyList()));
        }
    }

    private Map<String, Object> orderData(Order order) {
        Map<String, Object> data = new HashMap<>();
        data.put("orderNo", order.getOrderNo());
        data.put("totalAmount", order.getTotalAmount());
        return data;
    }
}
