package batch.project.service;

import batch.project.entity.Member;
import batch.project.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;
    @Override
    public UserDetails loadUserByUsername(String email) {
        Member m = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("없음"));
        return User.builder()
                .username(m.getEmail())
                .password(m.getPassword())          // bcrypt 저장된 값
                .roles(m.getRole().name())          // ROLE_ 접두어 자동
                .build();
    }
}