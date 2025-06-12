package batch.project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import batch.project.entity.Member;
import batch.project.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class MemberQueryService {
    private final MemberRepository memberRepository;

    @Cacheable(value = "memberCache", key = "#email")
    public Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("회원 없음"));
    }

    // 회원 정보 변경 시 캐시 삭제
    @CacheEvict(value = "memberCache", key = "#email")
    public void evictMemberCache(String email) {
        // 캐시만 삭제 (DB 변경은 별도 처리)
    }
}
