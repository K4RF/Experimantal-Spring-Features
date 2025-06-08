package monitoring.project.dto.request;

import monitoring.project.entity.enums.SocialType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SocialRegisterRequest {
    private String email;
    private String password;
    private String name;
    private String socialId;
    private SocialType socialType;
}