package com.iphayao.linebot.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
//import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.iphayao.linebot.model.Customer;
import com.iphayao.linebot.repository.ApprovePaymentRepository.ModelUpdate;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Service
public class ApprovePaymentRepository {

	public class Model {

		public String profileCode;
		public String profileDesc;
		public Boolean active;
		// private String createdProgram;
		// private String updatedProgram;

	}
	
	@Data
	public class ModelUpdate {
		private Integer paymentId;
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

	public String approvePay(Customer data) {
		ArrayList<Map<String, Object>> result = null;
		ArrayList<Map<String, Object>> account_id = null;
		String random;
		try {
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			// stb = new StringBuilder();
			// random = randomAlphaNumeric(10);

			// if (data.getApprove_status()) {
			// stb.append(
			// " INSERT INTO account(account_no, customer_user_id, created_by, created_date,
			// created_program, updated_by, updated_date, updated_program) ");
			// stb.append(
			// " VALUES (:account_no, :customer_user_id, 'SS-Pico-Finance', NOW(),
			// 'SS-Pico-Finance','SS-Pico-Finance', NOW(), 'SS-Pico-Finance') ");

			// MapSqlParameterSource parameters = new MapSqlParameterSource();
			// parameters.addValue("account_no", random);
			// parameters.addValue("customer_user_id", data.getCustomer_code());
			// jdbcTemplate.update(stb.toString(), parameters);
			// stb.setLength(0);
			// stb.append(" UPDATE request_loan SET approve_status = :approve_status ");
			// stb.append(" WHERE customer_user_id = :userId ");

			// parameters.addValue("userId", data.getCustomer_code());
			// parameters.addValue("approve_status", data.getApprove_status());
			// jdbcTemplate.update(stb.toString(), parameters);
			// stb.setLength(0);

			// stb.append(" Select account_id From account");
			// stb.append(" WHERE customer_user_id = :customer_user_id ");

			// parameters.addValue("customer_user_id", data.getCustomer_code());
			// account_id = (ArrayList<Map<String, Object>>)
			// jdbcTemplate.queryForList(stb.toString(), parameters);

			// stb.setLength(0);
			// stb.append(
			// " INSERT INTO payment(account_id, status_id, payment_amount_paid,
			// payment_period, payment_principle, ");
			// stb.append(
			// " payment_installment, payment_outstanding_balance, payment_pay_date_next,
			// created_by, created_date, created_program, updated_by, updated_date,
			// updated_program) ");
			// stb.append(
			// " VALUES (:account_id, :status_id, :payment_amount_paid, :payment_period,
			// :payment_principle, :payment_installment, :payment_outstanding_balance ");
			// stb.append(
			// " ,to_timestamp(extract(year from now())::TEXT || '-' || lpad(extract(month
			// from now() + INTERVAL '1 month')::text, 2, '0') || '-20', 'yyyy-mm-dd') ");
			// stb.append(
			// " ,'SS-Pico-Finance', NOW(), 'SS-Pico-Finance','SS-Pico-Finance', NOW(),
			// 'SS-Pico-Finance')");

			// parameters.addValue("account_id", account_id.get(0).get("account_id"));
			// parameters.addValue("status_id", 1);
			// parameters.addValue("payment_period", "1");
			// parameters.addValue("payment_amount_paid", 11050);
			// parameters.addValue("payment_principle", 10000);
			// parameters.addValue("payment_installment", 1050);
			// parameters.addValue("payment_outstanding_balance", 90000);

			// jdbcTemplate.update(stb.toString(), parameters);
			// }

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
		System.out.println("customer_user_line_id ============ " + (String) result.get(0).get("customer_user_line_id"));
		return (String) result.get(0).get("customer_user_line_id");
	}

	public ArrayList<Map<String, Object>> searchPaymant(String keyword) {
		ArrayList<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		try {
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			StringBuilder sql = new StringBuilder();
			sql.append(
					" SELECT payment_id, cus.customer_first_name || ' ' || cus.customer_last_name AS name, payment_id, payment_period ");
			sql.append(" FROM payment pay ");
			sql.append("   JOIN account acc ");
			sql.append("     ON pay.account_id = acc.account_id ");
			sql.append("   JOIN customer cus ");
			sql.append("     ON cus.customer_user_id = acc.customer_user_id ");
			sql.append("   JOIN status stat ");
			sql.append("     ON stat.status_id = pay.status_id ");
			sql.append(" WHERE 1 = 1 ");
			sql.append("   AND ( cus.customer_first_name LIKE :keyword ");
			sql.append("         OR cus.customer_last_name LIKE :keyword ");
			sql.append("         OR cus.customer_user_id::VARCHAR LIKE :keyword ");
			sql.append("         OR stat.status_name LIKE :keyword) ");
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("keyword", "%" + keyword + "%");
			result = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(sql.toString(), params);
		} catch (EmptyResultDataAccessException ex) {
			log.error("Msg :: {}, Trace :: {}", ex.getMessage(), ex.getStackTrace());
		}
		return result;
	}

	public Map<String, Object> searchPaymantUpdate(Integer paymentId) {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			StringBuilder sql = new StringBuilder();
			sql.append(
					" SELECT pref.prefix_name, cus.customer_first_name, cus.customer_last_name, cus.customer_tel, cus.customer_user_line_id ");
			sql.append(
					" , cus.customer_email, cus.career, reql.salary, credt.credit_type_name, pay.payment_period, pay.payment_amount_paid, slippay.slip ");
			sql.append(" FROM payment pay ");
			sql.append("   JOIN account acc ");
			sql.append("     ON pay.account_id = acc.account_id ");
			sql.append("   JOIN customer cus ");
			sql.append("     ON cus.customer_user_id = acc.customer_user_id ");
			sql.append("   JOIN status stat ");
			sql.append("     ON stat.status_id = pay.status_id ");
			sql.append("   JOIN prefix pref ");
			sql.append("     ON pref.prefix_id = cus.prefix_id ");
			sql.append("   JOIN request_loan reql ");
			sql.append("     ON reql.customer_user_id = cus.customer_user_id ");
			sql.append("   JOIN credit_type credt ");
			sql.append("     ON reql.credit_type_id = credt.credit_type_id ");
			sql.append("   JOIN slip_payment slippay ");
			sql.append("     ON slippay.payment_id = pay.payment_id ");
			sql.append(" WHERE 1 = 1 ");
			sql.append("   AND pay.payment_id = :paymentId ");
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("paymentId", paymentId);
			result = jdbcTemplate.queryForMap(sql.toString(), params);
		} catch (EmptyResultDataAccessException ex) {
			log.error("Msg :: {}, Trace :: {}", ex.getMessage(), ex.getStackTrace());
		}
		return result;
	}

	public Map<String, Object> Update(ModelUpdate model) {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			StringBuilder sql = new StringBuilder();
			sql.append(" UPDATE payment ");
			sql.append(" SET status_id = 6 ");
			sql.append(" WHERE payment_id = :paymentId ");
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("paymentId", model.getPaymentId());
			int checkUpdate = jdbcTemplate.update(sql.toString(), params);
			if (checkUpdate == 1) {
				result = searchPaymantUpdate(model.getPaymentId());
			} else {
				result = null;
			}
		} catch (EmptyResultDataAccessException ex) {
			log.error("Msg :: {}, Trace :: {}", ex.getMessage(), ex.getStackTrace());
		}
		return result;
	}

}
