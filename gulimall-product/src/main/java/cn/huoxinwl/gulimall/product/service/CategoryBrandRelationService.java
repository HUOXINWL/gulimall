package cn.huoxinwl.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.huoxinwl.common.utils.PageUtils;
import cn.huoxinwl.gulimall.product.entity.CategoryBrandRelationEntity;

import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author huoxin
 * @email HUOXINWL@163.com
 * @date 2023-01-05 15:32:51
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

