package jwtsession.controller;

import java.util.Date;

import jwtsession.constant.TokenConstantResponse;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class TokenStatus {

	private boolean status=Boolean.FALSE;
	private String message= TokenConstantResponse.TOKEN_EXPIRED;
	private String accessToken;
	private Long userId;
	private Date createdAt;
	private Boolean isAccessTokenNewCreated=Boolean.FALSE;
	private String expireAt;
	private String firstName;
	private Integer httpStatus= HttpStatus.UNAUTHORIZED.value();
	private String browser;
	private Boolean refreshTokenExpired=Boolean.FALSE;
	private Boolean accessTokenNew=Boolean.FALSE;
}
