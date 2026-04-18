package com.roomres.audit_service.config;

import com.roomres.audit_service.model.AuditLog;
import com.roomres.audit_service.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.Collections;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final AuditRepository auditRepository;

    // 1. READER
    @Bean
    @StepScope // <-- A MÁGICA ESTÁ AQUI: Garante que o método rode a cada disparo do cron!
    public RepositoryItemReader<AuditLog> oldAuditLogsReader() {
        // Agora o 'now()' será a hora EXATA em que o relógio disparar (ex: 22:50, 22:52, etc)
        LocalDateTime cutoffDate = LocalDateTime.now().minusMinutes(1);
        log.info(">>>> BATCH READER: Buscando registros anteriores a {} <<<<", cutoffDate);

        return new RepositoryItemReaderBuilder<AuditLog>()
                .name("oldAuditLogsReader")
                .repository(auditRepository)
                .methodName("findByTimestampBefore")
                .arguments(Collections.singletonList(cutoffDate))
                .sorts(Collections.singletonMap("timestamp", Sort.Direction.ASC))
                .pageSize(10)
                .build();
    }

    // 2. PROCESSOR
    @Bean
    public ItemProcessor<AuditLog, AuditLog> auditLogProcessor() {
        return item -> item;
    }

    // 3. WRITER
    @Bean
    public ItemWriter<AuditLog> compositeAuditWriter() {
        return items -> {
            log.info(">>>> BATCH WRITER: Extraindo {} registros para arquivo .csv (Simulado) <<<<", items.size());
            log.info(">>>> BATCH WRITER: Limpando base de dados principal... <<<<");
            auditRepository.deleteAll(items);
        };
    }

    // 4. STEP
    @Bean
    public Step archiveOldLogsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("archiveOldLogsStep", jobRepository)
                .<AuditLog, AuditLog>chunk(10, transactionManager)
                .reader(oldAuditLogsReader())
                .processor(auditLogProcessor())
                .writer(compositeAuditWriter())
                .build();
    }

    // 5. JOB
    @Bean
    public Job archiveAuditLogsJob(JobRepository jobRepository, Step archiveOldLogsStep) {
        return new JobBuilder("archiveAuditLogsJob", jobRepository)
                .start(archiveOldLogsStep)
                .build();
    }
}