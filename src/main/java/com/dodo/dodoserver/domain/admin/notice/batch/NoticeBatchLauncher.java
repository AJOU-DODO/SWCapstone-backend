package com.dodo.dodoserver.domain.admin.notice.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NoticeBatchLauncher {

    private final JobLauncher jobLauncher;
    private final Job noticePublishJob;

    /**
     * Batch Job을 비동기로 실행합니다.
     * 별도의 컴포넌트로 분리하여 Self-invocation 문제를 해결하고, 
     * 상위 트랜잭션과 분리된 스레드에서 실행되도록 합니다.
     */
    @Async("batchLauncherExecutor")
    public void runPublishJobAsync(Long noticeId, String title, String noticeCategoryName) {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("noticeId", noticeId)
                    .addString("title", title)
                    .addString("noticeCategoryName", noticeCategoryName)
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(noticePublishJob, jobParameters);
            log.info("Successfully launched Notice Publish Batch Job for noticeId: {}, title: {}", noticeId, title);
        } catch (Exception e) {
            log.error("Failed to launch Notice Publish Batch Job for noticeId: {}", noticeId, e);
        }
    }
}
