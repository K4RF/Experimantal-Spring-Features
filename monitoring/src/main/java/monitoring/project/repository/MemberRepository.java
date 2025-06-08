package monitoring.project.repository;

import monitoring.project.entity.Member;
import monitoring.project.entity.enums.SocialType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    Optional<Member> findBySocialIdAndSocialType(String socialId, SocialType socialType);
    Optional<Member> findByResetPasswordToken(String token);
    Optional<Member> findByEmailVerificationToken(String emailVerificationToken);
    boolean existsByEmail(String email);
}
