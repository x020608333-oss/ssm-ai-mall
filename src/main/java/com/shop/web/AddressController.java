package com.shop.web;

import com.shop.common.Result;
import com.shop.common.WebUtil;
import com.shop.dao.AddressMapper;
import com.shop.entity.Address;
import com.shop.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 收货地址管理
 */
@RestController
@RequestMapping("/api/address")
public class AddressController {

    @Autowired
    private AddressMapper addressMapper;

    @GetMapping("/list")
    public Result<List<Address>> list(HttpServletRequest request) {
        User user = WebUtil.getLoginUser(request);
        return Result.ok(addressMapper.findByUser(user.getId()));
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody Address address, HttpServletRequest request) {
        User user = WebUtil.getLoginUser(request);
        address.setUserId(user.getId());
        if (address.getReceiver() == null || address.getPhone() == null || address.getDetail() == null) {
            return Result.error(400, "收货人、电话、详细地址不能为空");
        }
        if (addressMapper.countByUser(user.getId()) >= 10) {
            return Result.error(400, "最多保存10个收货地址");
        }
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            addressMapper.clearDefault(user.getId());
        } else if (addressMapper.countByUser(user.getId()) == 0) {
            address.setIsDefault(1);
        } else {
            address.setIsDefault(0);
        }
        addressMapper.insert(address);
        return Result.ok();
    }

    @PostMapping("/delete")
    public Result<Void> delete(@RequestBody Address body, HttpServletRequest request) {
        User user = WebUtil.getLoginUser(request);
        addressMapper.deleteByIdAndUser(body.getId(), user.getId());
        return Result.ok();
    }

    @PostMapping("/default")
    public Result<Void> setDefault(@RequestBody Address body, HttpServletRequest request) {
        User user = WebUtil.getLoginUser(request);
        addressMapper.clearDefault(user.getId());
        addressMapper.setDefault(body.getId(), user.getId());
        return Result.ok();
    }
}
