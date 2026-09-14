package com.shop.web;

import com.shop.common.WebUtil;
import com.shop.entity.Category;
import com.shop.entity.Product;
import com.shop.entity.User;
import com.shop.service.BehaviorService;
import com.shop.service.CategoryService;
import com.shop.service.ProductService;
import com.shop.ai.RecommenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 前台商城页面：首页 / 搜索 / 商品详情
 */
@Controller
public class HomeController {

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ProductService productService;
    @Autowired
    private RecommenderService recommenderService;
    @Autowired
    private BehaviorService behaviorService;

    @GetMapping({"/", "/home"})
    public String home(Model model, HttpServletRequest request) {
        model.addAttribute("categories", categoryService.listAll());
        model.addAttribute("hotProducts", productService.hotList(8));

        // AI个性化推荐：登录用户走ItemCF，未登录展示热门
        User user = WebUtil.getLoginUser(request);
        if (user != null) {
            List<Product> recommend = recommenderService.recommendForUser(user.getId(), 8);
            model.addAttribute("aiTitle", "🤖 AI 猜你喜欢（根据您的浏览/购买行为推荐）");
            model.addAttribute("aiProducts", recommend);
        } else {
            model.addAttribute("aiTitle", "🔥 大家都在买（登录后解锁AI个性化推荐）");
            model.addAttribute("aiProducts", productService.hotList(4));
        }
        return "front/index";
    }

    @GetMapping("/search")
    public String search(String keyword, Long categoryId, String sort, Integer page, Model model) {
        int p = page == null || page < 1 ? 1 : page;
        com.shop.common.PageResult<Product> result = productService.page(
                keyword, categoryId, 1, null, sort, p, 12, true);
        model.addAttribute("categories", categoryService.listAll());
        model.addAttribute("result", result);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("sort", sort);
        model.addAttribute("page", p);
        return "front/search";
    }

    @GetMapping("/product/{id}")
    public String product(@PathVariable Long id, Model model, HttpServletRequest request) {
        Product product = productService.detail(id);
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.listAll());
        // "买了又买"：ItemCF 关联推荐
        model.addAttribute("related", recommenderService.relatedProducts(id, 4));

        // 记录浏览行为（AI推荐数据源）
        User user = WebUtil.getLoginUser(request);
        if (user != null) {
            behaviorService.log(user.getId(), id, com.shop.entity.BehaviorLog.TYPE_VIEW);
        }
        return "front/product";
    }

    /** web.xml 404/500 error-page 指向这里 */
    @GetMapping("/error/404")
    public String notFound() {
        return "front/404";
    }

    @GetMapping("/error/500")
    public String serverError() {
        return "front/500";
    }
}
