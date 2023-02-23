package cn.huoxinwl.gulimall.member.dao;

import cn.huoxinwl.gulimall.member.entity.MemberLevelEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员等级
 * 
 * @author huoxin
 * @email HUOXINWL@163.com
 * @date 2023-01-05 17:07:46
 */
@Mapper
public interface MemberLevelDao extends BaseMapper<MemberLevelEntity> {

    MemberLevelEntity getDefaultLevel();
}
