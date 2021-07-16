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

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserTest extends Specification {
	
	private String jwtToken;
	
	private HttpHeaders headers;
	
	private TestRestTemplate restTemplate;
	
	private TestRestTemplate testRestTemplate;
	
	//Funcion que se ejecuta para cada request de prueba.
	def setup() {
		
		//Se agrega header por defecto
		this.headers = new HttpHeaders();
		this.headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
		this.headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		this.restTemplate = new TestRestTemplate();
		
		//Se autentica el usuario admin y se obtiene el jwt
		AuthenticationRequest user = new AuthenticationRequest("admin@gmail.com","admin");
		HttpEntity<String> authenticationEntity = new HttpEntity<String>(new ObjectMapper().writeValueAsString(user),headers);
		ResponseEntity<String> authenticationResponse = this.restTemplate.exchange("http://localhost:8080/authenticate",
				HttpMethod.POST, authenticationEntity, String.class);
		this.jwtToken = authenticationResponse.getBody().toString().split("\"")[3];
		
		//se agrega en el header el token para que cada request pueda ejecutarse correctamente 
		String authorizationHeader = "Bearer " + this.jwtToken;
		this.headers.add("Authorization", authorizationHeader);
	}
	
	def "/authenticate debe retornar un jwt token"(){
		when: "Get jwt token"
		def entity = this.jwtToken
		then: "Validar que el status de la respuestsa sea OK y que haya un token jwt disponible"
		entity != null
	}

	
	def "/usuarios/{email} debe retornar un usuario (busqueda por mail)"(){
		when: "Get user"
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

		HttpEntity<String> requestToPost = new HttpEntity<String>(user,this.headers);
		//Uso el JWT para el request
		ResponseEntity<User> responseEntity = restTemplate.exchange("http://localhost:8080/usuario/",
				HttpMethod.POST, requestToPost, User.class);

		when: "The user is sent via POST"
		def response = responseEntity

		then: "Se debe retornar status OK y los datos de respuesta"
		response.statusCode == HttpStatus.OK
		response.body.id != null
		response.body.createdDate != null
		response.body.lastModified != null
		response.body.enabled != null
	}
	
	def "/usuario no debe insertar un usuario cuando el email ya se encuentra registrado"(){
		given: "Codigo de respuesta 406"
		User user = new User()
		user.setEmail("ivan_pb_bsso@hotmail.com")
		user.setName("Ivan Polichella")
		user.setPassword("Ivancete123")

		HttpEntity<String> requestToPost = new HttpEntity<String>(user,this.headers);
		//Uso el JWT para el request
		ResponseEntity<User> responseEntity = restTemplate.exchange("http://localhost:8080/usuario/",
			HttpMethod.POST, requestToPost, User.class);

		when: "Se intenta registrar un usuario con un mail en uso"
		  

		then: "Se valida status code NOT ACCEPTABLE"
		responseEntity.statusCode == HttpStatus.NOT_ACCEPTABLE
	}
	
	def "/usuario no debe insertar un usuario cuando la password no cumple con los requisitos"(){
		given: "Codigo de respuesta 406"
		User user = new User()
		user.setEmail("ivancete.iapb@gmail.com")
		user.setName("Ivancete Brieba")
		user.setPassword("ivancete123") //Falta una mayuscula

		HttpEntity<String> requestToPost = new HttpEntity<String>(user,this.headers);
		//Uso el JWT para el request
		ResponseEntity<User> responseEntity = restTemplate.exchange("http://localhost:8080/usuario/",
			HttpMethod.POST, requestToPost, User.class);

		when: "Se intenta registrar un usuario con una contraseña invalida"
		  
		then: "Se valida status code NOT ACCEPTABLE"
		responseEntity.statusCode == HttpStatus.NOT_ACCEPTABLE
	}

    def "/usuarios debe retornar una LISTA de usuarios"(){

		HttpEntity<String> authenticationEntity = new HttpEntity<String>("",this.headers);
		//Uso el JWT para el request
		ResponseEntity<List<User>> responseEntity = restTemplate.exchange("http://localhost:8080/usuarios/",
				HttpMethod.GET, authenticationEntity, List.class);

		def entity = responseEntity
		when: "Consulto la totalidad de usuarios"
		
        then: "Se debe retornar status ok y se valida que el dato sea una lista, la cantidad"
        entity.statusCode    == HttpStatus.OK
        entity.body instanceof ArrayList
    }
	
}