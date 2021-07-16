package com.apirest.springboot.app.bank.security.util;

import com.apirest.springboot.app.bank.model.dto.UserDto;
import com.apirest.springboot.app.bank.model.entity.User;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

public class Mapper {
	
	@Autowired
    private static ModelMapper modelMapper = new ModelMapper();
	
	public static UserDto convertToDto(User usr) {
		UserDto userDto = modelMapper.map(usr, UserDto.class);
	    return userDto;
	}
	
	public static User convertToEntity(UserDto userDto){
	    User usr = modelMapper.map(userDto, User.class);	 
	    return usr;
	}

}
