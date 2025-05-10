package domain.driven.service;

import domain.driven.dto.request.SocialRegisterRequest;
import domain.driven.entity.Member;
import domain.driven.entity.SocialInfo;
import domain.driven.entity.enums.Role;

import domain.driven.repository.MemberRepositoryPort;
import domain.driven.repository.RefreshTokenRedisRepo;

import domain.driven.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service @RequiredArgsConstructor @Transactional
public class MemberCommandService {

    private final MemberRepositoryPort memberRepo;
    private final RefreshTokenRedisRepo rtRepo;
    private final PasswordEncoder pe;
    private final JwtUtils jwt;

    /* 회원가입 */
    public void registerUser(String id,String pw,String name, Role role){
        Member m = new Member(null,id,pw,name,role,null);
        m.encodePw(pe);
        memberRepo.save(m);
    }

    /* 로그인 + 토큰 */
    public Map<String,String> login(String id,String pw){
        Member m = memberRepo.findByLoginId(id)
                .orElseThrow(()->new RuntimeException("미가입"));
        if(!pe.matches(pw,m.getPassword()))
            throw new RuntimeException("비밀번호 불일치");

        String at = jwt.generateToken(id, m.getRole().name());
        String rt = jwt.refreshToken(id);
        rtRepo.save(id, rt);
        return Map.of("accessToken",at,"refreshToken",rt);
    }

    /* 로그아웃 */
    public void logout(String id){ rtRepo.delete(id); }

    /* 소셜 회원가입 */
    public void registerSocial(SocialRegisterRequest dto){
        SocialInfo si = new SocialInfo(dto.getSocialType(), dto.getSocialId());
        Member m = new Member(null,dto.getLoginId(),
                dto.getPassword()!=null? pe.encode(dto.getPassword()) : "",
                dto.getName(), Role.USER, si);
        memberRepo.save(m);
    }

    /* 소셜 연결 해제 */
    public void disconnectSocial(String id){
        Member m = memberRepo.findByLoginId(id).orElseThrow();
        m.disconnectSocial();
    }
}