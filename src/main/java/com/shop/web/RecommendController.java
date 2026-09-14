package com.shop.web;

import com.shop.ai.RecommenderService;
import com.shop.common.Result;
import com.shop.common.WebUtil;
import com.shop.entity.Product;
import com.shop.entity.User;
import com.shop.service.BehaviorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 个性化推荐 API：登录用户走 ItemCF，未登录走热门兜底
 */
@RestController
public class RecommendController {

    @Autowired
    private RecommenderService recommenderService;
    @Autowired
    private BehaviorService behaviorService;

    @GetMapping("/api/recommend")
    public Result<List<Product>> recommend(@RequestParam(defaultValue = "8") int limit,
                                           HttpServletRequest request) {
        if (limit < 1 || limit > 20) {
            limit = 8;
        }
        User user = WebUtil.getLoginUser(request);
        if (user != null) {
            return Result.ok(recommenderService.recommendForUser(user.getId(), limit));
        }
        return Result.ok(behaviorService.topHot(7, limit));
    }
}
