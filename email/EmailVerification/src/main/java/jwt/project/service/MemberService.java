package jwt.project.service;

import jakarta.transaction.Transactional;
import jwt.project.dto.SocialUserInfo;
import jwt.project.dto.request.SocialRegisterRequest;
import jwt.project.dto.response.LoginResponse;
import jwt.project.entity.Member;
import jwt.project.entity.RefreshToken;
import jwt.project.entity.enums.Role;
import jwt.project.entity.enums.SocialType;
import jwt.project.repository.MemberRepository;
import jwt.project.repository.RefreshTokenRepository;
import jwt.project.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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


    public void registerUser(String loginId, String password, String name) {
        Member member = new Member();
        member.setLoginId(loginId);
        member.setPassword(passwordEncoder.encode(password));
        member.setName(name);
        member.setRole(Role.USER);

        memberRepository.save(member);
    }

    public void registerAdmin(String loginId, String password, String name) {
        Member member = new Member();
        member.setLoginId(loginId);
        member.setPassword(passwordEncoder.encode(password));
        member.setName(name);
        member.setRole(Role.ADMIN);

        memberRepository.save(member);
    }

    public Member findByLoginId(String loginId) {
        return memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));
    }

    public Map<String, String> loginAndGetToken(String loginId, String password) {
        Member member = findByLoginId(loginId);

        // 소셜 회원이 아니라면 이메일 인증 체크
        if (member.getSocialType() == null) {
            if (!member.isEmailVerified()) {
                // 인증 토큰 생성 및 저장
                String token = UUID.randomUUID().toString();
                member.setEmailVerificationToken(token);
                member.setEmailVerificationExpiry(LocalDateTime.now().plusHours(1));
                memberRepository.save(member);

                // 인증 메일 발송
                String siteUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
                emailService.sendVerificationMail(member.getLoginId(), token, siteUrl);

                throw new IllegalStateException("이메일 인증이 필요합니다. 이메일을 확인해주세요.");
            }
        }

        if(!passwordEncoder.matches(password, member.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다");
        }

        // AccessToken과 RefreshToken 생성
        String accessToken = jwtUtil.generateToken(member.getLoginId(), member.getRole().name());
        String refreshToken = jwtUtil.refreshToken(member.getLoginId());

        // ✅ Redis에 저장 (자동 TTL)
        refreshTokenService.save(loginId, refreshToken);

        return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
    }

    public LoginResponse socialLoginOrRegister(SocialUserInfo userInfo, SocialType socialType) {
        // socialId(구글: sub, 카카오: id, 네이버: id 등)로 회원 조회
        Optional<Member> memberOpt = memberRepository.findBySocialIdAndSocialType(userInfo.getSocialId(), socialType);
        Member member;
        if (memberOpt.isEmpty()) {
            // 회원가입
            member = new Member();
            member.setLoginId(userInfo.getEmail());
            member.setName(userInfo.getName());
            member.setRole(Role.USER);
            member.setSocialType(socialType);
            member.setSocialId(userInfo.getSocialId());
            memberRepository.save(member);
        } else {
            member = memberOpt.get();
        }
        // JWT 토큰 발급 및 Redis에 refreshToken 저장
        String accessToken = jwtUtil.generateToken(member.getLoginId(), member.getRole().name());
        String refreshToken = jwtUtil.refreshToken(member.getLoginId());
        refreshTokenService.save(member.getLoginId(), refreshToken);

        return new LoginResponse(accessToken, refreshToken, member.getLoginId());
    }

    public void registerSocialUser(SocialRegisterRequest request) {
        // 비밀번호 처리
        String encodedPassword = (request.getPassword() != null && !request.getPassword().isEmpty())
                ? passwordEncoder.encode(request.getPassword()) : "";

        // 소셜 타입이 명확하지 않으면 예외 처리
        SocialType socialType = Optional.ofNullable(request.getSocialType())
                .orElseThrow(() -> new IllegalArgumentException("소셜 타입이 필요합니다."));
        Member member = new Member();
        member.setLoginId(request.getLoginId());
        member.setPassword(encodedPassword);
        member.setRole(Role.USER);
        member.setSocialType(socialType);
        member.setName(request.getName());
        member.setSocialId(request.getSocialId());

        memberRepository.save(member);
    }

    @Transactional
    public void disconnectSocialAccount(String loginId) {
        Member member = findByLoginId(loginId);

        member.setSocialId(null);
        member.setSocialType(null);

        memberRepository.save(member);
    }
}
