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
import lombok.Getter;

@Data
@Entity
@Table(name="token_session")
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

	@Column(name = "is_logined", nullable = false,columnDefinition = "boolean default false")
	private Boolean isLogined;

	@Column(name="expire_at",nullable = false)
	private LocalDateTime expireAt;

	@Column(name = "refresh_token", nullable = false)
	private String refreshToken;

	@Column(name="is_active",columnDefinition = "boolean default false")
	private Boolean isActive;

	@Column(name = "first_name", nullable = false)
	private String firstName;

	@Column(name="userAgent",nullable = false)
	private String deviceName;

	@Column(name="identity",nullable = false)
	private String tokenIdentity;

	@Column(name="clientIp",nullable = false)
	private String clientIp;

	@Column(name="hostServer",nullable = false)
	private String hostServer;

	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
		this.expireAt=LocalDateTime.now().plusDays(1);
	}

	@PreUpdate
	public void preUpdate() {
		this.createdAt = LocalDateTime.now();
		this.expireAt=LocalDateTime.now().plusMinutes(30);
	}

}
