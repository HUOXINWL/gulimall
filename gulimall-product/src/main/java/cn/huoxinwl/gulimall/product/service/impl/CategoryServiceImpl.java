package cn.huoxinwl.gulimall.product.service.impl;

import cn.huoxinwl.gulimall.product.service.CategoryBrandRelationService;
import cn.huoxinwl.gulimall.product.vo.Catelog2Vo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.huoxinwl.common.utils.PageUtils;
import cn.huoxinwl.common.utils.Query;

import cn.huoxinwl.gulimall.product.dao.CategoryDao;
import cn.huoxinwl.gulimall.product.entity.CategoryEntity;
import cn.huoxinwl.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1. ??????????????????
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);

        //2. ???????????????
        List<CategoryEntity> level1Menus = categoryEntities.stream().filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                .map((menu)->{
                    menu.setChildren(getChildrens(menu,categoryEntities));
                    return menu;
                }).sorted((menu1,menu2)->{
                    return (menu1.getSort()==null?0:menu1.getSort()) - (menu2.getSort()==null?0:menu2.getSort());
                }).collect(Collectors.toList());
        return level1Menus;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO 1. ?????????????????????????????????????????????????????????
        //????????????
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId,paths);

        Collections.reverse(parentPath);

        return (Long[]) parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     * ?????????????????????????????????
     *
     * @CacheEvict:????????????
     *      * @CachePut:?????????????????????????????????
     *      * 1????????????????????????????????????@Caching
     *      * 2????????????????????????????????????????????? @CacheEvict(value = "category",allEntries = true)
     *      * 3???????????????????????????????????????????????????????????????
     * @param category
     */
    // @Caching(evict = {
    //         @CacheEvict(value = "category",key = "'getLevel1Categorys'"),
    //         @CacheEvict(value = "category",key = "'getCatalogJson'")
    // })
    @CacheEvict(value = "category",allEntries = true)//????????????????????????????????????
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????????????????????????????(?????????????????????)???
     * ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     * ????????????
     *      ???????????????????????????????????????
     *      key??????????????????:???????????????::SimpleKey::[](????????????key???)
     *      ?????????value??????????????????jdk?????????????????????????????????????????????redis???
     *      ??????????????? -1???
     *
     *   ??????????????????key?????????
     *      ?????????????????????key???key???????????????????????????Spel
     *      ????????????????????????????????????:?????????????????????????????????
     *      ??????????????????json??????
     *
     * 4???Spring-Cache??????????????????
     *      *  1???????????????
     *      *      ???????????????????????????null???????????????????????????????????????
     *      *      ???????????????????????????????????????????????????????????????????????????????????????????????? ? ?????????????????????;??????sync = true?????????????????????
     *      *      ????????????????????????key????????????????????????????????????????????????????????????
     *      *  2)?????????????????????????????????????????????
     *      *      1?????????????????????
     *      *      2????????????Canal,?????????MySQL??????????????????Redis
     *      *      3???????????????????????????????????????????????????
     *      *
     *      *  ?????????
     *      *      ?????????????????????????????????????????????????????????????????????????????????????????????Spring-Cache???????????????(????????????????????????????????????????????????)
     *      *      ???????????????????????????
     *      *
     *      *  ?????????
     *      *      CacheManager(RedisCacheManager)->Cache(RedisCache)->Cache?????????????????????
     *      * @return
     */
    @Cacheable(value = {"category"},key = "#root.method.name")
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        System.out.println("getLevel1Categorys........");
        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(
                new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return categoryEntities;
    }

    @Cacheable(value = "category",key = "#root.methodName")
    @Override
    public Map<String, List<Catelog2Vo>>  getCatalogJson(){
        System.out.println("??????????????????");

        /**
         * ???????????????????????????????????????
         */
        List<CategoryEntity> selectList = this.baseMapper.selectList(null);

        //1?????????????????????
        //1???1???????????????????????????
        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);

        //????????????
        Map<String, List<Catelog2Vo>> parentCid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //1???????????????????????????,???????????????????????????????????????
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());

            //2????????????????????????
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName().toString());

                    //1????????????????????????????????????????????????vo
                    List<CategoryEntity> level3Catelog = getParent_cid(selectList, l2.getCatId());

                    if (level3Catelog != null) {
                        List<Catelog2Vo.Category3Vo> category3Vos = level3Catelog.stream().map(l3 -> {
                            //2????????????????????????
                            Catelog2Vo.Category3Vo category3Vo = new Catelog2Vo.Category3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());

                            return category3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(category3Vos);
                    }

                    return catelog2Vo;
                }).collect(Collectors.toList());
            }

            return catelog2Vos;
        }));
        return parentCid;
    }

    /**
     * TODO ???????????????????????? OutOfDirectMemoryError
     * 1???SpringBoot2.0?????????????????? Lettuce????????????redis???????????????????????? netty??????????????????
     * 2???lettuce ???bug??????netty?????????????????????-Xmx300m netty ???????????????????????????????????????????????? -Xmx300m
     *      ????????????-Dio.netty.maxDirectMemory ????????????
     *   ???????????? ???????????? -Dio.netty.maxDirectMemory????????????
     *   1????????? lettuce????????????2??? ????????????jedis
     *   redisTemplate:
     *   lettuce???jedis ??????redis?????????????????????Spring????????????
     * @return
     */
//    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson2(){
        //???????????????json?????????????????????json???????????????????????????????????????????????????????????????????????????

        //1. ?????????????????????????????????????????????json?????????
        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
        if (StringUtils.isEmpty(catalogJSON)){
            //2. ?????????????????????????????????
            Map<String, List<Catelog2Vo>> catalogJsonFromDB = getCatalogJsonFromDbWithRedissonLock();

            return catalogJsonFromDB;
        }

        //???????????????????????????
        Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>(){});
        return result;
    }

    /**
     * ???????????????????????????????????????????????????????????????
     * ?????????????????????
     * 1)???????????????
     * 2)???????????????
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedissonLock() {

        //1????????????????????????redis??????
        //??????????????????????????????:?????????????????????????????????11???????????? product-11-lock
        //RLock catalogJsonLock = redissonClient.getLock("catalogJson-lock");
        //????????????
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("catalogJson-lock");

        RLock rLock = readWriteLock.readLock();

        Map<String, List<Catelog2Vo>> dataFromDb = null;
        try {
            rLock.lock();
            //????????????...????????????
            dataFromDb = getDataFromDb();
        } finally {
            rLock.unlock();
        }
        //??????redis???????????????????????????????????????
        //????????????????????????????????????=????????? lua????????????
        // String lockValue = stringRedisTemplate.opsForValue().get("lock");
        // if (uuid.equals(lockValue)) {
        //     //?????????????????????
        //     stringRedisTemplate.delete("lock");
        // }

        return dataFromDb;

    }

    /**
     * ?????????????????????????????????::????????????
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedisLock() {

        //1????????????????????????redis??????      ?????????????????????????????????????????????????????????????????????????????????
        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid,300, TimeUnit.SECONDS);
        if (lock) {
            System.out.println("????????????????????????...");
            Map<String, List<Catelog2Vo>> dataFromDb = null;
            try {
                //????????????...????????????
                dataFromDb = getDataFromDb();
            } finally {
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

                //?????????
                stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), uuid);

            }
            //??????redis???????????????????????????????????????
            //????????????????????????????????????=????????? lua????????????
            // String lockValue = stringRedisTemplate.opsForValue().get("lock");
            // if (uuid.equals(lockValue)) {
            //     //?????????????????????
            //     stringRedisTemplate.delete("lock");
            // }

            return dataFromDb;
        } else {
            System.out.println("????????????????????????...????????????...");
            //????????????...????????????
            //??????????????????
            try { TimeUnit.MILLISECONDS.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
            return getCatalogJsonFromDbWithRedisLock();     //???????????????
        }
    }


    /**
     * ?????????????????????????????????::?????????
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithLocalLock() {

        // //?????????????????????????????????
        // Map<String, List<Catelog2Vo>> catalogJson = (Map<String, List<Catelog2Vo>>) cache.get("catalogJson");
        // if (cache.get("catalogJson") == null) {
        //     //????????????
        //     //???????????????????????????
        // }

        //????????????????????????????????????????????????????????????
        //1???synchronized (this)???SpringBoot?????????????????????????????????????????????
        //TODO ????????????synchronized???JUC???Lock),?????????????????????????????????????????????????????????????????????
        synchronized (this) {

            //?????????????????????????????????????????????????????????????????????????????????????????????
            return getDataFromDb();
        }


    }

    private Map<String, List<Catelog2Vo>> getDataFromDb() {
        //?????????????????????????????????????????????????????????????????????????????????????????????
        String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
        if (!StringUtils.isEmpty(catalogJson)) {
            //???????????????????????????
            Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });

            return result;
        }

        System.out.println("??????????????????");

        /**
         * ???????????????????????????????????????
         */
        List<CategoryEntity> selectList = this.baseMapper.selectList(null);

        //1?????????????????????
        //1???1???????????????????????????
        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);

        //????????????
        Map<String, List<Catelog2Vo>> parentCid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //1???????????????????????????,???????????????????????????????????????
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());

            //2????????????????????????
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName().toString());

                    //1????????????????????????????????????????????????vo
                    List<CategoryEntity> level3Catelog = getParent_cid(selectList, l2.getCatId());

                    if (level3Catelog != null) {
                        List<Catelog2Vo.Category3Vo> category3Vos = level3Catelog.stream().map(l3 -> {
                            //2????????????????????????
                            Catelog2Vo.Category3Vo category3Vo = new Catelog2Vo.Category3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());

                            return category3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(category3Vos);
                    }

                    return catelog2Vo;
                }).collect(Collectors.toList());
            }

            return catelog2Vos;
        }));

        //3?????????????????????????????????,???????????????json
        String valueJson = JSON.toJSONString(parentCid);
        stringRedisTemplate.opsForValue().set("catalogJson", valueJson, 1, TimeUnit.DAYS);

        return parentCid;
    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList,Long parentCid) {
        List<CategoryEntity> categoryEntities = selectList.stream().filter(item -> item.getParentCid().equals(parentCid)).collect(Collectors.toList());
        return categoryEntities;
        // return this.baseMapper.selectList(
        //         new QueryWrapper<CategoryEntity>().eq("parent_cid", parentCid));
    }

    //????????????
    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        // 1?????????????????????id
        paths.add(catelogId);
        // 2???????????????id?????? Category ??????
        CategoryEntity byId = this.getById(catelogId);
        // 3???????????????????????? ???????????????????????????
        if (byId.getParentCid() != 0){
            findParentPath(byId.getParentCid(),paths);
        }
        return paths;
    }

    //????????????????????????????????????
    private List<CategoryEntity> getChildrens(CategoryEntity root,List<CategoryEntity> all){
        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid() == root.getCatId();
        }).map(categoryEntity -> {
            //1.???????????????
            categoryEntity.setChildren(getChildrens(categoryEntity,all));
            return categoryEntity;
        }).sorted((menu1,menu2)->{
            //2. ????????????
            return (menu1.getSort()==null?0:menu1.getSort()) - (menu2.getSort()==null?0:menu2.getSort());
        }).collect(Collectors.toList());

        return children;
    }

}