package batch.project.config;

import batch.project.entity.Member;
import batch.project.repository.MemberRepository;
import batch.project.utils.JobCompletionNotificationListener;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final MemberRepository memberRepository;

    // 1. Reader: JPA로 Member 엔티티를 페이지 단위로 읽기
    @Bean
    public JpaPagingItemReader<Member> memberReader() {
        return new JpaPagingItemReaderBuilder<Member>()
                .name("memberReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT m FROM Member m")
                .pageSize(100)
                .build();
    }

    // 2. Processor: 이름을 대문자로 변환
    @Bean
    public ItemProcessor<Member, Member> memberProcessor() {
        return member -> {
            member.setName(member.getName().toUpperCase());
            return member;
        };
    }

    // 3. Writer: DB에 저장 (업데이트)
    @Bean
    public RepositoryItemWriter<Member> memberWriter() {
        RepositoryItemWriter<Member> writer = new RepositoryItemWriter<>();
        writer.setRepository(memberRepository);
        writer.setMethodName("save");
        return writer;
    }

    // 4. Step: chunk 단위로 Reader→Processor→Writer 연결
    @Bean
    public Step memberBatchStep(JpaPagingItemReader<Member> reader,
                                ItemProcessor<Member, Member> processor,
                                RepositoryItemWriter<Member> writer) {
        return new StepBuilder("memberBatchStep", jobRepository)
                .<Member, Member>chunk(100, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    // 5. Job: Step을 실행하는 배치
    @Bean
    public Job memberBatchJob(Step memberBatchStep, JobCompletionNotificationListener listener) {
        return new JobBuilder("memberBatchJob", jobRepository)
                .start(memberBatchStep)
                .listener(listener)
                .build();
    }
}
