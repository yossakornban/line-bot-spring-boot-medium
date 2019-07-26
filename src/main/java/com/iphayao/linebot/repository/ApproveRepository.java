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
public class ApproveRepository {

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

	private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	public static String randomAlphaNumeric(int count) {
		StringBuilder builder = new StringBuilder();
		while (count-- != 0) {
			int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
			builder.append(ALPHA_NUMERIC_STRING.charAt(character));
		}
		return builder.toString();
	}

	public String approve(Customer data) {
		ArrayList<Map<String, Object>> result = null;
		String random;
		try {
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			stb = new StringBuilder();
			random = randomAlphaNumeric(10);

			if (data.getApprove_status()) {
				stb.append(
						" INSERT INTO account(account_no, customer_user_id, created_by, created_date, created_program, updated_by, updated_date, updated_program, account_approve) ");
				stb.append(
						" VALUES (:account_no, :customer_user_id, 'SS-Pico-Finance', NOW(), 'SS-Pico-Finance','SS-Pico-Finance', NOW(), 'SS-Pico-Finance') ");

				MapSqlParameterSource parameters = new MapSqlParameterSource();
				parameters.addValue("account_no", random);
				parameters.addValue("customer_user_id", data.getCustomer_code());
				parameters.addValue("account_approve", data.getApprove_status());
				jdbcTemplate.update(stb.toString(), parameters);
				stb.setLength(0);

				stb2 = new StringBuilder();
				stb2.append(" Select customer_user_line_id From customer");
				stb2.append(" WHERE customer_user_id = :customer_user_id ");
	
				MapSqlParameterSource parameters2 = new MapSqlParameterSource();
				parameters2.addValue("customer_user_id", data.getCustomer_code());
				result = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(stb2.toString(), parameters2);

				stb.setLength(0);
				stb.append(
						" INSERT INTO payment(account_id, status_id, payment_amount_paid, payment_period, payment_principle, ");
				stb.append(
						" payment_installment, payment_outstanding_balance, payment_pay_date, payment_pay_date_next, created_by, created_date, created_program, updated_by, updated_date, updated_program, payment_desc) ");
				stb.append(
						" VALUES (:account_no, :customer_user_id, 'SS-Pico-Finance', NOW(), 'SS-Pico-Finance','SS-Pico-Finance', NOW(), 'SS-Pico-Finance') ");

				parameters.addValue("account_no", random);
				parameters.addValue("customer_user_id", data.getCustomer_code());
				parameters.addValue("account_approve", data.getApprove_status());
				jdbcTemplate.update(stb.toString(), parameters);
			}

			stb2 = new StringBuilder();
			stb2.append(" Select customer_user_line_id From customer");
			stb2.append(" WHERE customer_user_id = :customer_user_id ");

			MapSqlParameterSource parameters2 = new MapSqlParameterSource();
			parameters2.addValue("customer_user_id", data.getCustomer_code());
			result = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(stb2.toString(), parameters2);

			// (stb.toString(), parameters,
			// new BeanPropertyRowMapper<Entity>(Entity.class));
		} catch (EmptyResultDataAccessException ex) {
			log.error("Msg :: {}, Trace :: {}", ex.getMessage(), ex.getStackTrace());
		}

		return (String) result.get(0).get("customer_user_line_id");
	}

}
