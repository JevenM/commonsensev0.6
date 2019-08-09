package com.captcha.commonsense;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * @Name: com.captcha.commonsense/commonsense
 * @Auther: 毛文杰
 * @Date: 12/17/2018 12:01
 * @Description:
 */
@Configuration
public class MySpringMVCConfiguration extends WebMvcConfigurationSupport {
    /**
     * @description:
     * @param: registry
     * @return: void
     * @auther: 毛文杰
     * @date: 12/17/2018 12:02 PM
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
    }

    /**
     * 以前要访问一个页面需要先创建个Controller控制类，再写方法跳转到页面
     * 在这里配置后就不需要那么麻烦了，直接访问http://localhost/login就跳转到login.html页面了,注意默认错误页面在error文件夹下，要把地址写对
     * @param registry
     */
    @Override
    protected void addViewControllers(ViewControllerRegistry registry) {
//        registry.addViewController("/").setViewName("inde");
        registry.addViewController("/sessions/login_404").setViewName("/error/404");
//        registry.addViewController("/500").setViewName("/error/500");
        super.addViewControllers(registry);
    }
}
