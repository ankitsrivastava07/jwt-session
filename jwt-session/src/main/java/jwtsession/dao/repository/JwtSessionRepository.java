package jwtsession.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jwtsession.dao.entity.JwtSessionEntity;

public interface JwtSessionRepository extends JpaRepository<JwtSessionEntity, Long> {

	@Query(value = "select * from token_session where access_token = ?1 ", nativeQuery = true)
	JwtSessionEntity findByAccessToken(String accessToken);

	@Query(value="select * from token_session where identity =?1 and is_active = true and is_logined = true ",nativeQuery = true)
	JwtSessionEntity findByTokenIdentityNumber(String identityToken);

	@Query(value="select * from token_session where identity =?1 and is_active = false and is_logined = false",nativeQuery = true)
	JwtSessionEntity findByTokenIdentity(String identityToken);

	@Modifying
	@Query(value = "delete from token_session where not access_token = ?1 and user_id = ?2 ", nativeQuery = true)
	void removeAllTokensNot(String accessToken, Long user_id);

	@Modifying
	@Query(value = "delete from token_session where user_id = ?1", nativeQuery = true)
	void removeAllTokensById(Long userId);

	@Modifying
	@Query(value = "delete from token_session where access_token = ?1", nativeQuery = true)
	Integer removeToken(String accessToken);
}