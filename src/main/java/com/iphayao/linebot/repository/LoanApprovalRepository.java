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

import com.iphayao.linebot.model.Register;
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
	public void approveLoan(Register data) {
		ArrayList<Map<String, Object>> result = null;
		ArrayList<Map<String, Object>> id = null;
		try {
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			stb = new StringBuilder();
			MapSqlParameterSource parameters = new MapSqlParameterSource();
			stb.append(" SELECT customer_user_id AS userId FROM customer ");
			stb.append(" WHERE customer_user_line_id = :lineId ");
			parameters.addValue("lineId", data.getCustomer_user_line_id());
			result = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(stb.toString(), parameters);
			stb.setLength(0);
			if (result.size() == 0) {
				stb.append(
						" INSERT INTO customer (customer_user_line_id, prefix_id, customer_first_name, customer_last_name, customer_tel, customer_email, created_by, created_date, created_program, updated_by, updated_date, updated_program) ");
				stb.append(
						" VALUES (:lineId, :prefixId, :first_name, :last_name, :tel, :email, 'SS-Pico-Finance', NOW(), 'SS-Pico-Finance','SS-Pico-Finance', NOW(), 'SS-Pico-Finance') ");

				parameters.addValue("lineId", data.getCustomer_user_line_id());
				parameters.addValue("prefixId", data.getPrefix_id());
				parameters.addValue("first_name", data.getCustomer_first_name());
				parameters.addValue("last_name", data.getCustomer_last_name());
				parameters.addValue("tel", data.getCustomer_tel());
				parameters.addValue("email", data.getCustomer_email());
				jdbcTemplate.update(stb.toString(), parameters);

				stb.setLength(0);

				stb.append(" SELECT customer_user_id AS userId FROM customer ");
				stb.append(" WHERE customer_user_line_id = :lineId ");
				parameters.addValue("lineId", data.getCustomer_user_line_id());
				id = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(stb.toString(), parameters);

				stb.setLength(0);

				stb.append(
						" INSERT INTO request_loan (customer_user_id, credit_type_id, salary, created_by, created_date, created_program, updated_by, updated_date, updated_program, career) ");
				stb.append(
						" VALUES (:userId, :credit_type_id, :salary, 'SS-Pico-Finance', NOW(), 'SS-Pico-Finance','SS-Pico-Finance', NOW(), 'SS-Pico-Finance', :career) ");

				parameters.addValue("userId", id.get(0).get("userId"));
				parameters.addValue("credit_type_id", data.getCredit_type_id());
				parameters.addValue("salary", data.getSalary());
				parameters.addValue("career", data.getCareer());
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

			if (result2.size() == 0) {
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
