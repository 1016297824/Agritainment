package com.agritainment;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.agritainment.mapper")
@EnableAsync
@EnableScheduling
public class AgritainmentApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgritainmentApplication.class, args);
    }
}
