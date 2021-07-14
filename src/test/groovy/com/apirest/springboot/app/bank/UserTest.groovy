package com.apirest.springboot.app.bank
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired
import com.apirest.springboot.app.bank.model.entity.User
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = TestConfig.class)
class UserTest extends Specification {

    @Autowired
    private TestRestTemplate restTemplate

	
	def "/usuarios/{email} should return an element"(){
		when: "Get user"
		def entity = restTemplate.getForEntity("/usuarios/admin@gmail.com", User)
		
//		def entity = restTemplate.getForEntity("http://localhost:8080/usuarios/admin@gmail.com", User.class);

		then: "Verify the response is 200 and data equals as the first element"
		entity.statusCode    == HttpStatus.OK
		entity.body.id       != null
		entity.body.name     == "admin"
		entity.body.email 	 == "admin@gmail.com"
	}

    def "/usuarios should return a list of users"(){
        when: "Get list"
        def entity = restTemplate.getForEntity("/usuarios/", List)

        then: "status code OK and values are the expected"
        entity.statusCode    == HttpStatus.OK
        entity.body instanceof List
    }
	
	def "/usuario should save via post"(){
		given: "A new User is created"
		User user = new User()
		user.with {
			name: "Ivan Polichella"
		    email: "ivan_pb_bsso@hotmail.com"
		    password: "Ivancete123"
		}

		HttpHeaders requestHeaders = new HttpHeaders()
		requestHeaders.setContentType(MediaType.APPLICATION_JSON)
		HttpEntity<User> data = new HttpEntity<>(user, requestHeaders)

		when: "The user is sent via POST"
		def response = restTemplate.postForEntity("/usuario/", data, User)

		then: "status code CREATED and values are the expected"
		response.statusCode == HttpStatus.CREATED
		response.body.id > 0
	}
	
}