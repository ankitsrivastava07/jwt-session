package jwtsession.dao;

import jwtsession.convertor.DtoToEntityConvertor;
import jwtsession.dateutil.DateUtil;
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
	public JwtSessionEntity findByIdentityTokenIsActiveTrueAndLoginTrue(String tokenIdentityNumber) {
		return repository.findByIdentityToken(tokenIdentityNumber);
	}

	@Override
	public JwtSessionEntity findByTokenIdentity(String tokenIdentityNumber) {
		return repository.findByTokenIdentity(tokenIdentityNumber);
	}

	@Override
	@Transactional
	public Integer updateSessionToken(JwtSessionEntity jwtSessionEntity) {
		DtoToEntityConvertor dtoToEntityConvertor = new DtoToEntityConvertor();
		String newIdentity=dtoToEntityConvertor.getTokenIdentity(jwtSessionEntity.getAccessToken());
		return repository.updateSessionToken(jwtSessionEntity.getAccessToken(),jwtSessionEntity.getRefreshToken(),newIdentity,jwtSessionEntity.getTokenIdentity());
	}

	@Override
	public JwtSessionEntity saveToken(JwtSessionEntity entity) {
		return repository.save(entity);
	}

	@Transactional
	@Override
	public JwtSessionEntity invalidateToken(String tokenIdentityNumber) {
		JwtSessionEntity entity = repository.findByIdentityToken(tokenIdentityNumber);
		if(entity!=null){
		entity.setIsActive(Boolean.FALSE);
		entity.setAccessTokenExpireAt(DateUtil.todayDate());
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
