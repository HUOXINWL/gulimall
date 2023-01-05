package cn.huoxinwl.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.huoxinwl.common.utils.PageUtils;
import cn.huoxinwl.gulimall.product.entity.BrandEntity;

import java.util.Map;

/**
 * 品牌
 *
 * @author huoxin
 * @email HUOXINWL@163.com
 * @date 2023-01-05 15:32:51
 */
public interface BrandService extends IService<BrandEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

