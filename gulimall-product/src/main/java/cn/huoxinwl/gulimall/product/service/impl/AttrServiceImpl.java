package cn.huoxinwl.gulimall.product.service.impl;

import cn.huoxinwl.gulimall.product.dao.AttrAttrgroupRelationDao;
import cn.huoxinwl.gulimall.product.dao.AttrGroupDao;
import cn.huoxinwl.gulimall.product.dao.CategoryDao;
import cn.huoxinwl.gulimall.product.entity.AttrAttrgroupRelationEntity;
import cn.huoxinwl.gulimall.product.entity.AttrGroupEntity;
import cn.huoxinwl.gulimall.product.entity.CategoryEntity;
import cn.huoxinwl.gulimall.product.service.CategoryService;
import cn.huoxinwl.gulimall.product.vo.AttrRespVo;
import cn.huoxinwl.gulimall.product.vo.AttrVo;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.huoxinwl.common.utils.PageUtils;
import cn.huoxinwl.common.utils.Query;

import cn.huoxinwl.gulimall.product.dao.AttrDao;
import cn.huoxinwl.gulimall.product.entity.AttrEntity;
import cn.huoxinwl.gulimall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Attr;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private AttrGroupDao attrGroupDao;
    @Autowired
    private CategoryDao categoryDao;
    @Autowired
    private AttrAttrgroupRelationDao relationDao;
    @Autowired
    private CategoryService categoryService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveAttr(AttrVo attr) {
        log.debug("attrVo的数据"+attr.toString());
        //1. 保存基本数据
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr,attrEntity);
        this.save(attrEntity);
        //2.保存关联关系
        AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
        attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
        attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
        relationDao.insert(attrAttrgroupRelationEntity);
    }

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId) {
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<>();

        if (catelogId != 0){
            queryWrapper.eq("catelog_id",catelogId);
        }

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)){
            queryWrapper.and((wrapper)->{
                wrapper.eq("attr_id",key).or().like("attr_name",key);
            });
        }

        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params),queryWrapper);

        PageUtils pageUtils = new PageUtils(page);
        // 拿到分页记录
        List<AttrEntity> records = page.getRecords();

        List<AttrRespVo> respVo = records.stream().map((attrEntity) -> {

            AttrRespVo attrRespVo = new AttrRespVo();
            BeanUtils.copyProperties(attrEntity, attrRespVo);

            // 1、设置分类和分组的名字
            AttrAttrgroupRelationEntity attr_idEntity = relationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>()
                    .eq("attr_id", attrEntity.getAttrId()));
            if (attr_idEntity != null) {
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attr_idEntity.getAttrGroupId());
                attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
            }
            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            if (categoryEntity != null) {
                attrRespVo.setCatelogName(categoryEntity.getName());
            }
            return attrRespVo;
        }).collect(Collectors.toList());

        pageUtils.setList(respVo);
        return pageUtils;
    }

    @Override
    public AttrRespVo getAttrInfo(Long attrId) {
        AttrRespVo respVo = new AttrRespVo();
        AttrEntity attrEntity = this.getById(attrId);
        BeanUtils.copyProperties(attrEntity,respVo);

        //1、设置分组信息
        AttrAttrgroupRelationEntity attrgroupRelation = relationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
        if(attrgroupRelation!=null){
            respVo.setAttrGroupId(attrgroupRelation.getAttrGroupId());
            AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupRelation.getAttrGroupId());
            if(attrGroupEntity!=null){
                respVo.setGroupName(attrGroupEntity.getAttrGroupName());
            }
        }

        //2、设置分类信息
        Long catelogId = attrEntity.getCatelogId();
        Long[] catelogPath = categoryService.findCatelogPath(catelogId);
        respVo.setCatelogPath(catelogPath);

        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);
        if(categoryEntity!=null){
            respVo.setCatelogName(categoryEntity.getName());
        }


        return respVo;
    }

    @Transactional
    @Override
    public void updateAttr(AttrRespVo attr) {
        // Vo数据传输对象属性拷贝到 attrEntity对象
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.updateById(attrEntity);

        //1、修改分组关联
        AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
        // 设置attrid 以及 分组id
        relationEntity.setAttrId(attr.getAttrId());
        relationEntity.setAttrGroupId(attr.getAttrGroupId());

        // 根据 attr_id 是否可以查询到结果
        Integer count = relationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>().
                eq("attr_id", attr.getAttrId()));

        if (count > 0) {
            // 进行更新
            relationDao.update(relationEntity, new UpdateWrapper<AttrAttrgroupRelationEntity>().
                    eq("attr_id", attr.getAttrId()));
        } else {
            // 查不到意味着 没有该记录 则进行插入
            relationDao.insert(relationEntity);
        }
    }

}