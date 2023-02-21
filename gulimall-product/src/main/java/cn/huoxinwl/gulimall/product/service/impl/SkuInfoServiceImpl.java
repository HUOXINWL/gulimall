package cn.huoxinwl.gulimall.product.service.impl;

import cn.huoxinwl.common.utils.R;
import cn.huoxinwl.gulimall.product.entity.SkuImagesEntity;
import cn.huoxinwl.gulimall.product.entity.SpuInfoDescEntity;
import cn.huoxinwl.gulimall.product.service.*;
import cn.huoxinwl.gulimall.product.vo.SkuItemSaleAttrVo;
import cn.huoxinwl.gulimall.product.vo.SkuItemVo;
import cn.huoxinwl.gulimall.product.vo.SpuItemAttrGroupVo;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.huoxinwl.common.utils.PageUtils;
import cn.huoxinwl.common.utils.Query;

import cn.huoxinwl.gulimall.product.dao.SkuInfoDao;
import cn.huoxinwl.gulimall.product.entity.SkuInfoEntity;

import javax.annotation.Resource;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Resource
    private SkuImagesService skuImagesService;

    @Resource
    private SpuInfoDescService spuInfoDescService;

    @Resource
    private AttrGroupService attrGroupService;

    @Resource
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Resource
    private ThreadPoolExecutor executor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key) && !"0".equalsIgnoreCase(key)) {
            queryWrapper.and((wrapper) -> {
                wrapper.eq("sku_id",key).or().like("sku_name",key);
            });
        }

        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            queryWrapper.eq("catalog_id",catelogId);
        }

        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            queryWrapper.eq("brand_id",brandId);
        }

        String min = (String) params.get("min");
        if (!StringUtils.isEmpty(min)) {
            queryWrapper.ge("price",min);
        }

        String max = (String) params.get("max");

        if (!StringUtils.isEmpty(max)) {
            try {
                BigDecimal bigDecimal = new BigDecimal(max);
                if (bigDecimal.compareTo(BigDecimal.ZERO) == 1) {
                    queryWrapper.le("price",max);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // key:
        // catelogId: 225
        // brandId: 9
        // min: 0
        // max: 0

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        List<SkuInfoEntity> skuInfoEntities = this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
        return skuInfoEntities;
    }

    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = new SkuItemVo();

        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            //1、sku基本信息的获取  pms_sku_info
            SkuInfoEntity info = this.getById(skuId);
            skuItemVo.setInfo(info);
            return info;
        }, executor);


        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            //3、获取spu的销售属性组合
            List<SkuItemSaleAttrVo> saleAttrVos = skuSaleAttrValueService.getSaleAttrBySpuId(res.getSpuId());
            skuItemVo.setSaleAttr(saleAttrVos);
        }, executor);


        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync((res) -> {
            //4、获取spu的介绍    pms_spu_info_desc
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(res.getSpuId());
            skuItemVo.setDesc(spuInfoDescEntity);
        }, executor);


        CompletableFuture<Void> baseAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            //5、获取spu的规格参数信息
            List<SpuItemAttrGroupVo> attrGroupVos = attrGroupService.getAttrGroupWithAttrsBySpuId(res.getSpuId(), res.getCatalogId());
            skuItemVo.setGroupAttrs(attrGroupVos);
        }, executor);


        //2、sku的图片信息    pms_sku_images
        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> imagesEntities = skuImagesService.getImagesBySkuId(skuId);
            skuItemVo.setImages(imagesEntities);
        }, executor);

        //等到所有任务都完成
        CompletableFuture.allOf(saleAttrFuture,descFuture,baseAttrFuture,imageFuture).get();

        return skuItemVo;

    }

}