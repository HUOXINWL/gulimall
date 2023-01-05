package cn.huoxinwl.gulimall.coupon.dao;

import cn.huoxinwl.gulimall.coupon.entity.CouponHistoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券领取历史记录
 * 
 * @author huoxin
 * @email HUOXINWL@163.com
 * @date 2023-01-05 17:02:42
 */
@Mapper
public interface CouponHistoryDao extends BaseMapper<CouponHistoryEntity> {
	
}
