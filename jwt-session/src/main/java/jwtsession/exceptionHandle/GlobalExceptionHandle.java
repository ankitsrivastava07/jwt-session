package jwtsession.exceptionHandle;

import java.util.Date;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.io.DecodingException;
import jwtsession.constant.TokenConstantResponse;
import jwtsession.dao.entity.JwtSessionEntity;
import jwtsession.dao.repository.JwtSessionRepository;
import jwtsession.exceptionHandle.exception.ApiError;
import jwtsession.exceptionHandle.exception.ApiErrorMissingAuthenticationToken;
import jwtsession.exceptionHandle.exception.TokenStatus;
import jwtsession.jwtutil.JwtAccessTokenUtil;
import jwtsession.service.JwtSessionService;
import jwtsession.service.JwtSessionServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandle {

	private Logger logger = LoggerFactory.getLogger(TokenStatus.class);
    @Autowired private JwtSessionRepository jwtSessionRepository;
    @Autowired private JwtAccessTokenUtil jwtAccessTokenUtil;
	@Autowired
	private HttpServletRequest httpServletRequest;

	@ExceptionHandler(ExpiredJwtException.class)
	public ResponseEntity<?> tokenException(ExpiredJwtException exception) {
		logger.info("Refresh Token has been expired "+exception.getMessage());
		TokenStatus tokenStatus = new TokenStatus();
		tokenStatus.setStatus(Boolean.FALSE);
		tokenStatus.setMessage("Your session has been expired.Please login again");
		return new ResponseEntity<>(tokenStatus, HttpStatus.OK);
	}

	@ExceptionHandler({JwtException.class})
	public ResponseEntity<?> tokenException(JwtException exception) {
		logger.info("Jwt Exception due to "+exception.getMessage());
		TokenStatus tokenStatus = new TokenStatus();
		tokenStatus.setStatus(Boolean.FALSE);
		tokenStatus.setMessage("Your session has been expired.Please login again");
		return new ResponseEntity<>(tokenStatus, HttpStatus.OK);
	}

	@ExceptionHandler({DecodingException.class})
	public ResponseEntity<?> decodingException(DecodingException exception) {
		logger.info("DecodingException due to "+exception.getMessage());
		TokenStatus tokenStatus = new TokenStatus();
		tokenStatus.setStatus(Boolean.FALSE);
		tokenStatus.setMessage("Your session has been expired.Please login again");
		return new ResponseEntity<>(tokenStatus, HttpStatus.OK);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<?> handleJsonParserException(HttpMessageNotReadableException ex) {
		ApiError apiError = new ApiError(new Date(), HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.name(),
				TokenConstantResponse.METHOD_NOT_READABLE_EXCEPTION_DEFAULT_MESSAGE, httpServletRequest.getRequestURI().toString());
		return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(MissingRequestHeaderException.class)
	public ResponseEntity<?> missingRequestHeaderException(MissingRequestHeaderException exception) {
		String authentication=httpServletRequest.getHeader("Authentication");
		String browser=httpServletRequest.getHeader("browser");
		String token= Objects.isNull(browser)?authentication : authentication;
		String identity = jwtAccessTokenUtil.getTokenIdentityNumber(token);
		JwtSessionEntity jwtSessionEntity=jwtSessionRepository.findByTokenIdentity(identity);
		jwtSessionEntity.setIsActive(Boolean.FALSE);
		jwtSessionEntity.setIsLogined(Boolean.FALSE);
		jwtSessionEntity=jwtSessionRepository.save(jwtSessionEntity);
		ApiErrorMissingAuthenticationToken apiError = new ApiErrorMissingAuthenticationToken(new Date(), HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.name(),
				TokenConstantResponse.HEADER_TOKEN_MISSING+" ("+exception.getHeaderName()+") "+"token in header", httpServletRequest.getRequestURI().toString());
		return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
	}
}
