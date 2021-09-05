package jwtsession.service;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import jwtsession.controller.CreateTokenRequest;
import jwtsession.controller.TokenStatus;

import javax.servlet.http.HttpServletRequest;

public interface JwtSessionService {

	TokenStatus isValidToken(String jwt) throws JsonProcessingException;

	TokenStatus generateToken(CreateTokenRequest request, HttpServletRequest httpServletRequest);

	TokenStatus removeToken(String token);

	TokenStatus removeAllTokens(Map<String, String> map);

	TokenStatus generateNewToken(String token);

}
