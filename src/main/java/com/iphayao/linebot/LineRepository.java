package com.iphayao.linebot;

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

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@Data
public class LineRepository {
	
	public class Model {

		public String profileCode;
		public String profileDesc;
		public Boolean active;  
		private String createdProgram; 
		private String updatedProgram;
		  
}

	@Autowired
	private DataSource dataSource;
	private NamedParameterJdbcTemplate jdbcTemplate = null;
	private StringBuilder stb = null;

	public int register(String empCode, String lineID) {
		int aaa = 0;
		try {
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			stb = new StringBuilder();

			stb.append(" UPDATE employee SET emp_line_id = :lineid ");
			stb.append(" WHERE emp_code = :empcode ");

			MapSqlParameterSource parameters = new MapSqlParameterSource();
			parameters.addValue("empcode", empCode);
			parameters.addValue("lineid", lineID);

			 aaa = jdbcTemplate.update(stb.toString(), parameters);
			return aaa;
//					(stb.toString(), parameters,
//					new BeanPropertyRowMapper<Entity>(Entity.class));
		} catch (EmptyResultDataAccessException ex) {
			log.error("Msg :: {}, Trace :: {}", ex.getMessage(), ex.getStackTrace());
		}
		return aaa;
	}
	
	public ArrayList<Map<String, Object>> findEmp(String empCode) {
		ArrayList<Map<String, Object>> result = null;
//		List<Map<String, Object>> result = null;
		try {
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			stb = new StringBuilder();

			stb.append(" SELECT message FROM log_chat ");
			stb.append(" WHERE emp_code = :empcode ");

			MapSqlParameterSource parameters = new MapSqlParameterSource();
			parameters.addValue("empcode", empCode);

			 result  = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(stb.toString(), parameters);
		} catch (EmptyResultDataAccessException ex) {
			log.error("Msg :: {}, Trace :: {}", ex.getMessage(), ex.getStackTrace());
		}
		return result;
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

