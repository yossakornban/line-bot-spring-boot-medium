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

import com.iphayao.linebot.model.UserLog;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Service
public class LoanApprovalRepository {

	@Autowired
	private DataSource dataSource;
	private NamedParameterJdbcTemplate jdbcTemplate = null;
	private StringBuilder stb = null;

	/* Loan approval ขออนุมัติสินเชื่อ */
	public void savePrefix(UserLog userLog, String Prefix) {
		ArrayList<Map<String, Object>> result = null;
		try {
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			stb = new StringBuilder();
			stb.append(" SELECT customer_user_id AS userId FROM customer ");
			stb.append(" WHERE customer_user_line_id = :lineId ");
			MapSqlParameterSource parameters = new MapSqlParameterSource();
			parameters.addValue("lineId", userLog.getUserID());
			result = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(stb.toString(), parameters);
			System.out.println("+++++-----****//// "+ result);
			stb.setLength(0);
			if (result.get(0).get("userId") == null) {
				stb.append(
						" INSERT INTO customer (customer_user_line_id, prefix_id, created_by, created_date, created_program, updated_by, updated_date, updated_program) ");
				stb.append(
						" VALUES (:lineId, :prefixId, 'SS-Pico-Finance', NOW(), 'SS-Pico-Finance','SS-Pico-Finance', NOW(), 'SS-Pico-Finance') ");

				parameters.addValue("lineId", userLog.getUserID());
				parameters.addValue("prefixId", Integer.parseInt(Prefix));
				jdbcTemplate.update(stb.toString(), parameters);
			} else {
				stb.append(" UPDATE customer SET prefix_id = :prefixId ");
				stb.append(" WHERE customer_user_id = :userId ");

				parameters.addValue("userId", result.get(0).get("userId"));
				parameters.addValue("prefixId", Integer.parseInt(Prefix));
				jdbcTemplate.update(stb.toString(), parameters);
			}

		} catch (EmptyResultDataAccessException ex) {
			log.error("Msg :: {}, Trace :: {}", ex.getMessage(), ex.getStackTrace());
		}
	}

	public void saveFirstName(UserLog userLog, String firstName) {
		try {
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			stb = new StringBuilder();

			stb.append(" UPDATE customer SET customer_first_name = :firstName ");
			stb.append(" WHERE customer_user_line_id = :lineId ");

			MapSqlParameterSource parameters = new MapSqlParameterSource();
			parameters.addValue("lineId", userLog.getUserID());
			parameters.addValue("firstName", firstName);

			jdbcTemplate.update(stb.toString(), parameters);
		} catch (EmptyResultDataAccessException ex) {
			log.error("Msg :: {}, Trace :: {}", ex.getMessage(), ex.getStackTrace());
		}
	}

	public void saveLastName(UserLog userLog, String lastName) {
		try {
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			stb = new StringBuilder();

			stb.append(" UPDATE customer SET customer_last_name = :lastName ");
			stb.append(" WHERE customer_user_line_id = :lineId ");

			MapSqlParameterSource parameters = new MapSqlParameterSource();
			parameters.addValue("lineId", userLog.getUserID());
			parameters.addValue("lastName", lastName);

			jdbcTemplate.update(stb.toString(), parameters);
		} catch (EmptyResultDataAccessException ex) {
			log.error("Msg :: {}, Trace :: {}", ex.getMessage(), ex.getStackTrace());
		}
	}

	public void saveTel(UserLog userLog, String telPhone) {
		try {
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			stb = new StringBuilder();

			stb.append(" UPDATE customer SET customer_tel = :telPhone ");
			stb.append(" WHERE customer_user_line_id = :lineId ");

			MapSqlParameterSource parameters = new MapSqlParameterSource();
			parameters.addValue("lineId", userLog.getUserID());
			parameters.addValue("telPhone", telPhone);

			jdbcTemplate.update(stb.toString(), parameters);
		} catch (EmptyResultDataAccessException ex) {
			log.error("Msg :: {}, Trace :: {}", ex.getMessage(), ex.getStackTrace());
		}
	}

	public void saveEmail(UserLog userLog, String email) {
		try {
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			stb = new StringBuilder();

			stb.append(" UPDATE customer SET customer_email = :email ");
			stb.append(" WHERE customer_user_line_id = :lineId ");

			MapSqlParameterSource parameters = new MapSqlParameterSource();
			parameters.addValue("lineId", userLog.getUserID());
			parameters.addValue("email", email);

			jdbcTemplate.update(stb.toString(), parameters);
		} catch (EmptyResultDataAccessException ex) {
			log.error("Msg :: {}, Trace :: {}", ex.getMessage(), ex.getStackTrace());
		}
	}

	public void saveSalary(UserLog userLog, String salary) {
		ArrayList<Map<String, Object>> result = null;
		ArrayList<Map<String, Object>> result2 = null;
		try {
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			stb = new StringBuilder();
			stb.append(" SELECT customer_user_id AS userId FROM customer ");
			stb.append(" WHERE customer_user_line_id = :lineId ");
			MapSqlParameterSource parameters = new MapSqlParameterSource();
			parameters.addValue("lineId", userLog.getUserID());
			result = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(stb.toString(), parameters);

			stb.setLength(0);
			stb.append(" SELECT customer_user_id AS userId FROM request_loan ");
			stb.append(" WHERE customer_user_id = :userId ");
			parameters.addValue("userId", result.get(0).get("userId"));
			result2 = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(stb.toString(), parameters);

			if (result2.get(0).get("userId") == null) {
				stb.setLength(0);
				stb.append(
						" INSERT INTO request_loan (customer_user_id, salary, created_by, created_date, created_program, updated_by, updated_date, updated_program) ");
				stb.append(
						" VALUES (:userId, :salary, 'SS-Pico-Finance', NOW(), 'SS-Pico-Finance','SS-Pico-Finance', NOW(), 'SS-Pico-Finance') ");
				log.info("result.{}", result);
				parameters.addValue("userId", result.get(0).get("userId"));
				parameters.addValue("salary", salary);
				jdbcTemplate.update(stb.toString(), parameters);
			} else {
				stb.setLength(0);
				stb.append(" UPDATE request_loan SET salary = :salary ");
				stb.append(" WHERE customer_user_id = :userId ");
				log.info("result.{}", result);
				parameters.addValue("userId", result.get(0).get("userId"));
				parameters.addValue("salary", salary);
				jdbcTemplate.update(stb.toString(), parameters);
			}

		} catch (EmptyResultDataAccessException ex) {
			log.error("Msg :: {}, Trace :: {}", ex.getMessage(), ex.getStackTrace());
		}
	}

	public void saveCreditType(UserLog userLog, String creditType) {
		ArrayList<Map<String, Object>> result = null;
		try {
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			stb = new StringBuilder();
			stb.append(" SELECT customer_user_id AS userId FROM customer ");
			stb.append(" WHERE customer_user_line_id = :lineId ");
			MapSqlParameterSource parameters = new MapSqlParameterSource();
			parameters.addValue("lineId", userLog.getUserID());
			result = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(stb.toString(), parameters);
			stb.setLength(0);
			stb.append(" UPDATE request_loan SET credit_type_id = :creditType ");
			stb.append(" WHERE customer_user_id = :userId ");

			parameters.addValue("userId", result.get(0).get("userId"));
			parameters.addValue("creditType", Integer.parseInt(creditType));

			jdbcTemplate.update(stb.toString(), parameters);
		} catch (EmptyResultDataAccessException ex) {
			log.error("Msg :: {}, Trace :: {}", ex.getMessage(), ex.getStackTrace());
		}
	}

}
