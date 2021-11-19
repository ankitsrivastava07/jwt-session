package jwtsession.service;

import java.util.*;
import jwtsession.controller.JwtSessionDto;
import jwtsession.convertor.DtoToEntityConvertor;
import jwtsession.dateutil.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.jsonwebtoken.ExpiredJwtException;
import jwtsession.constant.TokenConstantResponse;
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
	public TokenStatus validateToken(String accessToken) {
		TokenStatus tokenStatus = new TokenStatus();
		JwtSessionEntity jwtSessionEntity = null;

		try {
			jwtAccessTokenUtil.validateToken(accessToken);
			jwtSessionEntity = jwtSessionDao.findByIdentityTokenIsActiveTrueAndLoginTrue(dtoToEntityConvertor.getTokenIdentity(accessToken));
			if (jwtSessionEntity==null){
				tokenStatus.setStatus(Boolean.FALSE);
				if((jwtSessionEntity=jwtSessionDao.findByTokenIdentity(dtoToEntityConvertor.getTokenIdentity(accessToken)))!=null) {
					tokenStatus.setMessage(TokenConstantResponse.TOKEN_EXPIRED_TIME+" "+DateUtil.dateFormat(jwtSessionEntity.getAccessTokenExpireAt())+"(GMT +5:30)");
					tokenStatus.setIsAccessTokenNewCreated(Boolean.FALSE);
					tokenStatus.setCreatedAt(jwtSessionEntity.getCreatedAt());
					tokenStatus.setExpireAt(TokenConstantResponse.REFRESH_TOKEN_EXPIRED_DEFAULT_MESSAGE+jwtSessionEntity.getRefreshTokenExpireAt());
				}else tokenStatus.setMessage(TokenConstantResponse.TOKEN_EXPIRED);
				return tokenStatus;
			}
			else if(jwtSessionEntity!=null){
				tokenStatus.setAccessToken(accessToken);
				tokenStatus.setStatus(Boolean.TRUE);
				tokenStatus.setCreatedAt(jwtSessionEntity.getCreatedAt());
				tokenStatus.setExpireAt(TokenConstantResponse.REFRESH_TOKEN_EXPIRED_DEFAULT_MESSAGE+jwtSessionEntity.getRefreshTokenExpireAt());
				tokenStatus.setFirstName(jwtSessionEntity.getFirstName());
				tokenStatus.setUserId(jwtSessionEntity.getUserId());
				tokenStatus.setAccessToken(jwtSessionEntity.getAccessToken());
				tokenStatus.setBrowser(jwtSessionEntity.getBrowser());
				tokenStatus.setMessage(TokenConstantResponse.TOKEN_VERIFIED_MESSAGE);
				return tokenStatus;
			}
		}catch (ExpiredJwtException exception){
			jwtSessionEntity =jwtSessionDao.findByIdentityTokenIsActiveTrueAndLoginTrue(dtoToEntityConvertor.getTokenIdentity(accessToken));
			if(jwtSessionEntity==null){
				tokenStatus.setStatus(Boolean.FALSE);
				tokenStatus.setMessage(TokenConstantResponse.TOKEN_EXPIRED);
				return tokenStatus;
			}
			else if(jwtSessionEntity!=null && jwtSessionEntity.getRefreshTokenExpireAt().before(DateUtil.todayDate())) {
				tokenStatus.setStatus(Boolean.FALSE);
				tokenStatus.setMessage(TokenConstantResponse.TOKEN_EXPIRED);
				tokenStatus.setCreatedAt(jwtSessionEntity.getCreatedAt());
				tokenStatus.setFirstName(jwtSessionEntity.getFirstName());
				tokenStatus.setUserId(jwtSessionEntity.getUserId());
				tokenStatus.setBrowser(jwtSessionEntity.getBrowser());
				tokenStatus.setExpireAt(TokenConstantResponse.REFRESH_TOKEN_EXPIRED_DEFAULT_MESSAGE+jwtSessionEntity.getRefreshTokenExpireAt());
				return tokenStatus;
			}
			else if(jwtSessionEntity!=null) {
				tokenStatus.setStatus(Boolean.TRUE);
				tokenStatus.setMessage(TokenConstantResponse.TOKEN_CREATED);
				jwtSessionEntity.setAccessToken(jwtAccessTokenUtil.createAccessToken(jwtSessionEntity.getUserId()));
				jwtSessionEntity.setRefreshToken(jwtRefreshTokenUtil.generateRefreshToken(jwtSessionEntity.getUserId()));
				String identity=dtoToEntityConvertor.getTokenIdentity(jwtSessionEntity.getAccessToken());
				jwtSessionEntity.setTokenIdentity(identity);
				jwtSessionEntity=jwtSessionDao.save(jwtSessionEntity);
				tokenStatus.setCreatedAt(jwtSessionEntity.getCreatedAt());
				tokenStatus.setIsAccessTokenNewCreated(Boolean.TRUE);
				tokenStatus.setStatus(Boolean.TRUE);
				tokenStatus.setCreatedAt(new Date());
				tokenStatus.setAccessTokenNew(Boolean.TRUE);
				tokenStatus.setFirstName(jwtSessionEntity.getFirstName());
				tokenStatus.setAccessToken(jwtSessionEntity.getAccessToken());
				tokenStatus.setExpireAt(TokenConstantResponse.REFRESH_TOKEN_EXPIRED_DEFAULT_MESSAGE+jwtSessionEntity.getRefreshTokenExpireAt());
				tokenStatus.setHttpStatus(HttpStatus.OK.value());
				tokenStatus.setBrowser(jwtSessionEntity.getBrowser());
				return tokenStatus;
			}
		}
		tokenStatus.setExpireAt(TokenConstantResponse.REFRESH_TOKEN_EXPIRED_DEFAULT_MESSAGE+jwtSessionEntity.getRefreshTokenExpireAt());
		return tokenStatus;
	}

	public TokenStatus defaultfallbackMethodGetFirstName(feign.RetryableException exception) {

		TokenStatus tokenStatus = new TokenStatus();
		tokenStatus.setStatus(TokenConstantResponse.FALSE);
		tokenStatus.setMessage(TokenConstantResponse.SERVER_DOWN_DEFAULT_MESSAGE);
		return tokenStatus;
	}

	@Override
	public TokenStatus createToken(CreateTokenRequest request,HttpServletRequest httpServletRequest) {
		TokenStatus tokenStatus = new TokenStatus();
		JwtSessionEntity entity = dtoToEntityConvertor.createTokenRequestDtoToJwtSessionEntityConversion(request,httpServletRequest);
		entity = jwtSessionDao.save(entity);
		String accessToken=entity.getAccessToken();
		tokenStatus.setStatus(TokenConstantResponse.TRUE);
		tokenStatus.setMessage(TokenConstantResponse.MESSAGE);
		tokenStatus.setUserId(entity.getUserId());
		tokenStatus.setAccessToken(accessToken);
		tokenStatus.setFirstName(entity.getFirstName());
		tokenStatus.setCreatedAt(entity.getCreatedAt());
		tokenStatus.setHttpStatus(HttpStatus.OK.value());
		tokenStatus.setBrowser(entity.getBrowser());
		tokenStatus.setExpireAt(TokenConstantResponse.REFRESH_TOKEN_EXPIRED_DEFAULT_MESSAGE+entity.getRefreshTokenExpireAt());
		return tokenStatus;
	}

	@Override
	public TokenStatus invalidateSession(String token) {
		String tokenIdentityNumber = jwtAccessTokenUtil.getTokenIdentityNumber(token);
		JwtSessionEntity entity = jwtSessionDao.invalidateToken(tokenIdentityNumber);
		TokenStatus tokenStatus = new TokenStatus();
		if (entity != null) {
			tokenStatus.setStatus(TokenConstantResponse.TRUE);
			tokenStatus.setMessage(TokenConstantResponse.LOGOUT);
		}
		tokenStatus.setStatus(TokenConstantResponse.TRUE);
		tokenStatus.setMessage(TokenConstantResponse.LOGOUT);
		return tokenStatus;
	}

	@Transactional
	@Override
	public TokenStatus refreshToken(String token) {
		String identity = jwtAccessTokenUtil.getTokenIdentityNumber(token);
		JwtSessionEntity jwtSessionEntity = jwtSessionDao.findByIdentityTokenIsActiveTrueAndLoginTrue(identity);
		TokenStatus tokenStatus = new TokenStatus();
		if (jwtSessionEntity != null) {

			if(jwtSessionEntity.getRefreshTokenExpireAt().before(DateUtil.todayDate()))
				return null;

			String accessToken = jwtAccessTokenUtil.createAccessToken(jwtSessionEntity.getUserId());
			String refreshToken = jwtRefreshTokenUtil.generateRefreshToken(jwtSessionEntity.getUserId());
			jwtSessionEntity.setAccessToken(accessToken);
			jwtSessionEntity.setRefreshToken(refreshToken);
			jwtSessionEntity=jwtSessionDao.save(jwtSessionEntity);
			tokenStatus.setStatus(TokenConstantResponse.TRUE);
			tokenStatus.setBrowser(jwtSessionEntity.getBrowser());
			tokenStatus.setMessage(TokenConstantResponse.MESSAGE);
			tokenStatus.setCreatedAt(jwtSessionEntity.getCreatedAt());
			tokenStatus.setFirstName(jwtSessionEntity.getFirstName());
			tokenStatus.setUserId(jwtSessionEntity.getUserId());
			tokenStatus.setHttpStatus(HttpStatus.OK.value());
			tokenStatus.setAccessToken(accessToken);
			return tokenStatus;
		}else{
			tokenStatus.setStatus(Boolean.FALSE);
			tokenStatus.setRefreshTokenExpired(Boolean.TRUE);
			return tokenStatus;
		}
	}

	@Transactional
	@Override
	public TokenStatus invalidateSessions(JwtSessionDto dto) {
		TokenStatus tokenStatus = new TokenStatus();
		Long userId = dto.getUserId();
		if(dto.getUserId()==null && dto.getToken()==null){
			tokenStatus.setCreatedAt(DateUtil.todayDate());
			tokenStatus.setMessage(TokenConstantResponse.INVALID_REQUEST);
			return tokenStatus;
		}
		jwtSessionDao.invalidateSessions(userId,true);
		tokenStatus.setStatus(TokenConstantResponse.FALSE);
		tokenStatus.setCreatedAt(DateUtil.todayDate());
		tokenStatus.setMessage(TokenConstantResponse.MESSAGE);
		return tokenStatus;
	}
}
