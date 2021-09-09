package jwtsession.controller;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jwtsession.service.JwtSessionService;

@RestController
@RequestMapping("/token-session")
public class JwtSessionController {

	@Autowired
	private JwtSessionService jwtSessionService;

	@PostMapping("/save-token")
	public ResponseEntity<?> generateToken(@RequestBody CreateTokenRequest request,HttpServletRequest httpServletRequest) {
		TokenStatus tokenStatus = jwtSessionService.generateToken(request,httpServletRequest);
		return new ResponseEntity<>(tokenStatus, HttpStatus.OK);
	}

	@PostMapping("/re-create-token")
	public ResponseEntity<?> reCreateToken(@RequestBody CreateTokenRequest request,HttpServletRequest httpServletRequest) {
		TokenStatus tokenStatus = jwtSessionService.generateToken(request,httpServletRequest);
		return new ResponseEntity<>(tokenStatus, HttpStatus.OK);
	}

	@PostMapping("/validate-token")
	public ResponseEntity<?> isValidToken(@RequestBody(required = true) String jwt, HttpServletRequest request) throws JsonProcessingException {
		TokenStatus tokenStatus = jwtSessionService.isValidToken(jwt.trim());
		return new ResponseEntity<>(tokenStatus, HttpStatus.OK);
	}

	@PostMapping("/invalidate-token")
	public ResponseEntity<?> invalidateToken(@RequestBody String token) {
		TokenStatus tokenStatus = jwtSessionService.removeToken(token);
		return new ResponseEntity<>(tokenStatus, HttpStatus.OK);
	}

	@PostMapping(value = "/invalidate-tokens")
	public ResponseEntity<?> invalidateTokens(@RequestBody JwtSessionDto dto, HttpServletRequest request) {
		TokenStatus tokenStatus = jwtSessionService.removeAllTokens(dto);
		return new ResponseEntity<>(tokenStatus, HttpStatus.OK);
	}

	@PostMapping(value = "/generate-new-token")
	public ResponseEntity<?> generateNewToken(@RequestBody String token) {
		TokenStatus tokenStatus = jwtSessionService.generateNewToken(token);
		return new ResponseEntity<>(tokenStatus, HttpStatus.OK);
	}

}
