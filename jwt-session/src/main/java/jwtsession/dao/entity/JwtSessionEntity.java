package jwtsession.dao.entity;

import java.time.LocalDateTime;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import jwtsession.dateutil.DateUtil;
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
	private Date createdAt;

	@Column(name = "is_logined", nullable = false,columnDefinition = "boolean default false")
	private Boolean isLogined;

	@Column(name="refresh_token_expireat",nullable = false)
	private Date refreshTokenExpireAt;

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

	@Column(name="sign_out_at",nullable = false)
	private Date accessTokenExpireAt;

	@PrePersist
	public void prePersist() {
		this.createdAt = DateUtil.todayDate();
		this.refreshTokenExpireAt=DateUtil.addDays(1);
		this.accessTokenExpireAt=DateUtil.todayDate();
	}

	@PreUpdate
	public void preUpdate() {
		this.createdAt = DateUtil.todayDate();
		this.refreshTokenExpireAt= DateUtil.addDays(1);
	}

}
