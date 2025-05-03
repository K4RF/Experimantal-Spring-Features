package domain.driven.service;

import domain.driven.entity.Member;
import domain.driven.repository.MemberRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryService {
    private final MemberRepositoryPort memberRepo;
    public Member me(String id){ return memberRepo.findByLoginId(id).orElseThrow(); }
}