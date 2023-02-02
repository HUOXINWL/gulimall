package cn.huoxinwl.gulimall.product.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement//开启使用
@MapperScan("cn.huoxinwl.gulimall.product.dao")
public class MyBatisConfig {

    //引入分页插件
    @Bean
    public PaginationInterceptor paginationInterceptor(){
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();

        //设置请求的页面大于最大页后操作，true跳回到首页
        paginationInterceptor.setOverflow(true);
        //设置最大单页限制数量，默认500条，-1无限
        paginationInterceptor.setLimit(500);
        return paginationInterceptor;
    }
}
