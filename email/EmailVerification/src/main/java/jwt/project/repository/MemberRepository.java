package jwt.project.repository;

import jwt.project.entity.Member;
import jwt.project.entity.enums.SocialType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByLoginId(String loginId);
    Optional<Member> findBySocialIdAndSocialType(String socialId, SocialType socialType);
    Optional<Member> findByEmailVerificationToken(String emailVerificationToken);
}
