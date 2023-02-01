package cn.huoxinwl.gulimall.product.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import cn.huoxinwl.common.valid.AddGroup;
import cn.huoxinwl.common.valid.UpdateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.huoxinwl.gulimall.product.entity.BrandEntity;
import cn.huoxinwl.gulimall.product.service.BrandService;
import cn.huoxinwl.common.utils.PageUtils;
import cn.huoxinwl.common.utils.R;

import javax.validation.Valid;
import javax.xml.ws.BindingType;


/**
 * 品牌
 *
 * @author huoxin
 * @email HUOXINWL@163.com
 * @date 2023-01-05 15:59:37
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:brand:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    //@RequiresPermissions("product:brand:info")
    public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:brand:save")
    public R save(@Validated(AddGroup.class) @RequestBody BrandEntity brand/*, BindingResult result*/){
        /*
        if (result.hasErrors()){
            Map<String,String> map = new HashMap<>();
            //1. 获取校验的错误结果
            result.getFieldErrors().forEach((item)->{
                String msg = item.getDefaultMessage();
                String field = item.getField();
                map.put(field,msg);
            });

            return R.error(400,"提交的数据不合法").put("data",map);
        }else {
            brandService.save(brand);
            return R.ok();
        }
*/
        brandService.save(brand);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:brand:update")
    public R update(@Validated(UpdateGroup.class) @RequestBody BrandEntity brand){
		brandService.updateById(brand);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:brand:delete")
    public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
