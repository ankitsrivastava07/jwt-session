package jwtsession.exceptionHandle;

import lombok.Data;

@Data
public class TokenStatus {

	private boolean status;

	private String message;
	private Boolean isAccessTokenNewCreated=Boolean.FALSE;
	private String token;
}
