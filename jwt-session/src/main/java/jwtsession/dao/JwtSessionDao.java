package jwtsession.dao;

import jwtsession.dao.entity.JwtSessionEntity;

public interface JwtSessionDao {

	JwtSessionEntity findByAccessToken(String accessToken);

	JwtSessionEntity findByIdentityTokenIsActiveTrueAndLoginTrue(String identityToken);

	JwtSessionEntity findByTokenIdentity(String identityToken);
	JwtSessionEntity save(JwtSessionEntity entity);

	JwtSessionEntity invalidateToken(String token);
	Integer updateSessionToken(JwtSessionEntity jwtSessionEntity);
	Boolean isTokenExist(String token);
}