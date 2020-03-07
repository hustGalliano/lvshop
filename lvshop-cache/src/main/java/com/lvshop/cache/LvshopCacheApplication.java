package com.lvshop.cache;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import com.lvshop.cache.listener.InitListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;


@EnableDubbo
@SpringBootApplication
@ComponentScan(
        basePackages = {
                "com.lvshop.cache.service",
                "com.lvshop.cache.controller",
                "com.lvshop.cache.config",
                "com.lvshop.cache.kafka"
        })
public class LvshopCacheApplication {

    // 监听注册器
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Bean
    public ServletListenerRegistrationBean servletListenerRegistrationBean() {
    	ServletListenerRegistrationBean servletListenerRegistrationBean = 
    			new ServletListenerRegistrationBean();
    	servletListenerRegistrationBean.setListener(new InitListener());
    	return servletListenerRegistrationBean;
    }

    public static void main(String[] args) {
        SpringApplication.run(LvshopCacheApplication.class, args);
    }
}