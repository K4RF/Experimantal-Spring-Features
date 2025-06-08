package monitoring.project.global.exception.code.Status;


import monitoring.project.global.exception.code.ApiCodeDto;
import monitoring.project.global.exception.code.ApiSuccessCodeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessStatus implements ApiSuccessCodeInterface {
    // For test
    _OK(HttpStatus.OK, "COMMON200", "성공입니다.")
    ;

    private final HttpStatus httpStatus;
    private final boolean isSuccess = true;
    private final String code;
    private final String message;

    @Override
    public ApiCodeDto getSuccessCode() {
        return ApiCodeDto.builder()
                .httpStatus(httpStatus)
                .isSuccess(isSuccess)
                .code(code)
                .message(message)
                .build();
    }
}