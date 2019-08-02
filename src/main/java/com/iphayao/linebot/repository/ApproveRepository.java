package com.iphayao.linebot.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
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

	public ArrayList<Map<String, Object>> line03Search(String countrySearch) throws Exception {
		log.info("<--Start ApproveRepository.{}-->", "line03Search()");
		jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		
		StringBuilder sql = new StringBuilder();
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		
		sql.append("SELECT status.status_name, cus.* ");
		sql.append("FROM customer cus ");
		sql.append("JOIN request_loan loan ON loan.customer_user_id = cus.customer_user_id  ");
		sql.append("JOIN status ON status.status_id = loan.status  ");
		sql.append("WHERE 1 = 1 ");
		try {
			return (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(sql.toString(), parameters);
		} catch (Exception ex) {
			log.error("Msg :: {}, Trace :: {}", ex.getMessage(), ex.getStackTrace());
			throw ex;
		}
	}
	
	public String approve(Customer data) {
		ArrayList<Map<String, Object>> result = null;
		ArrayList<Map<String, Object>> account_id = null;
		String random;
		try {
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			stb = new StringBuilder();
			random = randomAlphaNumeric(10);

			if (data.getApprove_status()) {
				stb.append(
						" INSERT INTO account(account_no, customer_user_id, created_by, created_date, created_program, updated_by, updated_date, updated_program) ");
				stb.append(
						" VALUES (:account_no, :customer_user_id, 'SS-Pico-Finance', NOW(), 'SS-Pico-Finance','SS-Pico-Finance', NOW(), 'SS-Pico-Finance') ");

				MapSqlParameterSource parameters = new MapSqlParameterSource();
				parameters.addValue("account_no", random);
				parameters.addValue("customer_user_id", data.getCustomer_code());
				jdbcTemplate.update(stb.toString(), parameters);
				stb.setLength(0);
				stb.append(" UPDATE request_loan SET approve_status = :approve_status ");
				stb.append(" WHERE customer_user_id = :userId ");

				parameters.addValue("userId", data.getCustomer_code());
				parameters.addValue("approve_status", data.getApprove_status());
				jdbcTemplate.update(stb.toString(), parameters);
				stb.setLength(0);

				stb.append(" Select account_id From account");
				stb.append(" WHERE customer_user_id = :customer_user_id ");

				parameters.addValue("customer_user_id", data.getCustomer_code());
				account_id = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(stb.toString(), parameters);

				stb.setLength(0);
				stb.append(
						" INSERT INTO payment(account_id, status_id, payment_amount_paid, payment_period, payment_principle, ");
				stb.append(
						" payment_installment, payment_outstanding_balance, payment_pay_date_next, created_by, created_date, created_program, updated_by, updated_date, updated_program) ");
				stb.append(
						" VALUES (:account_id, :status_id, :payment_amount_paid, :payment_period, :payment_principle, :payment_installment, :payment_outstanding_balance ");
				stb.append(
						" ,to_timestamp(extract(year from now())::TEXT || '-' || lpad(extract(month from now() + INTERVAL '1 month')::text, 2, '0') || '-20', 'yyyy-mm-dd') ");
				stb.append(
						" ,'SS-Pico-Finance', NOW(), 'SS-Pico-Finance','SS-Pico-Finance', NOW(), 'SS-Pico-Finance')");

				parameters.addValue("account_id", account_id.get(0).get("account_id"));
				parameters.addValue("status_id", 1);
				parameters.addValue("payment_period", "1");
				parameters.addValue("payment_amount_paid", 11050);
				parameters.addValue("payment_principle", 10000);
				parameters.addValue("payment_installment", 1050);
				parameters.addValue("payment_outstanding_balance", 90000);

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
		System.out.println("customer_user_line_id ============ " + (String) result.get(0).get("customer_user_line_id"));
		return (String) result.get(0).get("customer_user_line_id");
	}
	
	public Customer approveWaitDoc(Customer data){
		log.info("<--Start approveWaitDoc.{}-->", data.getCustomer_user_id());
		Customer customer = new Customer();
		ArrayList<Map<String, Object>> result = null;
		String random = randomAlphaNumeric(10);
		
		StringBuilder sql = new StringBuilder();
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		
		try {
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		
		sql.append(
				" INSERT INTO account(account_no, customer_user_id, created_by, created_date, created_program, updated_by, updated_date, updated_program, account_credit, account_interest, account_period, status) ");
		sql.append(
				" VALUES (:account_no, :customer_user_id, 'SS-Pico-Finance', NOW(), 'SS-Pico-Finance','SS-Pico-Finance', NOW(), 'SS-Pico-Finance', :account_credit, :account_interest, :account_period, 2) ");

		parameters.addValue("account_no", random);
		parameters.addValue("customer_user_id", data.getCustomer_user_id());
		parameters.addValue("account_credit", data.getAccount_credit());
		parameters.addValue("account_interest", data.getAccount_interest());
		parameters.addValue("account_period", data.getAccount_period());
		jdbcTemplate.update(sql.toString(), parameters);
		
		StringBuilder sql2 = new StringBuilder();
		sql2.append(" SELECT cus.customer_first_name || ' ' || cus.customer_last_name AS customer_name, cus.customer_user_line_id, acc.* ");
		sql2.append(" FROM account acc ");
		sql2.append(" JOIN customer cus ON cus.customer_user_id = acc.customer_user_id ");
		sql2.append(" WHERE cus.customer_user_id = :customer_user_id ");

		MapSqlParameterSource parameters2 = new MapSqlParameterSource();
		parameters2.addValue("customer_user_id", data.getCustomer_code());
		customer = (Customer) jdbcTemplate.queryForMap(sql2.toString(), parameters2);
		} catch (EmptyResultDataAccessException ex) {
			log.error("Msg :: {}, Trace :: {}", ex.getMessage(), ex.getStackTrace());
		}
		return customer;
	}

}
