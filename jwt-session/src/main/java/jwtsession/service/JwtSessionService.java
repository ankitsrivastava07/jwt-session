package jwtsession.service;

import java.util.Map;

import jwtsession.controller.CreateTokenRequest;
import jwtsession.controller.TokenStatus;

public interface JwtSessionService {

	TokenStatus isValidToken(String jwt);

	TokenStatus generateToken(CreateTokenRequest request);

	TokenStatus removeToken(String token);

	TokenStatus removeAllTokens(Map<String, String> map);

	TokenStatus generateNewToken(String token);

}
