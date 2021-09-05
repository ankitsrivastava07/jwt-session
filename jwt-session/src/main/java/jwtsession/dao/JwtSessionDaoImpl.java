package jwtsession.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import jwtsession.controller.TokenStatus;
import jwtsession.dao.entity.JwtSessionEntity;
import jwtsession.dao.repository.JwtSessionRepository;

@Repository
public class JwtSessionDaoImpl implements JwtSessionDao {

	@Autowired
	JwtSessionRepository repository;

	@Override
	public JwtSessionEntity findByAccessToken(String refreshToken) {
		return repository.findByAccessToken(refreshToken);
	}

	@Override
	public JwtSessionEntity findByIdentityToken(String tokenIdentityNumber) {
		return repository.findByTokenIdentityNumber(tokenIdentityNumber);
	}

	@Override
	public JwtSessionEntity findByTokenIdentity(String tokenIdentityNumber) {
		return repository.findByTokenIdentity(tokenIdentityNumber);
	}

	@Override
	public JwtSessionEntity saveToken(JwtSessionEntity entity) {
		return repository.save(entity);
	}

	@Transactional
	@Override
	public JwtSessionEntity removeToken(String tokenIdentityNumber) {
		JwtSessionEntity entity = repository.findByTokenIdentityNumber(tokenIdentityNumber);
		if(entity!=null){
		entity.setIsActive(Boolean.FALSE);
		entity.setIsLogined(Boolean.FALSE);
			repository.save(entity);
		}
		return null;
	}

	@Override
	public Boolean isTokenExist(String token) {
		return repository.findByAccessToken(token) == null ? false : true;
	}
}
