package cn.huoxinwl.gulimall.product.web;

import cn.huoxinwl.gulimall.product.entity.CategoryEntity;
import cn.huoxinwl.gulimall.product.service.CategoryService;
import cn.huoxinwl.gulimall.product.vo.Catelog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class IndexController {
    @Resource
    private CategoryService categoryService;
    @Autowired
    private RedissonClient redission;

    @GetMapping(value = {"/","/index.html"})
    private String indexPage(Model model){
        //1、查出所有的一级分类
        List<CategoryEntity> categoryEntities = categoryService.getLevel1Categorys();
        model.addAttribute("categories",categoryEntities);

        return "index";
    }

    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatalogJson() {

        Map<String, List<Catelog2Vo>> catalogJson = categoryService.getCatalogJson();

        return catalogJson;

    }

    @RequestMapping("/hello")
    @ResponseBody
    public String hello(){
        // 1、获取一把锁，只要锁得名字一样，就是同一把锁
        RLock lock = redission.getLock("my-lock");

        // 2、加锁
        //lock.lock(); // 阻塞式等待，默认加的锁都是30s时间
        // 1、锁的自动续期，如果业务超长，运行期间自动给锁续上新的30s，不用担心业务时间长，锁自动过期后被删掉
        // 2、加锁的业务只要运行完成，就不会给当前锁续期，即使不手动解锁，锁默认会在30s以后自动删除

        lock.lock(10, TimeUnit.SECONDS); //10s 后自动删除
        //问题 lock.lock(10, TimeUnit.SECONDS) 在锁时间到了后，不会自动续期
        // 1、如果我们传递了锁的超时时间，就发送给 redis 执行脚本，进行占锁，默认超时就是我们指定的时间
        // 2、如果我们为指定锁的超时时间，就是用 30 * 1000 LockWatchchdogTimeout看门狗的默认时间、
        //      只要占锁成功，就会启动一个定时任务，【重新给锁设置过期时间，新的过期时间就是看门狗的默认时间】,每隔10s就自动续期
        //      internalLockLeaseTime【看门狗时间】 /3,10s

        //最佳实践
        // 1、lock.lock(10, TimeUnit.SECONDS);省掉了整个续期操作，手动解锁

        try {
            System.out.println("加锁成功，执行业务..." + Thread.currentThread().getId());
            Thread.sleep(15000);
        } catch (Exception e) {

        } finally {
            // 解锁 将设解锁代码没有运行，reidsson会不会出现死锁
            System.out.println("释放锁...." + Thread.currentThread().getId());
            lock.unlock();
        }

        return "hello";
    }
}
