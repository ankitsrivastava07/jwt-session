package jwtsession.controller;

import lombok.Data;

@Data
public class CreateTokenRequest {
	private String firstName;
	private Long userId;
}
