package jwtsession.exceptionHandle;

import java.security.SignatureException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandle {

	private Logger logger = LoggerFactory.getLogger(TokenStatus.class);

	@Autowired
	private HttpServletRequest path;

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

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<?> handleJsonParserException(HttpMessageNotReadableException ex) {
		ApiError apiError = new ApiError(new Date(), HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.name(),
				ex.getMessage(), path.getRequestURL().toString());
		return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
	}

}
