package cn.huoxinwl.gulimall.member.service;

import cn.huoxinwl.gulimall.member.vo.MemberUserLoginVo;
import cn.huoxinwl.gulimall.member.vo.MemberUserRegisterVo;
import cn.huoxinwl.gulimall.member.vo.SocialUser;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.huoxinwl.common.utils.PageUtils;
import cn.huoxinwl.gulimall.member.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author huoxin
 * @email HUOXINWL@163.com
 * @date 2023-01-05 17:07:46
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(MemberUserRegisterVo vo);
    /**
     * 用户登录
     * @param vo
     * @return
     */
    MemberEntity login(MemberUserLoginVo vo);

    /**
     * 社交用户的登录
     * @param socialUser
     * @return
     */
    MemberEntity login(SocialUser socialUser) throws Exception;
}

