package jwtsession.controller;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jwtsession.service.JwtSessionService;

@RestController
@RequestMapping("/token-session")
public class JwtSessionController {

	@Autowired
	private JwtSessionService jwtSessionService;

	@PostMapping("/create-token")
	public ResponseEntity<?> createToken(@RequestBody CreateTokenRequest request,HttpServletRequest httpServletRequest) {
		TokenStatus tokenStatus = jwtSessionService.createToken(request,httpServletRequest);
		return new ResponseEntity<>(tokenStatus, HttpStatus.OK);
	}

	@PostMapping("/re-create-token")
	public ResponseEntity<?> createNewToken(@RequestBody CreateTokenRequest request,HttpServletRequest httpServletRequest) {
		TokenStatus tokenStatus = jwtSessionService.createToken(request,httpServletRequest);
		return new ResponseEntity<>(tokenStatus, HttpStatus.OK);
	}

	@PostMapping("/validate-token")
	public ResponseEntity<?> validateToken(@RequestHeader(name="AuthenticationToken",required = true) String accessToken, HttpServletRequest request) throws JsonProcessingException {
		TokenStatus tokenStatus = jwtSessionService.validateToken(accessToken.trim());
		return new ResponseEntity<>(tokenStatus, HttpStatus.OK);
	}

	@PostMapping("/invalidate-token")
	public ResponseEntity<?> invalidateToken(@RequestBody String token) {
		TokenStatus tokenStatus = jwtSessionService.invalidateToken(token);
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
