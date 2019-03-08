package codesquad.web;

import codesquad.domain.UserRepository;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import support.test.AcceptanceTest;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class LoginAccpetanceTest {
	private static final Logger log = LoggerFactory.getLogger(UserAcceptanceTest.class);


	@Autowired
	private TestRestTemplate template;

	@Autowired
	private UserRepository userRepository;

	@Test
	public void loginForm() throws Exception{
		ResponseEntity<String> response = template.getForEntity("/login/form", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		log.debug("body : {}", response.getBody());
	}

	@Test
	public void login_sucess() throws Exception{
		HttpHeaders header = new HttpHeaders();
		header.setAccept(Arrays.asList(MediaType.TEXT_HTML));
		header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
		String userId = "javajigi";
		String password = "test";
		params.add("userId",userId);
		params.add("password", password);

		HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<MultiValueMap<String, Object>>(params , header);
		ResponseEntity<String> response = template.postForEntity("/login", request, String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
		assertThat(userRepository.findByUserId(userId).isPresent()).isTrue();
		assertThat(response.getHeaders().getLocation().getPath()).isEqualTo("/");
	}
	@Test
	public void login_fail() throws Exception{
		HttpHeaders header = new HttpHeaders();
		header.setAccept(Arrays.asList(MediaType.TEXT_HTML));
		header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
		String userId = "javajigi1";
		String password = "test";
		params.add("userId",userId);
		params.add("password", password);

		HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<MultiValueMap<String, Object>>(params , header);
		ResponseEntity<String> response = template.postForEntity("/login", request, String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(userRepository.findByUserId(userId).isPresent()).isFalse();
		assertThat(response.getHeaders().getLocation().getPath()).isEqualTo("/login/form");

	}

}
