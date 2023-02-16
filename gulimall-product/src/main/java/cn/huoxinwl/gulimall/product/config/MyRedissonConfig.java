package cn.huoxinwl.gulimall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class MyRedissonConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redisson() throws IOException{
        //1. 创建配置
        Config config = new Config();
        //Redis url should start with redis:// or rediss://
        config.useSingleServer().setAddress("redis://hxwl:6379");
        //2、根据Config创建出RedissonClient实例
        return Redisson.create(config);
    }
}
