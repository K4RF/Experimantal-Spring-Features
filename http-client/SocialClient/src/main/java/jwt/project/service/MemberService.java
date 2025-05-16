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

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService; // ✅ Redis 서비스 추가


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

    public  Map<String, String> loginAndGetToken(String loginId, String password) {
        Member member = findByLoginId(loginId);
        if(!passwordEncoder.matches(password, member.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다");
        }

        // AccessToken과 RefreshToken 생성
        String accessToken = jwtUtil.generateToken(member.getLoginId(), member.getRole().name());
        String refreshToken = jwtUtil.refreshToken(member.getLoginId());

        /* RefreshTOekn DB 저장
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByLoginId(loginId);
        if(existingToken.isPresent()) {
            existingToken.get().setRefreshToken(refreshToken);
            refreshTokenRepository.save(existingToken.get());
        }else{
            refreshTokenRepository.save(new RefreshToken(null, loginId, refreshToken));
        }
         */

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
