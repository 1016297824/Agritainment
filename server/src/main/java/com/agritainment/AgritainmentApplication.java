package com.agritainment;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.agritainment.mapper")
public class AgritainmentApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgritainmentApplication.class, args);
    }
}
