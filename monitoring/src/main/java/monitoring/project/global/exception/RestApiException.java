package monitoring.project.global.exception;

import monitoring.project.global.exception.code.ApiCodeDto;
import monitoring.project.global.exception.code.ApiErrorCodeInterface;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RestApiException extends RuntimeException {

    private final ApiErrorCodeInterface errorCode; //추상화 시킨 인터페이스를 받아서 사용

    // 추상화 시킨 ErrorCode의 getErrorCode()를 사용하여 ErrorCode를 반환
    public ApiCodeDto getErrorCode() {
        return this.errorCode.getErrorCode();
    }
}