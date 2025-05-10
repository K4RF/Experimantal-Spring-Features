package domain.driven.dto.request;

import lombok.Getter;

@Getter
public class RegisterRequest {
    private String loginId;
    private String password;
    private String name;
}
