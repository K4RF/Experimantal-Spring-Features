package domain.driven.repository;


import domain.driven.entity.Member;
import domain.driven.entity.enums.SocialType;

import java.util.Optional;

public interface MemberRepositoryPort {

    Optional<Member> findByLoginId(String loginId);

    Optional<Member> findBySocialIdAndSocialType(
            String socialId, SocialType socialType);

    Member save(Member member);
}