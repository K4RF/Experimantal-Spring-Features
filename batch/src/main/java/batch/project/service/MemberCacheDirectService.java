package batch.project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import batch.project.entity.Member;
import batch.project.entity.MemberCache;
import batch.project.repository.MemberCacheRepository;

@Service
@RequiredArgsConstructor
public class MemberCacheDirectService {
    private final MemberCacheRepository memberCacheRepository;

    public void saveMemberCache(Member member) {
        MemberCache cache = MemberCache.builder()
                .email(member.getEmail())
                .name(member.getName())
                .role(member.getRole().name())
                .emailVerified(member.isEmailVerified())
                .build();
        memberCacheRepository.save(cache);
    }

    public MemberCache getMemberCache(String email) {
        return memberCacheRepository.findById(email).orElse(null);
    }

    public void deleteMemberCache(String email) {
        memberCacheRepository.deleteById(email);
    }
}
