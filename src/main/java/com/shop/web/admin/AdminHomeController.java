package com.shop.web.admin;

import com.shop.common.Result;
import com.shop.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * 管理后台：仪表盘（今日订单/销售额 + 近7天趋势 + 分类占比）
 */
@Controller
@RequestMapping("/admin")
public class AdminHomeController {

    @Autowired
    private StatsService statsService;

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAllAttributes(statsService.dashboard());
        model.addAttribute("dailyStats", statsService.daily(7));
        return "admin/dashboard";
    }

    @GetMapping("/api/chart")
    @ResponseBody
    public Result<List<Map<String, Object>>> chart() {
        return Result.ok(statsService.daily(7));
    }
}
