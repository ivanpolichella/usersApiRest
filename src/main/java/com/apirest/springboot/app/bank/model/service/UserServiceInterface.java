package com.apirest.springboot.app.bank.model.service;

import java.util.List;
import java.util.UUID;

import com.apirest.springboot.app.bank.model.entity.User;

public interface UserServiceInterface {
	
	public List<User> listAll();
	public User findForEmail(String email);
	public User findById(UUID id);
	public User save(User u);
	public User modify(User u);
	public void delete(User user);
}
