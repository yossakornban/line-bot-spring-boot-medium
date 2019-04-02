package com.iphayao.linebot;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

import com.iphayao.linebot.model.UserLog;
import com.linecorp.bot.model.message.flex.component.Text;

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
	
	
	public  String CountVote(UserLog  userLog) {
		
		ArrayList<Map<String, Object>> result = null;
		
		// List<Map<String, Object>> result = null;
		try {
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			stb = new StringBuilder();
			stb.append(" select count(emp_emp_id) from employee_vote where emp_emp_id =:empcode ");
			MapSqlParameterSource parameters = new MapSqlParameterSource();
			parameters.addValue("empcode", userLog.getEmpCode());
			result = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(stb.toString(), parameters);
			String kkl = result.toString();
			kkl.replace("[{count=", "");
			kkl.replace("}]", "");
			int raider = Integer.parseInt(kkl);
			
			userLog.setCountVote(raider);
			System.out.println("333333333333333333333333333333333333333333333"+result);
			
			if (result.size() == 0) {
				return null;
			}
		} catch (EmptyResultDataAccessException ex) {
			log.error("Msg :: {}, Trace :: {}", ex.getMessage(), ex.getStackTrace());
		}
		return (String) result.get(0).get("emp_emp_id");
	
		

		
		
	}
	public int register(UserLog userLog) {
		int aaa = 0;
		try {
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			stb = new StringBuilder();
			stb.append(" UPDATE employee SET emp_emp_line_id = :lineid");
			stb.append(" WHERE emp_emp_code = :empcode ");

			MapSqlParameterSource parameters = new MapSqlParameterSource();
			parameters.addValue("empcode", userLog.getEmpCode());
			parameters.addValue("lineid", userLog.getUserID());

			aaa = jdbcTemplate.update(stb.toString(), parameters);
			return aaa;
			// (stb.toString(), parameters,
			// new BeanPropertyRowMapper<Entity>(Entity.class));
		} catch (EmptyResultDataAccessException ex) {
			log.error("Msg :: {}, Trace :: {}", ex.getMessage(), ex.getStackTrace());
		}
		return aaa;
	}
	public int saveFood(UserLog string ){
	
		int aaa = 0;
		try {
			MapSqlParameterSource parameters = new MapSqlParameterSource();
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			stb = new StringBuilder();
			DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			Date date = new Date();
	   	     stb.append("insert into employee_vote (emp_emp_id,food_id,date_vote)  values  (:employeeCode,:foodIdVote,:dateNow)");
		    String employeeCode = string.getEmpCode();
		    String FoodsIdVote = string.getFoodId();
			parameters.addValue("employeeCode", employeeCode);
			parameters.addValue("foodIdVote", FoodsIdVote);
			parameters.addValue("dateNow", dateFormat.format(date));
		    aaa = jdbcTemplate.update(stb.toString(), parameters);
			return aaa;
			// (stb.toString(), parameters,
			// new BeanPropertyRowMapper<Entity>(Entity.class));
		} catch (EmptyResultDataAccessException ex) {
			log.error("Msg :: {}, Trace :: {}", ex.getMessage(), ex.getStackTrace());
		}
		return aaa;
	}
	public String findFoods(String foodId) {
		ArrayList<Map<String, Object>> result = null;
		// List<Map<String, Object>> result = null;
		try {
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			stb = new StringBuilder();
			stb.append(" select  food_food_name from foods ");
			stb.append(" WHERE food_food_id = :foodCode ");
			MapSqlParameterSource parameters = new MapSqlParameterSource();
			parameters.addValue("foodCode", foodId);

			result = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(stb.toString(), parameters);
			if (result.size() == 0) {
				return null;
			}
		} catch (EmptyResultDataAccessException ex) {
			log.error("Msg :: {}, Trace :: {}", ex.getMessage(), ex.getStackTrace());

		}
		return (String) result.get(0).get("food_food_name");
	}

	public String findEmp(String empCode) {
		ArrayList<Map<String, Object>> result = null;
		// List<Map<String, Object>> result = null;
		try {
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			stb = new StringBuilder();
			stb.append(" SELECT emp_emp_name FROM employee ");
			stb.append(" WHERE emp_emp_code = :empcode ");
			MapSqlParameterSource parameters = new MapSqlParameterSource();
			parameters.addValue("empcode", empCode);
			result = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(stb.toString(), parameters);
			if (result.size() == 0) {
				return null;
			}
		} catch (EmptyResultDataAccessException ex) {
			log.error("Msg :: {}, Trace :: {}", ex.getMessage(), ex.getStackTrace());
		}
		return (String) result.get(0).get("emp_emp_name");
	}

	public ArrayList<Map<String, Object>> holidayList() {
		ArrayList<Map<String, Object>> result = null;
		// List<Map<String, Object>> result = null;
		try {
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			stb = new StringBuilder();
			stb.append(" select * from holiday order by number_event ");
			MapSqlParameterSource parameters = new MapSqlParameterSource();
			result = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(stb.toString(), parameters);
		} catch (EmptyResultDataAccessException ex) {
			log.error("Msg :: {}, Trace :: {}", ex.getMessage(), ex.getStackTrace());
		}
		return result;
	}

	public ArrayList<Map<String, Object>> Holiday_Soon() {
		ArrayList<Map<String, Object>> result = null;
		// List<Map<String, Object>> result = null;
		try {
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			stb = new StringBuilder();
			stb.append(
					
					" select to_date(date_holiday, 'dd/mm/yyyy'),name_holiday from holiday where to_date(date_holiday, 'dd/mm/yyyy') between now() and to_date('31/12/2019', 'dd/mm/yyyy') order by to_date  limit 3 ");
			MapSqlParameterSource parameters = new MapSqlParameterSource();
			result = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(stb.toString(), parameters);
		} catch (EmptyResultDataAccessException ex) {
			log.error("Msg :: {}, Trace :: {}", ex.getMessage(), ex.getStackTrace());
		}
		return result;
	}
	public ArrayList<Map<String, Object>> list() {
		ArrayList<Map<String, Object>> result = null;
		// List<Map<String, Object>> result = null;
		try {
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			stb = new StringBuilder();
			stb.append(" SELECT message FROM log_chat ");
			MapSqlParameterSource parameters = new MapSqlParameterSource();
			result = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(stb.toString(), parameters);
		} catch (EmptyResultDataAccessException ex) {
			log.error("Msg :: {}, Trace :: {}", ex.getMessage(), ex.getStackTrace());
		}
		return result;
	}
}
