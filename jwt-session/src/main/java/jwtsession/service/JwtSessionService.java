package jwtsession.service;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import jwtsession.controller.CreateTokenRequest;
import jwtsession.controller.JwtSessionDto;
import jwtsession.controller.TokenStatus;

import javax.servlet.http.HttpServletRequest;

public interface JwtSessionService {

	TokenStatus validateToken(String jwt) throws JsonProcessingException;

	TokenStatus createToken(CreateTokenRequest request, HttpServletRequest httpServletRequest);

	TokenStatus invalidateSession(String token);

	TokenStatus invalidateSessions(JwtSessionDto dto);

	TokenStatus refreshToken(String token);

}
