package jwt.project.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

// 비밀번호 재설정 요청(이메일 입력)
@Getter
@Setter
public class PasswordResetEmailRequest {
    @Email
    @NotBlank
    private String email;
}