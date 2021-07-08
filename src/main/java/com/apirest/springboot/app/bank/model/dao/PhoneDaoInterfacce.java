package com.apirest.springboot.app.bank.model.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.apirest.springboot.app.bank.model.entity.Phone;

@Repository
public interface PhoneDaoInterfacce extends CrudRepository<Phone, Long>{

}
