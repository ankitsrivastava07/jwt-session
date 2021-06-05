package jwtsession.dao.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "token_session")
public class JwtSessionEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;
	@Column(name = "access_token", nullable = false)
	private String accessToken;
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "refresh_token", nullable = false)
	private String refreshToken;

	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
	}

	@PreUpdate
	public void preUpdate() {
		this.createdAt = LocalDateTime.now();
	}

}
