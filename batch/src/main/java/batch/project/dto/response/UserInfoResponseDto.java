package batch.project.dto.response;


import batch.project.entity.Member;
import batch.project.entity.enums.SocialType;
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