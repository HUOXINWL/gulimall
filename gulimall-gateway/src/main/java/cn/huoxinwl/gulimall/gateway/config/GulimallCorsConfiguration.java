package cn.huoxinwl.gulimall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class GulimallCorsConfiguration {
    @Bean
    public CorsWebFilter corsWebFilter(){
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        CorsConfiguration corsConfiguration = new CorsConfiguration();
        //1. 配置跨域
        corsConfiguration.addAllowedHeader("*");// 允许那些头
        corsConfiguration.addAllowedMethod("*");// 允许那些请求方式
        corsConfiguration.addAllowedOrigin("*");//  允许请求来源
        corsConfiguration.setAllowCredentials(true);// 是否允许携带cookie跨越
        // 注册跨越配置
        source.registerCorsConfiguration("/**",corsConfiguration);

        return new CorsWebFilter(source);
    }
}
