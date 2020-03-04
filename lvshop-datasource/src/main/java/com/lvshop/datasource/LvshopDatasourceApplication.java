package com.lvshop.datasource;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
//import com.lvshop.datasource.listener.InitListener;
//import org.apache.ibatis.session.SqlSessionFactory;
//import org.apache.tomcat.jdbc.pool.DataSource;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;



/**
 * 数据源服务启动类
 * @author Galliano
 * 数据源服务直接连接数据库，是缓存服务的依赖服务
 * 将数据源服务作为Provider加入到Dubbo中，让Consumer缓存服务进行RPC
 */

@SpringBootApplication
@EnableTransactionManagement
@EnableDubbo
@MapperScan("com.lvshop.datasource.mapper")
public class LvshopDatasourceApplication {

//    @Bean
//    @ConfigurationProperties(prefix="spring.datasource")
//    public DataSource dataSource() {
//        return new DataSource();
//    }
//
//    @Bean
//    public SqlSessionFactory sqlSessionFactoryBean() throws Exception {
//        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
//        sqlSessionFactoryBean.setDataSource(dataSource());
//        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
//        sqlSessionFactoryBean.setMapperLocations(resolver.getResources("classpath:/mybatis/*.xml"));
//        return sqlSessionFactoryBean.getObject();
//    }

//    @Bean
//    public PlatformTransactionManager transactionManager() {
//        return new DataSourceTransactionManager(dataSource());
//    }


    /**
     * 注册监听器
     * @return
     */
//    @SuppressWarnings({ "rawtypes", "unchecked" })
//    @Bean
//    public ServletListenerRegistrationBean servletListenerRegistrationBean() {
//        ServletListenerRegistrationBean servletListenerRegistrationBean =
//                new ServletListenerRegistrationBean();
//        servletListenerRegistrationBean.setListener(new InitListener());
//        return servletListenerRegistrationBean;
//    }


//    @Bean
//    public PlatformTransactionManager txManager(DataSource dataSource) {
//        return new DataSourceTransactionManager(dataSource);
//    }
//
//    @Bean
//    public Object testBean(PlatformTransactionManager platformTransactionManager){
//        System.out.println(">>>>>>>>>>" + platformTransactionManager.getClass().getName());
//        return new Object();
//    }

    public static void main(String[] args) {
        SpringApplication.run(LvshopDatasourceApplication.class, args);
    }

}
