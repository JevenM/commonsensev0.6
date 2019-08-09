package com.captcha.commonsense;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class CommonsenseApplication extends SpringBootServletInitializer {

    /**
     * @description: 去掉banner，启动器
     * @param: [args]
     * @return: void
     * @auther: 毛文杰
     * @date: 12/17/2018 11:46 PM
     */
    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(CommonsenseApplication.class);
        springApplication.setBannerMode(Banner.Mode.OFF);
        springApplication.run(args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(CommonsenseApplication.class);
    }

}

