package com.dodo.dodoserver.global.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

	@Bean(name = "fcmExecutor")
	public Executor fcmExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(10);        // 기본 쓰레드
		executor.setMaxPoolSize(20);        // 최대 확장 쓰레드
		executor.setQueueCapacity(500);     // 대기 큐
		executor.setThreadNamePrefix("FCM-Worker-"); // 모니터링 및 디버깅용
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // 큐가 가득 차면 호출한 쓰레드(Main)가 알림 로직 처리
		executor.initialize();
		return executor;
	}

	@Bean(name = "batchLauncherExecutor")
	public Executor batchLauncherExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2);         // 배치 런처는 동시에 많이 뜰 필요가 없으므로 적게 설정
		executor.setMaxPoolSize(5);          // 최대 확장 쓰레드
		executor.setQueueCapacity(100);      // 대기 큐
		executor.setThreadNamePrefix("Batch-Launcher-");
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		executor.initialize();
		return executor;
	}
}
