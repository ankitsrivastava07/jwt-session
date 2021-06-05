package jwtsession.controller;

import java.util.Map;

import lombok.Data;

@Data
public class JwtSessionDto {

	private Map<String, String> token;
}
