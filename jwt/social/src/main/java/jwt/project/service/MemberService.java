package jwt.project.service;

import jwt.project.entity.Member;
import jwt.project.entity.enums.Role;
import jwt.project.repository.MemberRepository;
import jwt.project.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    public Member findByLoginId(String loginId) {
        return memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));
    }

    public String loginAndGetToken(String loginId, String password) {
        Member member = findByLoginId(loginId);
        if(!passwordEncoder.matches(password, member.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다");
        }

        return jwtUtil.generateToken(member.getLoginId(), member.getRole().name());
    }
}
