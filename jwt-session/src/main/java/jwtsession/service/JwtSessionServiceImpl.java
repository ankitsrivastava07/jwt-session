package jwtsession.service;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jwtsession.controller.JwtSessionDto;
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
		JwtSessionEntity jwtSessionEntity = null;

		try {
			jwtAccessTokenUtil.validateToken(accessToken);
			jwtSessionEntity = jwtSessionDao.findByIdentityToken(dtoToEntityConvertor.getTokenIdentity(accessToken));
			if (jwtSessionEntity==null){
				tokenStatus.setStatus(Boolean.FALSE);
				if((jwtSessionEntity=jwtSessionDao.findByTokenIdentity(dtoToEntityConvertor.getTokenIdentity(accessToken)))!=null) {
					tokenStatus.setMessage(TokenStatusConstant.TOKEN_EXPIRED_TIME+" "+DateUtil.dateFormat(new Date()));
					tokenStatus.setIsAccessTokenNewCreated(Boolean.FALSE);
					tokenStatus.setCreatedAt(jwtSessionEntity.getCreatedAt());
					tokenStatus.setExpireAt(TokenStatusConstant.REFRESH_TOKEN_EXPIRED_DEFAULT_MESSAGE+jwtSessionEntity.getExpireAt());
				}else tokenStatus.setMessage(TokenStatusConstant.TOKEN_EXPIRED);
				return tokenStatus;
			}
			else if(jwtSessionEntity!=null){
				tokenStatus.setAccessToken(accessToken);
				tokenStatus.setStatus(Boolean.TRUE);
				//jwtSessionEntity.setTokenIdentity(jwtAccessTokenUtil.generateAccessToken(jwtSessionEntity.getAccessToken()));
				tokenStatus.setCreatedAt(jwtSessionEntity.getCreatedAt());
				tokenStatus.setExpireAt(TokenStatusConstant.REFRESH_TOKEN_EXPIRED_DEFAULT_MESSAGE+jwtSessionEntity.getAccessTokenExpireAt());
				tokenStatus.setFirstName(jwtSessionEntity.getFirstName());
				tokenStatus.setUserId(jwtSessionEntity.getUserId());
				tokenStatus.setAccessToken(jwtSessionEntity.getAccessToken());
				tokenStatus.setMessage(TokenStatusConstant.TOKEN_VERIFIED_MESSAGE);
				return tokenStatus;
			}

		}catch (ExpiredJwtException exception){
			jwtSessionEntity =jwtSessionDao.findByIdentityToken(dtoToEntityConvertor.getTokenIdentity(accessToken));
			if(jwtSessionEntity==null){
				tokenStatus.setStatus(Boolean.FALSE);
				if(jwtSessionDao.findByTokenIdentity(dtoToEntityConvertor.getTokenIdentity(accessToken))!=null)
					tokenStatus.setMessage(TokenStatusConstant.TOKEN_EXPIRED);
			      else tokenStatus.setMessage(TokenStatusConstant.TOKEN_EXPIRED);
				  return tokenStatus;
			}

			else if(jwtSessionEntity!=null && jwtSessionEntity.getExpireAt().before(DateUtil.todayDate())) {
				tokenStatus.setStatus(Boolean.FALSE);
				tokenStatus.setMessage(TokenStatusConstant.TOKEN_EXPIRED);
				tokenStatus.setCreatedAt(jwtSessionEntity.getCreatedAt());
				tokenStatus.setFirstName(jwtSessionEntity.getFirstName());
				tokenStatus.setUserId(jwtSessionEntity.getUserId());
				tokenStatus.setExpireAt(TokenStatusConstant.REFRESH_TOKEN_EXPIRED_DEFAULT_MESSAGE+jwtSessionEntity.getExpireAt());
				return tokenStatus;
			}
			else if(jwtSessionEntity!=null) {
				tokenStatus.setStatus(Boolean.TRUE);
				tokenStatus.setMessage(TokenStatusConstant.TOKEN_CREATED);
				jwtSessionEntity.setAccessToken(jwtAccessTokenUtil.generateAccessToken(jwtSessionEntity.getUserId()));
				jwtSessionEntity.setRefreshToken(jwtRefreshTokenUtil.generateRefreshToken(jwtSessionEntity.getUserId()));
				String identity=dtoToEntityConvertor.getTokenIdentity(jwtSessionEntity.getAccessToken());
				jwtSessionEntity.setTokenIdentity(identity);
				jwtSessionEntity=jwtSessionDao.saveToken(jwtSessionEntity);
				tokenStatus.setCreatedAt(jwtSessionEntity.getCreatedAt());
				tokenStatus.setIsAccessTokenNewCreated(Boolean.TRUE);
				tokenStatus.setStatus(Boolean.TRUE);
				tokenStatus.setFirstName(jwtSessionEntity.getFirstName());
				tokenStatus.setAccessToken(jwtSessionEntity.getAccessToken());
				tokenStatus.setExpireAt(TokenStatusConstant.REFRESH_TOKEN_EXPIRED_DEFAULT_MESSAGE+jwtSessionEntity.getExpireAt());
				return tokenStatus;
			}
		}
		tokenStatus.setExpireAt(TokenStatusConstant.REFRESH_TOKEN_EXPIRED_DEFAULT_MESSAGE+jwtSessionEntity.getExpireAt());
		return tokenStatus;
	}

	public TokenStatus defaultfallbackMethodGetFirstName(feign.RetryableException exception) {

		TokenStatus tokenStatus = new TokenStatus();
		tokenStatus.setStatus(TokenStatusConstant.FALSE);
		tokenStatus.setMessage(TokenStatusConstant.SERVER_DOWN_DEFAULT_MESSAGE);
		return tokenStatus;
	}

	@Override
	public TokenStatus createToken(CreateTokenRequest request,HttpServletRequest httpServletRequest) {

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
		tokenStatus.setExpireAt(TokenStatusConstant.REFRESH_TOKEN_EXPIRED_DEFAULT_MESSAGE+entity.getExpireAt());
		return tokenStatus;
	}

	@Override
	public TokenStatus invalidateToken(String token) {
		String tokenIdentityNumber = jwtAccessTokenUtil.getTokenIdentityNumber(token);
		JwtSessionEntity entity = jwtSessionDao.invalidateToken(tokenIdentityNumber);
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
	public TokenStatus removeAllTokens(JwtSessionDto dto) {

		TokenStatus tokenStatus = new TokenStatus();
		Long userId = dto.getUserId();
		if(dto.getUserId()==null && dto.getToken()==null){
			tokenStatus.setCreatedAt(DateUtil.todayDate());
			tokenStatus.setMessage(TokenStatusConstant.INVALID_REQUEST);
			return tokenStatus;
		}
		Integer delete=repository.removeAllTokensById(userId,DateUtil.addMonths(-1),new Date());
		tokenStatus.setStatus(TokenStatusConstant.FALSE);
		tokenStatus.setCreatedAt(DateUtil.todayDate());
		tokenStatus.setMessage(TokenStatusConstant.MESSAGE);

		return tokenStatus;
	}
}
