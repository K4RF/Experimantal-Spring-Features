package jwt.project.dto.response;


import jwt.project.entity.Member;
import jwt.project.entity.enums.SocialType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.management.relation.Role;

@Getter
@AllArgsConstructor
public class UserInfoResponseDto {
    private String loginId;
    private String name;
    private String role;
    private SocialType socialType;

    public static UserInfoResponseDto from(Member member) {
        return new UserInfoResponseDto(
                member.getLoginId(),
                member.getName(),
                member.getRole().name(),
                member.getSocialType()
        );
    }
}