package batch.project.service;

import jakarta.transaction.Transactional;
import batch.project.dto.SocialUserInfo;
import batch.project.dto.request.SocialRegisterRequest;
import batch.project.dto.response.LoginResponse;
import batch.project.entity.Member;
import batch.project.entity.enums.Role;
import batch.project.entity.enums.SocialType;
import batch.project.repository.MemberRepository;
import batch.project.repository.RefreshTokenRepository;
import batch.project.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService; // ✅ Redis 서비스 추가
    private final EmailService emailService;


    private Member registerMember(String email, String password, String name, Role role) {
        // 중복 이메일 체크
        if (memberRepository.existsByEmail(email)) {
            throw new IllegalStateException("이미 가입된 이메일입니다.");
        }

        // 회원 생성
        Member member = Member.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .name(name)
                .role(role)
                .build();

        // 이메일 인증 토큰 생성 및 저장
        String verificationToken = UUID.randomUUID().toString();
        member.setEmailVerificationToken(verificationToken);
        member.setEmailVerificationExpiry(LocalDateTime.now().plusHours(24));

        // 이메일 인증 링크 발송
        String siteUrl = "http://localhost:8080"; // 개발 환경용 (실제 배포 시 도메인 변경)
        emailService.sendVerificationMail(email, verificationToken, siteUrl);

        return memberRepository.save(member);
    }

    public void registerUser(String email, String password, String name) {
        registerMember(email, password, name, Role.USER);
    }

    public void registerAdmin(String email, String password, String name) {
        registerMember(email, password, name, Role.ADMIN);
    }

    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));
    }

    public Map<String, String> loginAndGetToken(String email, String password) {
        Member member = findByEmail(email);

        // 소셜 회원이 아니라면 이메일 인증 체크
        if (member.getSocialType() == null) {
            if (!member.isEmailVerified()) {
                throw new IllegalStateException("이메일 인증이 필요합니다. 이메일을 확인해주세요.");
            }
        }

        if(!passwordEncoder.matches(password, member.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다");
        }

        // AccessToken과 RefreshToken 생성
        String accessToken = jwtUtil.generateToken(member.getEmail(), member.getRole().name());
        String refreshToken = jwtUtil.refreshToken(member.getEmail());

        // ✅ Redis에 저장 (자동 TTL)
        refreshTokenService.save(email, refreshToken);

        return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
    }

    public LoginResponse socialLoginOrRegister(SocialUserInfo userInfo, SocialType socialType) {
        // socialId(구글: sub, 카카오: id, 네이버: id 등)로 회원 조회
        Optional<Member> memberOpt = memberRepository.findBySocialIdAndSocialType(userInfo.getSocialId(), socialType);
        Member member;
        if (memberOpt.isEmpty()) {
            // 회원가입
            member = new Member();
            member.setEmail(userInfo.getEmail());
            member.setName(userInfo.getName());
            member.setRole(Role.USER);
            member.setSocialType(socialType);
            member.setSocialId(userInfo.getSocialId());
            memberRepository.save(member);
        } else {
            member = memberOpt.get();
        }
        // JWT 토큰 발급 및 Redis에 refreshToken 저장
        String accessToken = jwtUtil.generateToken(member.getEmail(), member.getRole().name());
        String refreshToken = jwtUtil.refreshToken(member.getEmail());
        refreshTokenService.save(member.getEmail(), refreshToken);

        return new LoginResponse(accessToken, refreshToken, member.getEmail());
    }

    public void registerSocialUser(SocialRegisterRequest request) {
        // 비밀번호 처리
        String encodedPassword = (request.getPassword() != null && !request.getPassword().isEmpty())
                ? passwordEncoder.encode(request.getPassword()) : "";

        // 소셜 타입이 명확하지 않으면 예외 처리
        SocialType socialType = Optional.ofNullable(request.getSocialType())
                .orElseThrow(() -> new IllegalArgumentException("소셜 타입이 필요합니다."));
        Member member = new Member();
        member.setEmail(request.getEmail());
        member.setPassword(encodedPassword);
        member.setRole(Role.USER);
        member.setSocialType(socialType);
        member.setName(request.getName());
        member.setSocialId(request.getSocialId());

        memberRepository.save(member);
    }

    @Transactional
    public void disconnectSocialAccount(String email) {
        Member member = findByEmail(email);

        member.setSocialId(null);
        member.setSocialType(null);

        memberRepository.save(member);
    }
}
