package jwtsession.service;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jwtsession.convertor.DtoToEntityConvertor;
import jwtsession.dateutil.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.jsonwebtoken.ExpiredJwtException;
import jwtsession.constant.TokenStatusConstant;
import jwtsession.controller.CreateTokenRequest;
import jwtsession.controller.TokenStatus;
import jwtsession.dao.JwtSessionDao;
import jwtsession.dao.entity.JwtSessionEntity;
import jwtsession.dao.repository.JwtSessionRepository;
import jwtsession.jwtutil.JwtAccessTokenUtil;
import jwtsession.jwtutil.JwtRefreshTokenUtil;

import javax.servlet.http.HttpServletRequest;

@Service
public class JwtSessionServiceImpl implements JwtSessionService {

	@Autowired
	private JwtSessionDao jwtSessionDao;

	@Autowired
	JwtSessionRepository repository;

	@Autowired
	DtoToEntityConvertor dtoToEntityConvertor;
	@Autowired
	JwtServiceProxy jwtServiceProxy;

	@Autowired
	private JwtAccessTokenUtil jwtAccessTokenUtil;

	@Autowired
	private JwtRefreshTokenUtil jwtRefreshTokenUtil;

	@Override
	@Transactional
	@CircuitBreaker(name = "users", fallbackMethod = "defaultfallbackMethodGetFirstName")
	public TokenStatus isValidToken(String accessToken) {
		TokenStatus tokenStatus = new TokenStatus();
		JwtSessionEntity jwtSessionTokenEntity = null;

		try {
			jwtAccessTokenUtil.validateToken(accessToken);
			JwtSessionEntity jwtSessionEntity = jwtSessionDao.findByIdentityToken(dtoToEntityConvertor.getTokenIdentity(accessToken));
			if (jwtSessionEntity==null){
				tokenStatus.setStatus(Boolean.FALSE);
				if((jwtSessionEntity=jwtSessionDao.findByTokenIdentity(dtoToEntityConvertor.getTokenIdentity(accessToken)))!=null) {
					tokenStatus.setMessage(TokenStatusConstant.TOKEN_EXPIRED_TIME+" "+(jwtSessionEntity.getExpireAt()));
				}else tokenStatus.setMessage(TokenStatusConstant.TOKEN_EXPIRED);
				return tokenStatus;
			}
			else if(jwtSessionEntity!=null){
				tokenStatus.setAccessToken(accessToken);
				tokenStatus.setStatus(Boolean.TRUE);
				tokenStatus.setCreatedAt(jwtSessionEntity.getCreatedAt());
				tokenStatus.setExpireAt(jwtSessionEntity.getExpireAt());
				tokenStatus.setFirstName(jwtSessionEntity.getFirstName());
				tokenStatus.setUserId(jwtSessionEntity.getUserId());
				tokenStatus.setMessage(TokenStatusConstant.TOKEN_VERIFIED_MESSAGE);
				return tokenStatus;
			}

		}catch (ExpiredJwtException exception){
			JwtSessionEntity jwtSessionEntity =jwtSessionDao.findByIdentityToken(dtoToEntityConvertor.getTokenIdentity(accessToken));
			if(jwtSessionEntity==null){
				tokenStatus.setStatus(Boolean.FALSE);
				if(jwtSessionDao.findByTokenIdentity(dtoToEntityConvertor.getTokenIdentity(accessToken))!=null)
					tokenStatus.setMessage(TokenStatusConstant.TOKEN_EXPIRED);
			      else tokenStatus.setMessage(TokenStatusConstant.TOKEN_EXPIRED);

				  //tokenStatus.setAccessToken(jwtSessionTokenEntity.getAccessToken());
				//  tokenStatus.set(jwtSessionTokenEntity.getAccessToken());
			return tokenStatus;
			}
			else if(jwtSessionEntity!=null && !jwtRefreshTokenUtil.getExpirationDateFromToken(jwtSessionEntity.getRefreshToken()).before(new Date())) {
				tokenStatus.setStatus(Boolean.TRUE);
				tokenStatus.setMessage(TokenStatusConstant.TOKEN_CREATED);
				jwtSessionEntity.setAccessToken(jwtAccessTokenUtil.generateAccessToken(jwtSessionEntity.getUserId()));
				jwtSessionEntity.setRefreshToken(jwtRefreshTokenUtil.generateRefreshToken(jwtSessionEntity.getUserId()));
				jwtSessionEntity = jwtSessionDao.saveToken(jwtSessionEntity);
				tokenStatus.setCreatedAt(jwtSessionEntity.getCreatedAt());
				tokenStatus.setIsAccessTokenNewCreated(Boolean.TRUE);
				tokenStatus.setStatus(Boolean.TRUE);
				tokenStatus.setAccessToken(jwtSessionEntity.getAccessToken());
				tokenStatus.setExpireAt(jwtSessionEntity.getExpireAt());
				return tokenStatus;
			}
		}
		return tokenStatus;
	}

	public TokenStatus defaultfallbackMethodGetFirstName(feign.RetryableException exception) {

		TokenStatus tokenStatus = new TokenStatus();
		tokenStatus.setStatus(TokenStatusConstant.FALSE);
		tokenStatus.setMessage(TokenStatusConstant.SERVER_DOWN_DEFAULT_MESSAGE);
		return tokenStatus;
	}

	@Override
	public TokenStatus generateToken(CreateTokenRequest request,HttpServletRequest httpServletRequest) {

		TokenStatus tokenStatus = new TokenStatus();
		JwtSessionEntity entity = dtoToEntityConvertor.createTokenRequestDtoToJwtSessionEntityConversion(request,httpServletRequest);
		entity = jwtSessionDao.saveToken(entity);
		String accessToken=entity.getAccessToken();
		tokenStatus.setStatus(TokenStatusConstant.TRUE);
		tokenStatus.setMessage(TokenStatusConstant.MESSAGE);
		tokenStatus.setUserId(entity.getUserId());
		tokenStatus.setAccessToken(accessToken);
		tokenStatus.setFirstName(entity.getFirstName());
		tokenStatus.setCreatedAt(entity.getCreatedAt());
		tokenStatus.setExpireAt(entity.getExpireAt());
		return tokenStatus;
	}

	@Override
	public TokenStatus removeToken(String token) {
		String tokenIdentityNumber = jwtAccessTokenUtil.getTokenIdentityNumber(token);
		JwtSessionEntity entity = jwtSessionDao.removeToken(tokenIdentityNumber);
		TokenStatus tokenStatus = new TokenStatus();
		if (entity != null) {
			tokenStatus.setStatus(TokenStatusConstant.TRUE);
			tokenStatus.setMessage(TokenStatusConstant.LOGOUT);
		}
		tokenStatus.setStatus(TokenStatusConstant.TRUE);
		tokenStatus.setMessage(TokenStatusConstant.LOGOUT);

		return tokenStatus;
	}

	@Transactional
	@Override
	public TokenStatus generateNewToken(String token) {
		String identity = jwtAccessTokenUtil.getTokenIdentityNumber(token);
		JwtSessionEntity jwtSessionEntity = jwtSessionDao.findByIdentityToken(identity);
		TokenStatus tokenStatus = new TokenStatus();
		if (jwtSessionEntity != null) {
			String accessToken = jwtAccessTokenUtil.generateAccessToken(jwtSessionEntity.getUserId());
			String refreshToken = jwtRefreshTokenUtil.generateRefreshToken(jwtSessionEntity.getUserId());
			jwtSessionEntity.setAccessToken(accessToken);
			jwtSessionEntity.setRefreshToken(refreshToken);
			tokenStatus.setStatus(TokenStatusConstant.TRUE);
			tokenStatus.setMessage(TokenStatusConstant.MESSAGE);
			tokenStatus.setAccessToken(accessToken);
		}

		return tokenStatus;
	}

	@Transactional
	@Override
	public TokenStatus removeAllTokens(Map<String, String> map) {

		Long user_id = null;
		TokenStatus tokenStatus = new TokenStatus();
		if (map != null && !map.isEmpty() && map.get("request").equals("change-password")) {
			String accessToken = map.get("token");
			user_id = jwtAccessTokenUtil.getUserId(accessToken);
			repository.removeAllTokensNot(accessToken, user_id);
		}

		else if (!map.isEmpty()) {
			user_id = jwtAccessTokenUtil.getUserId(map.get("token"));
			repository.removeAllTokensById(user_id);
		}
		tokenStatus.setStatus(TokenStatusConstant.FALSE);
		tokenStatus.setCreatedAt(LocalDateTime.now());
		tokenStatus.setMessage(TokenStatusConstant.MESSAGE);

		return tokenStatus;
	}
}
