package com.apirest.springboot.app.bank.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.apirest.springboot.app.bank.model.dao.UserDaoInterface;
import org.springframework.security.core.userdetails.User;
import java.util.ArrayList;

@Service
public class MyUserDetailsService implements UserDetailsService {
	@Autowired
	private UserDaoInterface userDao;
	
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        
    	com.apirest.springboot.app.bank.model.entity.User result = userDao.findForEmail(s);
    	return new User(result.getEmail(), result.getPassword(),
                new ArrayList<>());
    }
}
