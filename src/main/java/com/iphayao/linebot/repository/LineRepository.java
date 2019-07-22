package com.iphayao.linebot.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
//import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import com.iphayao.linebot.model.UserLog;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Service
public class LineRepository {
	
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
		int aaa = 0;
		try {
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			stb = new StringBuilder();

			stb.append(" UPDATE db.employee SET emp_emp_line_id = :lineid, sender_id = :sender_id ");
			stb.append(" WHERE emp_emp_code = :empcode ");

			MapSqlParameterSource parameters = new MapSqlParameterSource();
			System.out.println("1111111111111111111"+userLog.getEmpCode());
			System.out.println( "22222222222222222222"+ userLog.getUserID());
			parameters.addValue("empcode", userLog.getEmpCode());
			parameters.addValue("lineid", userLog.getUserID());
			parameters.addValue("sender_id", SenderId);
			
			 aaa = jdbcTemplate.update(stb.toString(), parameters);
			return aaa;
//					(stb.toString(), parameters,
//					new BeanPropertyRowMapper<Entity>(Entity.class));
		} catch (EmptyResultDataAccessException ex) {
			log.error("Msg :: {}, Trace :: {}", ex.getMessage(), ex.getStackTrace());
		}
		return aaa;
	}
	
	public String findEmp(String empCode) {
		ArrayList<Map<String, Object>> result = null;
//		List<Map<String, Object>> result = null;
		try {
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			stb = new StringBuilder();

			stb.append(" SELECT emp_emp_name FROM db.employee ");
			stb.append(" WHERE emp_emp_code = :empcode ");

			MapSqlParameterSource parameters = new MapSqlParameterSource();
			parameters.addValue("empcode", empCode);

			 result  = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(stb.toString(), parameters);

			 if (result.size() == 0) {
				 return null;
			 }
			 
		} catch (EmptyResultDataAccessException ex) {
			log.error("Msg :: {}, Trace :: {}", ex.getMessage(), ex.getStackTrace());
		}
		return (String) result.get(0).get("emp_emp_name");
	}
	
	public ArrayList<Map<String, Object>> list() {
		ArrayList<Map<String, Object>> result = null;
//		List<Map<String, Object>> result = null;
		try {
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			stb = new StringBuilder();

			stb.append(" SELECT message FROM log_chat ");

			MapSqlParameterSource parameters = new MapSqlParameterSource();

			 result  = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(stb.toString(), parameters);
		} catch (EmptyResultDataAccessException ex) {
			log.error("Msg :: {}, Trace :: {}", ex.getMessage(), ex.getStackTrace());
		}
		return result;
	}
}
