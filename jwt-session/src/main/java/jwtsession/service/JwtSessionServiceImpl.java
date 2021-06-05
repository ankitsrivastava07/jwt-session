package jwtsession.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;

import io.jsonwebtoken.ExpiredJwtException;
import jwtsession.constant.TokenStatusConstant;
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
	UserServiceProxy userServiceProxy;

	@Autowired
	private JwtAccessTokenUtil jwtAccessTokenUtil;

	@Autowired
	private JwtRefreshTokenUtil jwtRefreshTokenUtil;

	@Override
	public TokenStatus isValidToken(String accessToken) {

		TokenStatus tokenStatus = new TokenStatus();
		JwtSessionEntity jwtSessionToken = jwtSessionDao.findByAccessToken(accessToken);

		try {

			if (Objects.isNull(jwtSessionToken)) {

				tokenStatus.setStatus(TokenStatusConstant.FALSE);
				tokenStatus.setMessage("Your session has been expired.Please login again");
				tokenStatus.setAccessToken(accessToken);
				return tokenStatus;
			}
			jwtAccessTokenUtil.validateToken(accessToken);

		} catch (ExpiredJwtException exception) {

			if (jwtSessionToken != null) {

				jwtRefreshTokenUtil.validateToken(jwtSessionToken.getRefreshToken());

				accessToken = jwtAccessTokenUtil.generateAccessToken(jwtSessionToken.getUserId());

				String refreshToken = jwtRefreshTokenUtil.generateRefreshToken(jwtSessionToken.getUserId());

				jwtSessionToken.setAccessToken(accessToken);

				jwtSessionToken.setRefreshToken(refreshToken);

				repository.save(jwtSessionToken);
			}

			else {
				tokenStatus.setStatus(TokenStatusConstant.FALSE);
				tokenStatus.setMessage("Your session have been expired. Please login again");
				tokenStatus.setAccessToken(accessToken);
				return tokenStatus;
			}

		}

		tokenStatus.setStatus(TokenStatusConstant.TRUE);
		tokenStatus.setMessage(TokenStatusConstant.MESSAGE);
		tokenStatus.setAccessToken(accessToken);

		String firstName = getFirstName(jwtAccessTokenUtil.getUserId(accessToken));
		tokenStatus.setFirstName(firstName);

		return tokenStatus;

	}

	public TokenStatus defaultResponseFallbackMethod(String accessToken) {

		TokenStatus tokenStatus = new TokenStatus();
		System.out.println("defaultResponseFallbackMethod");
		tokenStatus.setStatus(TokenStatusConstant.FALSE);
		tokenStatus.setMessage("Sorry Server is currently down.Please try again later");
		tokenStatus.setAccessToken(accessToken);
		return tokenStatus;
	}

	@HystrixCommand(fallbackMethod = "defaultResponseFallbackMethod", commandProperties = {
			@HystrixProperty(name = "command.default.execution.timeout.enabled:", value = "true"),
			@HystrixProperty(name = "hystrix.command.default.execution.timeout.enabled", value = "true"),
			@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "1") })
	public String getFirstName(Long userId) {
		String firstName = userServiceProxy.getFirstName(userId).getBody();
		return firstName;
	}

	@Override
	public TokenStatus generateToken(Long userId) {

		TokenStatus tokenStatus = new TokenStatus();

		String accessToken = jwtAccessTokenUtil.generateAccessToken(userId);
		String refreshToken = jwtRefreshTokenUtil.generateRefreshToken(userId);

		JwtSessionEntity entity = new JwtSessionEntity();

		entity.setUserId(userId);
		entity.setAccessToken(accessToken);
		entity.setRefreshToken(refreshToken);
		jwtSessionDao.saveToken(entity);

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
	public TokenStatus removeAllTokens(Map<String, String> map) {

		Long user_id = null;
		TokenStatus tokenStatus = new TokenStatus();
		if (map != null && !map.isEmpty() && map.get("request").equals("change-password")) {
			String token = map.get("token");
			user_id = jwtAccessTokenUtil.getUserId(token);
			repository.removeAllTokensNot(token, user_id);
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
