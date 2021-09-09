package jwtsession.service;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import jwtsession.controller.CreateTokenRequest;
import jwtsession.controller.JwtSessionDto;
import jwtsession.controller.TokenStatus;

import javax.servlet.http.HttpServletRequest;

public interface JwtSessionService {

	TokenStatus isValidToken(String jwt) throws JsonProcessingException;

	TokenStatus generateToken(CreateTokenRequest request, HttpServletRequest httpServletRequest);

	TokenStatus removeToken(String token);

	TokenStatus removeAllTokens(JwtSessionDto dto);

	TokenStatus generateNewToken(String token);

}
