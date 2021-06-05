package jwtsession.dao;

import jwtsession.dao.entity.JwtSessionEntity;

public interface JwtSessionDao {

	JwtSessionEntity findByAccessToken(String jwt);

	JwtSessionEntity saveToken(JwtSessionEntity entity);

	Integer removeToken(String token);

	Boolean isTokenExist(String token);
}