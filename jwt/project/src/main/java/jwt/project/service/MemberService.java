package jwt.project.service;

import jwt.project.dto.request.SocialRegisterRequest;
import jwt.project.entity.Member;
import jwt.project.entity.enums.Role;
import jwt.project.entity.enums.SocialType;
import jwt.project.repository.MemberRepository;
import jwt.project.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

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

    public String loginAndGetToken(String loginId, String password) {
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        return jwtUtil.generateToken(member.getLoginId(), member.getRole().name());
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

}
