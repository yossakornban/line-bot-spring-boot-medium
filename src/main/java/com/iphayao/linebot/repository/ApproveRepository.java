package com.iphayao.linebot.repository;

import java.util.ArrayList;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
//import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.iphayao.linebot.model.Customer;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Service
public class ApproveRepository  {
	
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
	private StringBuilder stb2 = null;

	public String approve(Customer data) {
		ArrayList<Map<String, Object>> result = null;
		try {
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			stb = new StringBuilder();

			stb.append(" UPDATE employee SET appove = :approve ");
			stb.append(" WHERE emp_emp_code = :emp_code ");

			MapSqlParameterSource parameters = new MapSqlParameterSource();
			parameters.addValue("emp_code", data.getCustomer_code());
			parameters.addValue("approve", data.getApprove_status());
			jdbcTemplate.update(stb.toString(), parameters);

			stb2 = new StringBuilder();

			stb2.append(" Select emp_emp_line_id From employee");
			stb2.append(" WHERE emp_emp_code = :emp_code ");

			MapSqlParameterSource parameters2 = new MapSqlParameterSource();
			parameters2.addValue("emp_code", data.getCustomer_code());
			result = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(stb2.toString(), parameters2);
			
//					(stb.toString(), parameters,
//					new BeanPropertyRowMapper<Entity>(Entity.class));
		} catch (EmptyResultDataAccessException ex) {
			log.error("Msg :: {}, Trace :: {}", ex.getMessage(), ex.getStackTrace());
		}

		return (String) result.get(0).get("emp_emp_line_id");
	}

}

