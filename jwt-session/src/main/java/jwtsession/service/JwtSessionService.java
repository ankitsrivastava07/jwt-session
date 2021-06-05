package jwtsession.service;

import java.util.Map;
import jwtsession.controller.TokenStatus;

public interface JwtSessionService {

	TokenStatus isValidToken(String jwt);

	TokenStatus generateToken(Long userId);

	TokenStatus removeToken(String token);

	TokenStatus removeAllTokens(Map<String, String> map);
}
