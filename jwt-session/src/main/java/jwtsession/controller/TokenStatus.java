package jwtsession.controller;

import java.time.LocalDateTime;

import jwtsession.constant.TokenStatusConstant;
import lombok.Data;

@Data
public class TokenStatus {

	private boolean status;
	private String message= TokenStatusConstant.TOKEN_EXPIRED;
	private String accessToken;
	private Long userId;
	private LocalDateTime createdAt;
	private Boolean isAccessTokenNewCreated=Boolean.FALSE;
	private LocalDateTime expireAt;
	private String firstName;
}
