package com.dodo.dodoserver.domain.admin.notice.batch;

import com.dodo.dodoserver.domain.user.entity.UserDevice;
import com.dodo.dodoserver.infrastructure.fcm.FcmService;
import com.dodo.dodoserver.infrastructure.fcm.NotificationEvent;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class NoticePublishBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final FcmService fcmService;

    private static final int CHUNK_SIZE = 500;

    @Bean
    public Job noticePublishJob() {
        return new JobBuilder("noticePublishJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(noticePublishStep())
                .build();
    }

    @Bean
    @JobScope
    public Step noticePublishStep() {
        return new StepBuilder("noticePublishStep", jobRepository)
                .<UserDevice, UserDevice>chunk(CHUNK_SIZE, transactionManager)
                .reader(userDeviceTokenReader())
                .writer(fcmTokenWriter(null, null, null)) // 파라미터는 late binding으로 주입됨
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<UserDevice> userDeviceTokenReader() {
        return new JpaPagingItemReaderBuilder<UserDevice>()
                .name("userDeviceTokenReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT ud FROM UserDevice ud")
                .pageSize(CHUNK_SIZE)
                .build();
    }

    @Bean
    @StepScope
    public ItemWriter<UserDevice> fcmTokenWriter(
            @Value("#{jobParameters['noticeId']}") Long noticeId,
            @Value("#{jobParameters['title']}") String title,
            @Value("#{jobParameters['content']}") String content) {
        
        return items -> {
            List<String> tokens = items.getItems().stream()
                    .map(UserDevice::getFcmToken)
                    .collect(Collectors.toList());
            
            log.info("Sending FCM Notifications to {} tokens for noticeId: {}", tokens.size(), noticeId);
            
            if (tokens.isEmpty()) {
                return;
            }
            
            NotificationEvent event = new NotificationEvent(
                    tokens,
                    title,
                    content,
                    Map.of(
                            "type", "NOTICE",
                            "noticeId", String.valueOf(noticeId)
                    )
            );
            
            fcmService.sendNotificationSync(event);
        };
    }
}
