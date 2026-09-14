package com.shop.service.impl;

import com.shop.common.BizException;
import com.shop.dao.*;
import com.shop.entity.*;
import com.shop.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private AddressMapper addressMapper;
    @Autowired
    private BehaviorLogMapper behaviorLogMapper;

    private static final Random RANDOM = new Random();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createFromCart(Long userId, Long addressId, String remark) {
        Address address = addressMapper.findByIdAndUser(addressId, userId);
        if (address == null) {
            throw new BizException("收货地址不存在");
        }
        // 只取勾选的购物车项（连商品表取最新价格与库存）
        List<Cart> carts = cartMapper.findByUser(userId);
        List<Cart> checkedItems = new ArrayList<>();
        for (Cart cart : carts) {
            if (cart.getChecked() == 1) {
                checkedItems.add(cart);
            }
        }
        if (checkedItems.isEmpty()) {
            throw new BizException("请先在购物车勾选要结算的商品");
        }

        Order order = buildOrder(userId, address, remark);
        List<OrderItem> items = new ArrayList<>();
        for (Cart cart : checkedItems) {
            items.add(buildItem(order, cart.getProductId(), cart.getProductName(),
                    cart.getMainImage(), cart.getPrice(), cart.getQuantity()));
        }
        Order saved = persistOrder(order, items);
        // 下单成功后移除购物车中已结算的勾选项
        cartMapper.deleteCheckedByUser(userId);
        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createBuyNow(Long userId, Long productId, int quantity, Long addressId, String remark) {
        Address address = addressMapper.findByIdAndUser(addressId, userId);
        if (address == null) {
            throw new BizException("收货地址不存在");
        }
        if (quantity <= 0 || quantity > 99) {
            throw new BizException("购买数量需在1~99之间");
        }
        Product product = productMapper.findById(productId);
        if (product == null || product.getStatus() != 1) {
            throw new BizException("商品已下架");
        }

        Order order = buildOrder(userId, address, remark);
        List<OrderItem> items = new ArrayList<>();
        items.add(buildItem(order, product.getId(), product.getName(),
                product.getMainImage(), product.getPrice(), quantity));
        return persistOrder(order, items);
    }

    /**
     * 订单落库核心流程（购物车下单 / 立即购买共用）
     * 任意一件商品库存不足都会抛出异常，整个事务回滚——这就是 @Transactional 的价值
     */
    private Order persistOrder(Order order, List<OrderItem> items) {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : items) {
            total = total.add(item.getSubtotal());
        }
        order.setTotalAmount(total);
        orderMapper.insert(order);

        for (OrderItem item : items) {
            item.setOrderId(order.getId());
        }
        orderItemMapper.insertBatch(items);

        // 条件更新扣库存：UPDATE ... WHERE stock >= quantity
        // 影响行数为0 => 库存不足或已下架 => 抛异常 => 整个事务回滚，防止超卖
        for (OrderItem item : items) {
            int rows = productMapper.reduceStock(item.getProductId(), item.getQuantity());
            if (rows == 0) {
                throw new BizException("商品【" + item.getProductName() + "】库存不足，下单失败");
            }
            // 行为日志：购买
            BehaviorLog blog = new BehaviorLog();
            blog.setUserId(order.getUserId());
            blog.setProductId(item.getProductId());
            blog.setType(BehaviorLog.TYPE_BUY);
            blog.setScore(5);
            behaviorLogMapper.insert(blog);
        }

        log.info("订单创建成功 orderNo={} userId={} amount={} items={}",
                order.getOrderNo(), order.getUserId(), order.getTotalAmount(), items.size());
        return order;
    }

    private Order buildOrder(Long userId, Address address, String remark) {
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setStatus(Order.STATUS_UNPAID);
        order.setReceiver(address.getReceiver());
        order.setPhone(address.getPhone());
        order.setAddressSnapshot(address.getProvince() + address.getCity() + address.getDistrict() + address.getDetail());
        order.setRemark(remark == null ? "" : remark);
        return order;
    }

    private OrderItem buildItem(Order order, Long productId, String name, String image,
                                BigDecimal price, int quantity) {
        OrderItem item = new OrderItem();
        item.setOrderNo(order.getOrderNo());
        item.setProductId(productId);
        // 商品信息快照，商品后续改名/调价不影响历史订单
        item.setProductName(name);
        item.setProductImage(image);
        item.setPrice(price);
        item.setQuantity(quantity);
        item.setSubtotal(price.multiply(BigDecimal.valueOf(quantity)));
        return item;
    }

    /** 订单号：年月日时分秒 + 5位随机数，实际生产建议用分布式ID（雪花算法） */
    private String generateOrderNo() {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                + String.format("%05d", RANDOM.nextInt(100000));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void paySuccess(String orderNo) {
        // 状态条件更新：只有"待支付"才能变成"已支付"，幂等，重复回调不会重复扣
        int rows = orderMapper.paySuccess(orderNo);
        if (rows == 0) {
            throw new BizException("订单状态异常，支付失败或已支付过");
        }
        Order order = orderMapper.findByOrderNo(orderNo);
        for (OrderItem item : orderItemMapper.findByOrderId(order.getId())) {
            productMapper.increaseSales(item.getProductId(), item.getQuantity());
        }
        log.info("订单支付成功 orderNo={}", orderNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long userId, String orderNo) {
        Order order = orderMapper.findByOrderNoAndUser(orderNo, userId);
        if (order == null) {
            throw new BizException("订单不存在");
        }
        int rows = orderMapper.updateStatus(orderNo, Order.STATUS_UNPAID, Order.STATUS_CANCELED);
        if (rows == 0) {
            throw new BizException("当前订单状态不可取消");
        }
        // 归还库存
        for (OrderItem item : orderItemMapper.findByOrderId(order.getId())) {
            productMapper.addStock(item.getProductId(), item.getQuantity());
        }
        log.info("订单已取消并归还库存 orderNo={}", orderNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ship(String orderNo) {
        int rows = orderMapper.updateStatus(orderNo, Order.STATUS_PAID, Order.STATUS_SHIPPED);
        if (rows == 0) {
            throw new BizException("只有已支付订单才能发货");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirm(Long userId, String orderNo) {
        Order order = orderMapper.findByOrderNoAndUser(orderNo, userId);
        if (order == null) {
            throw new BizException("订单不存在");
        }
        int rows = orderMapper.updateStatus(orderNo, Order.STATUS_SHIPPED, Order.STATUS_FINISHED);
        if (rows == 0) {
            throw new BizException("当前订单状态不可确认收货");
        }
    }
}
