package domain.driven.dto.request;

import domain.driven.entity.enums.SocialType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SocialRegisterRequest {
    private String loginId;
    private String password;
    private String name;
    private String socialId;
    private SocialType socialType;
}