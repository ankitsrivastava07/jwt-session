package jwtsession.exceptionHandle.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class ApiErrorMissingAuthenticationToken {
    private Date timestamp;
    private Integer httpStatus;
    private String error;
    private String message;
    private String path;
}
