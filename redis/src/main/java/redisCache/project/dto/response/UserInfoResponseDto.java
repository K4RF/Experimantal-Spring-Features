package redisCache.project.dto.response;


import redisCache.project.entity.Member;
import redisCache.project.entity.enums.SocialType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInfoResponseDto {
    private String email;
    private String name;
    private String role;
    private SocialType socialType;

    public static UserInfoResponseDto from(Member member) {
        return new UserInfoResponseDto(
                member.getEmail(),
                member.getName(),
                member.getRole().name(),
                member.getSocialType()
        );
    }
}