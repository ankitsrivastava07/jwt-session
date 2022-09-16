package jwtsession.controller;

import java.util.Map;

import lombok.Data;

@Data
public class JwtSessionDto {

	private Long userId;
	private String password;
	private String token;
}
