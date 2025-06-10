package redisCache.project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import redisCache.project.entity.Member;
import redisCache.project.service.MemberQueryService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {
    private final MemberQueryService memberQueryService;

    @GetMapping("/{email}")
    public Member getMember(@PathVariable String email) {
        return memberQueryService.getMemberByEmail(email);
    }

    @DeleteMapping("/{email}/cache")
    public void evictMemberCache(@PathVariable String email) {
        memberQueryService.evictMemberCache(email);
    }
}
