package domain.driven.repository;

import domain.driven.entity.Member;
import domain.driven.entity.enums.SocialType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryAdapter implements MemberRepositoryPort {

    private final MemberRepository jpa;

    @Override
    public Optional<Member> findByLoginId(String loginId) {
        return jpa.findByLoginId(loginId);
    }

    @Override
    public Optional<Member> findBySocialIdAndSocialType(
            String socialId, SocialType socialType) {
        return jpa.findBySocialIdAndSocialType(socialId, socialType);
    }

    @Override
    public Member save(Member member) {
        return jpa.save(member);
    }
}