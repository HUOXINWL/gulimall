package cn.huoxinwl.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.huoxinwl.common.utils.PageUtils;
import cn.huoxinwl.gulimall.order.entity.OrderReturnReasonEntity;

import java.util.Map;

/**
 * ้่ดงๅๅ 
 *
 * @author huoxin
 * @email HUOXINWL@163.com
 * @date 2023-01-05 17:19:34
 */
public interface OrderReturnReasonService extends IService<OrderReturnReasonEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

