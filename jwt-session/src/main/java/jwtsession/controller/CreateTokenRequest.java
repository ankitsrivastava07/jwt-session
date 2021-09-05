package jwtsession.controller;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTokenRequest {
	private String firstName;
	private Long userId;
	private String token;
}
