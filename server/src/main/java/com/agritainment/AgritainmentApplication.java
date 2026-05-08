package com.agritainment;

import com.agritainment.config.MdcTaskDecorator;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@SpringBootApplication
@MapperScan("com.agritainment.mapper")
@EnableAsync
@EnableScheduling
public class AgritainmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgritainmentApplication.class, args);
    }

    @Bean(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    public Executor asyncExecutor(MdcTaskDecorator mdcTaskDecorator) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setTaskDecorator(mdcTaskDecorator);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}
