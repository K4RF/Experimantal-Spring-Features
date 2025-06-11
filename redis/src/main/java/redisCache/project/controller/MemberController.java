package redisCache.project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import redisCache.project.dto.response.CacheResponse;
import redisCache.project.entity.Member;
import redisCache.project.service.MemberQueryService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {
    private final MemberQueryService memberQueryService;

    @GetMapping("/{email}")
    public CacheResponse getMember(@PathVariable String email) {
        Member member = memberQueryService.getMemberByEmail(email);
        return new CacheResponse(
                member.getEmail(),
                member.getName(),
                member.getRole().name(),
                member.isEmailVerified()
        );
    }

    @DeleteMapping("/{email}/cache")
    public void evictMemberCache(@PathVariable String email) {
        memberQueryService.evictMemberCache(email);
    }
}
