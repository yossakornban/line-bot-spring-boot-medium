package com.pico.communication.service;

import java.util.ArrayList;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
//import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.pico.communication.model.UserLog;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Service
public class LineService {
	
	public class Model {

		public String profileCode;
		public String profileDesc;
		public Boolean active;  
		// private String createdProgram; 
		// private String updatedProgram;
		  
}

	@Autowired
	private DataSource dataSource;
	private NamedParameterJdbcTemplate jdbcTemplate = null;
	private StringBuilder stb = null;

	public int register(UserLog userLog, String SenderId) {
		return (Integer) null;
	}
	
	public String findEmp(String empCode) {
		return null;
	}
	
	public ArrayList<Map<String, Object>> list() {
		return null;
	}
}

