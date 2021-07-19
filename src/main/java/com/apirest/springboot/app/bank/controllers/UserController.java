package com.apirest.springboot.app.bank.controllers;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.apirest.springboot.app.bank.exception.MessageException;
import com.apirest.springboot.app.bank.model.dto.UserDto;
import com.apirest.springboot.app.bank.model.entity.User;
import com.apirest.springboot.app.bank.model.service.UserServiceInterface;
import com.apirest.springboot.app.bank.security.MyUserDetailsService;
import com.apirest.springboot.app.bank.security.models.AuthenticationRequest;
import com.apirest.springboot.app.bank.security.models.AuthenticationResponse;
import com.apirest.springboot.app.bank.security.util.JwtUtil;
import com.apirest.springboot.app.bank.security.util.Mapper;

@RestController
public class UserController {
	private static final Logger logger = LoggerFactory.getLogger(UserController.class);
	@Autowired
	private UserServiceInterface userService;
	
	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtUtil jwtTokenUtil;

	@Autowired
	private MyUserDetailsService userDetailsService;
	
	@RequestMapping(value = "/authenticate", method = RequestMethod.POST)
	public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {

		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
			);
		}
		catch (BadCredentialsException e) {
			logger.info("No se pudo autenticar al usuario:  "+ authenticationRequest.getUsername());
			throw new Exception("Incorrect username or password", e);
		}


		final UserDetails userDetails = userDetailsService
				.loadUserByUsername(authenticationRequest.getUsername());

		final String jwt = jwtTokenUtil.generateToken(userDetails);
		logger.info("El usuario "+ authenticationRequest.getUsername() + " se logeo correctamente, genero el token " + jwt );
		//Se actualiza la fecha de ultimo login
		actualizarLoginUsuario(authenticationRequest.getUsername());
		return ResponseEntity.ok(new AuthenticationResponse(jwt));
	}
	
	private void actualizarLoginUsuario(String username) {
		User user = this.userService.findForEmail(username);
		user.setLastLogin(new Date());
		this.userService.modify(user);
		logger.info("Usuario: "+ username + "LastLogin: " + user.getLastLogin());
	}

	@GetMapping("/usuarios")
	@ResponseStatus(HttpStatus.OK)
	public List<UserDto> obtenerUsuarios() {
		List<User> usuarios = userService.listAll();
		logger.info("Se listarán todos los usuarios");
		//Uso 2 caracteristicas de Java 8 Collectors y :: 
		List<UserDto> res = usuarios.stream()
		          .map(Mapper::convertToDto).collect(Collectors.toList());
		return res;
	}
	
	@GetMapping("/usuarios/{email}")
	@ResponseStatus(HttpStatus.OK)
	public UserDto obtenerUsuarioPorEmail(@PathVariable String email) {
		UserDto resultado = Mapper.convertToDto(userService.findForEmail(email));
		logger.info("Se realizó una busqueda por mail entre todos los usuarios");
		return resultado;	
	}
	
	@PostMapping("/usuario")
	@ResponseStatus(HttpStatus.CREATED)
	public UserDto addUser(HttpServletResponse response, @RequestBody UserDto userDto){
		if(!this.isValidEmail(userDto.getEmail())) {
			
			throw new MessageException("Mail Invalido!" );
		}
		
		if(this.mailInUse(userDto.getEmail())) {
			throw new MessageException("El correo ya registrado");
		}
		
		if(this.invalidPassword(userDto.getPassword()))
			throw new MessageException("La clave no cumple con los requisitos de seguridad (Una Mayúscula, letras minúsculas, y dos números)");
		
		userDto.setCreatedDate(new Date());
		userDto.setLastModified(new Date());
		userDto.setLastLogin(new Date());
		userDto.setEnabled((long) 1);
		User user = Mapper.convertToEntity(userDto);
		User result = this.userService.save(user);
		logger.info("Se agregó el usuario" + user.getEmail());
		return Mapper.convertToDto(result);
	}
	
	private boolean invalidPassword(String password) {
		String pattern = "(?=.*[0-9]{2})(?=.*[a-z])(?=.*[A-Z]).{4,}";
		return (!password.matches(pattern));
	}

	private boolean mailInUse(String email) {
		return ( this.userService.findForEmail(email) != null );
	}

	private boolean isValidEmail(String email) {
		   String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
		   return email.matches(regex);
	}
	
	@PutMapping("/usuario")
	@ResponseStatus(HttpStatus.CREATED)
	public UserDto modifyUser(@RequestBody UserDto userDto) {
		User userToMod = userService.findById(userDto.getId());
		if (userToMod != null) {
			userToMod = checkChangesToModify(userToMod,userDto);
			userToMod.setLastModified(new Date());
			UserDto result = Mapper.convertToDto(this.userService.modify(userToMod));
			logger.info("Se modificó el usuario" + userDto.getEmail());
			return result;
		}else
			throw new MessageException("No existe el usuario enviado en el body, recuerde que la busqueda se realiza por ID");
	}
	
	@DeleteMapping("/usuario")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteUser(@RequestBody UserDto userDto) {
		User userToDel = userService.findById(userDto.getId());
		if (userToDel != null) {
			this.userService.delete(Mapper.convertToEntity(userDto));
			logger.info("Se eliminó el usuario" + userDto.getEmail());
		}else
			throw new MessageException("No existe el usuario que se desea eliminar, recuerde que la eliminacion se realiza por ID como campo en el body");
	}
	
	public User checkChangesToModify(User userToMod, UserDto userDto) {
		if((userDto.getEnabled() != null) && (!userToMod.getEnabled().equals(userDto.getEnabled())) )
			userToMod.setEnabled(userDto.getEnabled());
		
		if((userDto.getPassword() != null) && (!userToMod.getPassword().equals(userDto.getPassword())) )
			userToMod.setPassword(userDto.getPassword());
		
		if((userDto.getName() != null) && (!userToMod.getName().equals(userDto.getName())) )
			userToMod.setName(userDto.getName());
		
		if((userDto.getEmail() != null) && (!userToMod.getEmail().equals(userDto.getEmail())) )
			userToMod.setEmail(userDto.getEmail());
		
		if((userDto.getPhones()) != null && (!userToMod.getPhones().equals(userDto.getPhones())) )
			userToMod.setPhones(userDto.getPhones());
	
		return userToMod;
	}
	
	@EventListener(ApplicationReadyEvent.class)
	private void addUserAdmin(){
		User user = new User("admin", "admin@gmail.com" ,"admin", new Date(), new Date(),new Date(), (long) 1);
		this.userService.save(user);
		logger.info("Se inicializa la aplicacion con el usuario " + user.getEmail() + "Password: admin");
	}
	
}
