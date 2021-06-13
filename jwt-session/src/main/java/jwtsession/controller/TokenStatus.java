package jwtsession.controller;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class TokenStatus {

	private boolean status;

	private String message;

	private String accessToken;

	private LocalDateTime createdAt;

	private String firstName;
}
