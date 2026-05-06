package com.agritainment;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.agritainment.mapper")
@EnableAsync
public class AgritainmentApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgritainmentApplication.class, args);
    }
}
