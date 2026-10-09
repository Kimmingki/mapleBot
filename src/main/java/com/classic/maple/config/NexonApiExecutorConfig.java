package com.classic.maple.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class NexonApiExecutorConfig {

    // 넥슨 API 병렬 호출 전용 실행기 (작업당 가상 스레드, 종료 시 shutdown)
    @Bean(destroyMethod = "shutdown")
    public ExecutorService nexonApiExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
