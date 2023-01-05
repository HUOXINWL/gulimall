package cn.huoxinwl.gulimall.order.dao;

import cn.huoxinwl.gulimall.order.entity.OrderSettingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单配置信息
 * 
 * @author huoxin
 * @email HUOXINWL@163.com
 * @date 2023-01-05 17:19:34
 */
@Mapper
public interface OrderSettingDao extends BaseMapper<OrderSettingEntity> {
	
}
