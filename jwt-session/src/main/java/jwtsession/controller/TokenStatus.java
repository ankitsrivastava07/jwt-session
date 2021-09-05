package jwtsession.controller;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class TokenStatus {

	private boolean status;
	private String message;
	private String accessToken;
	private Long userId;
	private LocalDateTime createdAt;
	private Boolean isAccessTokenNewCreated=Boolean.FALSE;
	private LocalDateTime expireAt;
	private String firstName;
}
