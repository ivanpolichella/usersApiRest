package com.apirest.springboot.app.bank.model.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apirest.springboot.app.bank.model.dao.UserDaoInterface;
import com.apirest.springboot.app.bank.model.entity.User;

@Service
public class UserServiceImpl implements UserServiceInterface {

	@Autowired
	private UserDaoInterface userdao;
	
	
	@Override
	@Transactional(readOnly = true)
	public List<User> listAll() {
		List<User> resultado = (List<User>) userdao.findAll();
		return resultado;
	}

	@Override
	@Transactional(readOnly = true)
	public User findForEmail(String email) {
		return userdao.findForEmail(email);
	}

	@Override
	public User save(User u) {
		return userdao.save(u);
	}

	@Override
	public User modify(User u) {
		return userdao.save(u);
	}

	@Override
	public User findById(UUID id) {
		return userdao.findById(id).orElse(null);
	}

	@Override
	public void delete(User u) {
		userdao.delete(u);
	}

}
