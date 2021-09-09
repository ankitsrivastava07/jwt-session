package jwtsession.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jwtsession.dao.entity.JwtSessionEntity;

import java.time.LocalDateTime;

public interface JwtSessionRepository extends JpaRepository<JwtSessionEntity, Long> {

	@Query(value = "select * from token_session where access_token = ?1 ", nativeQuery = true)
	JwtSessionEntity findByAccessToken(String accessToken);

	@Query(value="select * from token_session where identity =?1 and is_active = true and is_logined = true ",nativeQuery = true)
	JwtSessionEntity findByTokenIdentityNumber(String identityToken);

	@Query(value="select * from token_session where identity =?1 and is_active = false and is_logined = false",nativeQuery = true)
	JwtSessionEntity findByTokenIdentity(String identityToken);

	@Modifying(clearAutomatically = true)
	@Query(value="update token_session set access_token=?1,refresh_token=?2,identity=?3  where identity =?4 and is_active = true and is_logined = true",nativeQuery = true)
	Integer updateSessionToken(String accessToken,String refreshToken,String identity,String oldIdentityToken);

	@Modifying
	@Query(value = "delete from token_session where not identity = ?1 and user_id = ?2 ", nativeQuery = true)
	void removeAllTokensNot(String identity, Long user_id);

	@Modifying
	@Query(value = "delete from token_session where user_id = ?1 and created_at >= ?2 and created_at<=?3", nativeQuery = true)
	void removeAllTokensById(Long userId, LocalDateTime oneMonthBefore,LocalDateTime todayDate);

	@Modifying
	@Query(value = "delete from token_session where access_token = ?1", nativeQuery = true)
	Integer removeToken(String accessToken);
}