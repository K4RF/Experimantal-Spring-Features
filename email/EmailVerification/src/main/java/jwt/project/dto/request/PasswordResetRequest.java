package jwt.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

// 실제 비밀번호 재설정
@Getter
@Setter
public class PasswordResetRequest {
    @NotBlank
    private String newPassword;

    @NotBlank
    private String confirmPassword;
}