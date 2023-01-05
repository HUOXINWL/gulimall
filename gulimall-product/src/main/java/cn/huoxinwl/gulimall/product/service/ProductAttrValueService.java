package cn.huoxinwl.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.huoxinwl.common.utils.PageUtils;
import cn.huoxinwl.gulimall.product.entity.ProductAttrValueEntity;

import java.util.Map;

/**
 * spu属性值
 *
 * @author huoxin
 * @email HUOXINWL@163.com
 * @date 2023-01-05 15:32:51
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

