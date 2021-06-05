package jwtsession.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "users", url = "localhost:8081")
public interface UserServiceProxy {

	@PostMapping("/users/get-first-name")
	public ResponseEntity<String> getFirstName(@RequestBody Long userId);

}
