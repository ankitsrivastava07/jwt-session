package jwtsession.exceptionHandle.exception;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ApiError {

	private Date timestamp;
	private Integer status;
	private String error;
	private String message;
	private String path;

	public ApiError(Date timestamp, Integer status, String error, String message, String path) {
		this.timestamp = timestamp;
		this.status = status;
		this.error = error;
		this.message = message;
		this.path = path;
	}
}