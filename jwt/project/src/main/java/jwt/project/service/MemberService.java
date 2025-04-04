package jwt.project.service;

import jwt.project.dto.request.SocialRegisterRequest;
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

    public Map<String, String> loginAndGetToken(String loginId, String password) {
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        // ✅ Access Token과 Refresh Token 생성
        String accessToken = jwtUtil.generateToken(member.getLoginId(), member.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(member.getLoginId());

        // ✅ Refresh Token DB 저장 (이미 있으면 갱신)
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByLoginId(loginId);
        if (existingToken.isPresent()) {
            existingToken.get().setRefreshToken(refreshToken);
            refreshTokenRepository.save(existingToken.get());
        } else {
            refreshTokenRepository.save(new RefreshToken(null, loginId, refreshToken));
        }

        // ✅ Access Token과 Refresh Token Map으로 반환
        return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
    }


    public void registerSocialUser(SocialRegisterRequest request) {
        // 비밀번호 처리
        String encodedPassword = (request.getPassword() != null && !request.getPassword().isEmpty())
                ? passwordEncoder.encode(request.getPassword())
                : "";

        // 소셜 타입이 명확하지 않으면 예외 처리
        SocialType socialType = Optional.ofNullable(request.getSocialType())
                .orElseThrow(() -> new IllegalArgumentException("소셜 타입이 필요합니다."));

        Member member = new Member();
        member.setLoginId(request.getLoginId());
        member.setPassword(encodedPassword);
        member.setName(request.getName());
        member.setRole(Role.USER);
        member.setSocialId(request.getSocialId());
        member.setSocialType(socialType);

        memberRepository.save(member);
    }


    public Member findByLoginId(String loginId) {
        return memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));
    }
}
