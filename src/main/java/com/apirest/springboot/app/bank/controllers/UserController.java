package com.apirest.springboot.app.bank.controllers;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.apirest.springboot.app.bank.exception.MessageException;
import com.apirest.springboot.app.bank.model.entity.User;
import com.apirest.springboot.app.bank.model.service.UserServiceInterface;

@RestController
public class UserController {
	private static final Logger logger = LoggerFactory.getLogger(UserController.class);
	@Autowired
	private UserServiceInterface userService;
	
	@GetMapping("/usuarios")
	public List<User> obtenerUsuarios() {
		List<User> resultado = userService.listAll();
		logger.debug("Se listarán todos los usuarios");
		return resultado;
		
	}
	
	@GetMapping("/usuarios/{email}")
	public User obtenerUsuarioPorEmail(@PathVariable String email) {
		User resultado = userService.findForEmail(email);
		logger.debug("Se realizó una busqueda por mail entre todos los usuarios");
		return resultado;	
	}
	
	@PostMapping("/usuario")
	public User addUser(HttpServletResponse response, @RequestBody User user){
		if(!this.isValidEmail(user.getEmail())) {
			throw new MessageException("Mail Invalido!" );
		}
		
		if(this.mailInUse(user.getEmail())) {
			throw new MessageException("El correo ya registrado");
		}
		
		if(this.invalidPassword(user.getPassword()))
			throw new MessageException("La clave no cumple con los requisitos de seguridad (Una Mayúscula, letras minúsculas, y dos números)");
		
		user.setCreatedDate(new Date());
		user.setLastModified(new Date());
		user.setEnabled((long) 1);
		User result = this.userService.save(user);
		logger.debug("Se agregó el usuario" + user.getEmail());
		return result;
	}
	
	private boolean invalidPassword(String password) {
		String pattern = "(?=.*[0-9]{2})(?=.*[a-z])(?=.*[A-Z]).{4,}";
		return (!password.matches(pattern));
	}

	private boolean mailInUse(String email) {
		return ( this.userService.findForEmail(email) != null );
	}

	public boolean isValidEmail(String email) {
		   String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
		   return email.matches(regex);
	}
	
	@PutMapping("/usuario")
	public User modifyUser(@RequestBody User user) {
		User userToMod = userService.findById(user.getId());
		if (userToMod != null)
			userToMod = checkChangesToModify(userToMod,user);
		userToMod.setLastModified(new Date());
		User result = this.userService.modify(userToMod);
		logger.debug("Se modificó el usuario" + user.getEmail());
		return result;
	}
	
	@DeleteMapping("/usuario")
	public void deleteUser(@RequestBody User user) {
		this.userService.delete(user);
		logger.debug("Se eliminó el usuario" + user.getEmail());
	}
	
	public User checkChangesToModify(User userToMod, User user) {
		if((user.getEnabled() != null) && (!userToMod.getEnabled().equals(user.getEnabled())) )
			userToMod.setEnabled(user.getEnabled());
		
		if((user.getPassword() != null) && (!userToMod.getPassword().equals(user.getPassword())) )
			userToMod.setPassword(user.getPassword());
		
		if((user.getName() != null) && (!userToMod.getName().equals(user.getName())) )
			userToMod.setName(user.getName());
		
		if((user.getEmail() != null) && (!userToMod.getEmail().equals(user.getEmail())) )
			userToMod.setEmail(user.getEmail());
		
		if((user.getPhones()) != null && (!userToMod.getPhones().equals(user.getPhones())) )
			userToMod.setPhones(user.getPhones());
	
		return userToMod;
	}
	
}
