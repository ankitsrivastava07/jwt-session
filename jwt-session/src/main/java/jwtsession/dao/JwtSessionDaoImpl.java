package jwtsession.dao;

import jwtsession.convertor.DtoToEntityConvertor;
import jwtsession.dateutil.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import jwtsession.controller.TokenStatus;
import jwtsession.dao.entity.JwtSessionEntity;
import jwtsession.dao.repository.JwtSessionRepository;

import java.util.List;

@Repository
public class JwtSessionDaoImpl implements JwtSessionDao {

	@Autowired
	JwtSessionRepository repository;
	@Override
	public JwtSessionEntity findByAccessToken(String refreshToken) {
		return repository.findByAccessToken(refreshToken);
	}

	@Override
	public JwtSessionEntity findByIdentityTokenStatusTrueAndLoginTrue(String tokenIdentityNumber) {
		JwtSessionEntity jwt=repository.findByTokenIdentity(tokenIdentityNumber);
		return repository.findByIdentityTokenStatusTrueAndLoginTrue(tokenIdentityNumber);
	}

	@Override
	public JwtSessionEntity findByTokenIdentity(String tokenIdentityNumber) {
		return repository.findByTokenIdentity(tokenIdentityNumber);
	}

	@Override
	@Transactional
	public Integer updateSessionToken(JwtSessionEntity jwtSessionEntity) {
		DtoToEntityConvertor dtoToEntityConvertor = new DtoToEntityConvertor();
		String newIdentity=dtoToEntityConvertor.getTokenIdentity(jwtSessionEntity.getAccessToken(),"identity");
		return repository.updateSessionToken(jwtSessionEntity.getAccessToken(),jwtSessionEntity.getRefreshToken(),newIdentity,jwtSessionEntity.getTokenIdentity());
	}

	@Override
	public JwtSessionEntity save(JwtSessionEntity entity) {
		return repository.save(entity);
	}

	@Transactional
	@Override
	public JwtSessionEntity invalidateToken(String tokenIdentityNumber) {
		JwtSessionEntity entity = repository.findByIdentityTokenStatusTrueAndLoginTrue(tokenIdentityNumber);
		if(entity!=null){
		entity.setStatus(Boolean.FALSE);
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

	public void invalidateSessions(Long userId,Boolean active){
       List<JwtSessionEntity> entities=repository.findByUserIdAndStatusTrue(userId);
	   entities.stream().forEach(entity-> {entity.setStatus(Boolean.FALSE);
	   entity.setIsLogined(Boolean.FALSE);
	   });
	   repository.saveAll(entities);
	}
}
