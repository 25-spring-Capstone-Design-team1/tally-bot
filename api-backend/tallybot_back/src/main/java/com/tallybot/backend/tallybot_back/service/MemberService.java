package com.tallybot.backend.tallybot_back.service;

import com.tallybot.backend.tallybot_back.domain.Member;
import com.tallybot.backend.tallybot_back.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public void recreateMemberWithCustomId(Long oldId, Long customId) {
        // 기존 멤버 삭제
        Member oldMember = memberRepository.findById(oldId)
                .orElseThrow(() -> new RuntimeException("기존 멤버 없음"));
        memberRepository.delete(oldMember);

        // 새 멤버 생성 (ID 직접 지정)
        Member newMember = Member.builder()
                .memberId(customId) // 👉 직접 ID 설정
                .nickname(oldMember.getNickname())
                .userGroup(oldMember.getUserGroup())
                .build();

        memberRepository.save(newMember);
    }

}
