package jwtsession.exceptionHandle.exception;
import lombok.Data;

@Data
public class TokenStatus {
	private boolean status;
	private String message;
	private Boolean isAccessTokenNewCreated=Boolean.FALSE;
	private String token;
}
