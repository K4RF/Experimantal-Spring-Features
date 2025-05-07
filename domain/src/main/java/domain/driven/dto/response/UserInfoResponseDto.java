package domain.driven.dto.response;



import domain.driven.entity.Member;
import domain.driven.entity.enums.SocialType;
import lombok.AllArgsConstructor;
import lombok.Getter;

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
                member.getSocial().getSocialType()
        );
    }
}