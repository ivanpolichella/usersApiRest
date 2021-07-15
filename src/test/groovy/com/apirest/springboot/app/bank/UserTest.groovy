package com.apirest.springboot.app.bank
import org.json.JSONArray
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value;

import com.apirest.springboot.app.bank.model.entity.User
import com.apirest.springboot.app.bank.security.models.AuthenticationRequest
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = TestConfig.class)
class UserTest extends Specification {
	
	@Value('${local.server.port}')
	private int port;
	
	private String jwtToken;
	
	private HttpHeaders headers;
	
	def setup() {
		this.headers = new HttpHeaders();
		headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		TestRestTemplate restTemplate = new TestRestTemplate();
		//Authenticate User Admin and get JWT
		AuthenticationRequest user = new AuthenticationRequest("admin@gmail.com","admin");
		HttpEntity<String> authenticationEntity = new HttpEntity<String>(new ObjectMapper().writeValueAsString(user),headers);
		ResponseEntity<String> authenticationResponse = restTemplate.exchange("http://localhost:8080/authenticate",
				HttpMethod.POST, authenticationEntity, String.class);
		this.jwtToken = authenticationResponse.getBody().toString().split("\"")[3];
	}
	
	def "/authenticate debe retornar un jwt token"(){
		when: "Get jwt token"
		def entity = this.jwtToken
		then: "Validar que el status de la respuestsa sea OK y que haya un token jwt disponible"
		entity != null
	}

	
	def "/usuarios/{email} debe retornar un usuario (busqueda por mail)"(){
		when: "Get user"
		TestRestTemplate restTemplate = new TestRestTemplate();
		String authorizationHeader = "Bearer " + this.jwtToken;
		headers.add("Authorization", authorizationHeader);
		HttpEntity<String> authenticationEntity = new HttpEntity<String>("",headers);
		//Uso el JWT para el request
		ResponseEntity<User> responseEntity = restTemplate.exchange("http://localhost:8080/usuarios/admin@gmail.com",
				HttpMethod.GET, authenticationEntity, User.class);

		def entity = responseEntity
		then: "Validar que el status de la respuestsa sea OK y que los datos coincidan con el usuario pedido"
		entity.statusCode    == HttpStatus.OK
		entity.body.id       != null
		entity.body.name     == "admin"
		entity.body.email 	 == "admin@gmail.com"
	}
	
	
	def "/usuario debe insertar un usuario via POST"(){
		given: "Un nuevo usuario es creado"
		User user = new User()
		user.setEmail("ivan_pb_bsso@hotmail.com")
		user.setName("Ivan Polichella")
		user.setPassword("Ivancete123")

		TestRestTemplate restTemplate = new TestRestTemplate();
		String authorizationHeader = "Bearer " + this.jwtToken;
		headers.add("Authorization", authorizationHeader);
		HttpEntity<String> requestToPost = new HttpEntity<String>(user,headers);
		//Uso el JWT para el request
		ResponseEntity<User> responseEntity = restTemplate.exchange("http://localhost:8080/usuario/",
				HttpMethod.POST, requestToPost, User.class);

		when: "The user is sent via POST"
		def response = responseEntity

		then: "Se debe retornar status OK y los datos de respuesta"
		response.statusCode == HttpStatus.OK
		response.body.id != null
	}
	
	def "/usuario no debe insertar un usuario cuando el email ya se encuentra registrado"(){
		given: "Una excepcion"
		User user = new User()
		user.setEmail("ivan_pb_bsso@hotmail.com")
		user.setName("Ivan Polichella")
		user.setPassword("Ivancete123")

		TestRestTemplate restTemplate = new TestRestTemplate();
		String authorizationHeader = "Bearer " + this.jwtToken;
		headers.add("Authorization", authorizationHeader);
		HttpEntity<String> requestToPost = new HttpEntity<String>(user,headers);
		//Uso el JWT para el request
		ResponseEntity<User> responseEntity = restTemplate.exchange("http://localhost:8080/usuario/",
			HttpMethod.POST, requestToPost, User.class);

		when: "Se intenta registrar un usuario con un mail en uso"
		  

		then: "Se valida status code NOT ACCEPTABLE"
		responseEntity.statusCode == HttpStatus.NOT_ACCEPTABLE
	}

    def "/usuarios should return a list of users"(){
        when: "Get list"
        TestRestTemplate restTemplate = new TestRestTemplate();
		String authorizationHeader = "Bearer " + this.jwtToken;
		headers.add("Authorization", authorizationHeader);
		HttpEntity<String> authenticationEntity = new HttpEntity<String>("",headers);
		//Uso el JWT para el request
		ResponseEntity<List<User>> responseEntity = restTemplate.exchange("http://localhost:8080/usuarios/",
				HttpMethod.GET, authenticationEntity, List.class);

		def entity = responseEntity
        then: "status code OK and values are the expected"
        entity.statusCode    == HttpStatus.OK
        entity.body instanceof ArrayList
    }
	
}