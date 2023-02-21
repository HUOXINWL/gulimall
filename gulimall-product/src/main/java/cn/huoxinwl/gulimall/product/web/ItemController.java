package cn.huoxinwl.gulimall.product.web;

import cn.huoxinwl.gulimall.product.service.SkuInfoService;
import cn.huoxinwl.gulimall.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;

@Controller
public class ItemController {
    @Autowired
    private SkuInfoService skuInfoService;
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId")Long skuId, Model model) throws ExecutionException, InterruptedException {
        System.out.println("准备查询" + skuId + "详情");

        SkuItemVo vos = skuInfoService.item(skuId);
        model.addAttribute("item",vos);

        return "item";
    }
}
