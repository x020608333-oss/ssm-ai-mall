package com.shop.web.admin;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.shop.common.PageResult;
import com.shop.common.Result;
import com.shop.common.ScriptJson;
import com.shop.common.WebUtil;
import com.shop.dao.OrderItemMapper;
import com.shop.dao.OrderMapper;
import com.shop.entity.Order;
import com.shop.entity.OrderItem;
import com.shop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理后台：订单管理（列表/搜索/发货）
 */
@Controller
@RequestMapping("/admin")
public class AdminOrderController {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private OrderService orderService;

    @GetMapping("/orders")
    public String page(Integer status, String orderNo, Integer page, Model model) {
        PageHelper.startPage(page == null ? 1 : page, 10);
        List<Order> orders = orderMapper.findList(status, orderNo);
        PageInfo<Order> info = new PageInfo<>(orders);
        // 批量填充明细
        if (!orders.isEmpty()) {
            List<Long> ids = new ArrayList<>();
            for (Order o : orders) {
                ids.add(o.getId());
            }
            Map<Long, List<OrderItem>> grouped = new HashMap<>();
            for (OrderItem item : orderItemMapper.findByOrderIds(ids)) {
                grouped.computeIfAbsent(item.getOrderId(), k -> new ArrayList<>()).add(item);
            }
            for (Order o : orders) {
                o.setItems(grouped.getOrDefault(o.getId(), new ArrayList<>()));
            }
        }
        model.addAttribute("pageResult", PageResult.of(info.getTotal(), info.getPageNum(), info.getPageSize(), orders));
        // 订单详情用 JSON 传给前端，避免 JSP 把用户可控字段拼进 JS 字符串（存储型 XSS）
        Map<String, Object> detailMap = new LinkedHashMap<>();
        for (Order o : orders) {
            Map<String, Object> d = new LinkedHashMap<>();
            d.put("orderNo", o.getOrderNo());
            d.put("username", o.getUsername());
            d.put("totalAmount", o.getTotalAmount());
            d.put("status", o.getStatus());
            d.put("receiver", o.getReceiver());
            d.put("phone", o.getPhone());
            d.put("address", o.getAddressSnapshot());
            d.put("createdAt", o.getCreatedAt() == null ? "" : o.getCreatedAt().toString());
            d.put("payTime", o.getPayTime() == null ? "" : o.getPayTime().toString());
            List<Map<String, Object>> items = new ArrayList<>();
            for (OrderItem item : o.getItems()) {
                Map<String, Object> im = new LinkedHashMap<>();
                im.put("name", item.getProductName());
                im.put("price", item.getPrice());
                im.put("qty", item.getQuantity());
                im.put("subtotal", item.getSubtotal());
                items.add(im);
            }
            d.put("items", items);
            detailMap.put(o.getOrderNo(), d);
        }
        model.addAttribute("ordersJson", ScriptJson.of(detailMap));
        model.addAttribute("status", status);
        model.addAttribute("orderNo", orderNo == null ? "" : orderNo);
        return "admin/orders";
    }

    @PostMapping("/api/orders/ship")
    @ResponseBody
    public Result<Void> ship(@RequestBody Map<String, String> body) {
        orderService.ship(body.get("orderNo"));
        return Result.ok();
    }
}
