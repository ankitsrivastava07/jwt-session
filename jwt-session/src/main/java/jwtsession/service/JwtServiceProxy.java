package jwtsession.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "users", url = "http://microservice-users.herokuapp.com")
public interface JwtServiceProxy {

	@PostMapping(value="/users/get-first-name")
	public ResponseEntity<String> getFirstName(@RequestBody Long userId);

}
