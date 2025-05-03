package domain.driven.repository;

import domain.driven.entity.Member;
import domain.driven.entity.enums.SocialType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByLoginId(String loginId);
    Optional<Member> findBySocialIdAndSocialType(String socialId, SocialType socialType);
}