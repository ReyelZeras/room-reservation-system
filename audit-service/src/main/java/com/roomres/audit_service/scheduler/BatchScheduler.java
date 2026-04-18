package com.roomres.audit_service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job archiveAuditLogsJob;

    // A cada 2 minutos dispara a rotina de limpeza.
    // Em produção seria algo como "0 0 3 * * *" (Todo dia às 3 da manhã)
    @Scheduled(cron = "0 */2 * * * *")
    public void runArchiveJob() {
        log.info("⏰ SCHEDULER: Acordando para executar rotina de expurgo de logs antigos...");
        try {
            // JobParameters é crucial! O Spring Batch precisa de parâmetros únicos
            // para considerar cada disparo uma "execução nova".
            JobParameters params = new JobParametersBuilder()
                    .addLong("executionTime", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(archiveAuditLogsJob, params);
        } catch (Exception e) {
            log.error("Erro na execução do Job de Auditoria: ", e);
        }
    }
}