package com.apirest.springboot.app.bank.model.dao;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.apirest.springboot.app.bank.model.entity.User;

public interface UserDaoInterface extends JpaRepository<User, UUID>{

	//User findForEmail(String email);
	@Query("select u from User u where u.email = ?1")
	User findForEmail(String email);

}
