package jwtsession.dao;

import jwtsession.dao.entity.JwtSessionEntity;

public interface JwtSessionDao {

	JwtSessionEntity findByAccessToken(String accessToken);

	JwtSessionEntity findByIdentityToken(String identityToken);
	JwtSessionEntity findByTokenIdentity(String identityToken);
	JwtSessionEntity saveToken(JwtSessionEntity entity);

	JwtSessionEntity removeToken(String token);
	Integer updateSessionToken(JwtSessionEntity jwtSessionEntity);
	Boolean isTokenExist(String token);
}