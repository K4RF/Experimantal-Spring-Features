package batch.project.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JobCompletionNotificationListener extends JobExecutionListenerSupport{
    @Override
    public void afterJob(JobExecution jobExecution) {
        if(jobExecution.getStatus() == BatchStatus.COMPLETED){
            log.info("배치 작업이 완료되었습니다!");
        }
    }
}
