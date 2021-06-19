package jwtsession.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

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

@Service
public class JwtSessionServiceImpl implements JwtSessionService {

	@Autowired
	private JwtSessionDao jwtSessionDao;

	@Autowired
	JwtSessionRepository repository;

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
		JwtSessionEntity jwtSessionTokenEntity = jwtSessionDao.findByAccessToken(accessToken);

		try {

			if (Objects.isNull(jwtSessionTokenEntity)) {

				tokenStatus.setStatus(TokenStatusConstant.FALSE);
				tokenStatus.setMessage(TokenStatusConstant.TOKEN_EXPIRED);
				return tokenStatus;
			}
			jwtAccessTokenUtil.validateToken(accessToken);

		} catch (ExpiredJwtException exception) {

			if (jwtSessionTokenEntity != null) {

				jwtRefreshTokenUtil.validateToken(jwtSessionTokenEntity.getRefreshToken());

				accessToken = jwtAccessTokenUtil.generateAccessToken(jwtSessionTokenEntity.getUserId());

				String refreshToken = jwtRefreshTokenUtil.generateRefreshToken(jwtSessionTokenEntity.getUserId());

				jwtSessionTokenEntity.setAccessToken(accessToken);

				jwtSessionTokenEntity.setRefreshToken(refreshToken);

				jwtSessionTokenEntity = repository.save(jwtSessionTokenEntity);
			}

			else {
				tokenStatus.setStatus(TokenStatusConstant.FALSE);
				tokenStatus.setMessage(TokenStatusConstant.TOKEN_EXPIRED);
				tokenStatus.setAccessToken(accessToken);
				return tokenStatus;
			}
		}

		tokenStatus.setStatus(TokenStatusConstant.TRUE);
		tokenStatus.setUserId(jwtSessionTokenEntity.getUserId());
		tokenStatus.setMessage(TokenStatusConstant.MESSAGE);
		tokenStatus.setAccessToken(jwtSessionTokenEntity.getAccessToken());
		tokenStatus.setCreatedAt(jwtSessionTokenEntity.getCreatedAt());

		// String firstName =
		// jwtServiceProxy.getFirstName(jwtAccessTokenUtil.getUserId(accessToken)).getBody();
		tokenStatus.setFirstName(jwtSessionTokenEntity.getFirstName());
		return tokenStatus;
	}

	/*
	 * @CircuitBreaker(name = "users", fallbackMethod =
	 * "defaultfallbackMethodGetFirstName") public String getFirstName(Long userId)
	 * {
	 * 
	 * String firstName =
	 * 
	 * return firstName; }
	 */

	public TokenStatus defaultfallbackMethodGetFirstName(feign.RetryableException exception) {

		TokenStatus tokenStatus = new TokenStatus();
		tokenStatus.setStatus(TokenStatusConstant.FALSE);
		tokenStatus.setMessage(TokenStatusConstant.SERVER_DOWN_DEFAULT_MESSAGE);
		return tokenStatus;
	}

	@Override
	public TokenStatus generateToken(CreateTokenRequest request) {

		TokenStatus tokenStatus = new TokenStatus();

		String accessToken = jwtAccessTokenUtil.generateAccessToken(request.getUserId());
		String refreshToken = jwtRefreshTokenUtil.generateRefreshToken(request.getUserId());

		JwtSessionEntity entity = new JwtSessionEntity();
		entity.setFirstName(request.getFirstName());
		entity.setUserId(request.getUserId());
		entity.setAccessToken(accessToken);
		entity.setRefreshToken(refreshToken);
		accessToken = jwtSessionDao.saveToken(entity).getAccessToken();
		tokenStatus.setStatus(TokenStatusConstant.TRUE);
		tokenStatus.setMessage(TokenStatusConstant.MESSAGE);
		tokenStatus.setAccessToken(accessToken);

		return tokenStatus;
	}

	@Override
	public TokenStatus removeToken(String token) {

		Integer count = jwtSessionDao.removeToken(token);
		TokenStatus tokenStatus = new TokenStatus();
		if (count != null) {

			tokenStatus.setStatus(TokenStatusConstant.FALSE);
			tokenStatus.setMessage(TokenStatusConstant.MESSAGE);
		}

		return tokenStatus;
	}

	@Transactional
	@Override
	public TokenStatus generateNewToken(String token) {

		JwtSessionEntity jwtSessionEntity = jwtSessionDao.findByAccessToken(token);
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
