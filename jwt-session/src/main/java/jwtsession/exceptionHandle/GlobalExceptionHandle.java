package jwtsession.exceptionHandle;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import io.jsonwebtoken.ClaimJwtException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;

@ControllerAdvice
public class GlobalExceptionHandle {

	@Autowired
	private HttpServletRequest path;

	@ExceptionHandler(ExpiredJwtException.class)
	public ResponseEntity<?> tokenExpiredException(ExpiredJwtException exception) {

		TokenStatus tokenStatus = new TokenStatus();

		tokenStatus.setStatus(Boolean.FALSE);
		tokenStatus.setMessage("Your session has been expired.Please login again");

		return new ResponseEntity<>(tokenStatus, HttpStatus.OK);
	}

	@ExceptionHandler(MalformedJwtException.class)
	public ResponseEntity<?> tokenException(MalformedJwtException exception) {

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
