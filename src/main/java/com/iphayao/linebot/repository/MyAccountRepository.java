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
public class MyAccountRepository {

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

	public ArrayList<Map<String, Object>> searchHis(UserLog userLog) {
		ArrayList<Map<String, Object>> result = null;
		try {
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			stb = new StringBuilder();

			stb.append(" SELECT pay.payment_period, pay.payment_amount_paid ");
			stb.append(" , pay.payment_installment, to_char(pay.payment_pay_date, 'dd/MM/yyyy') AS payment_pay_date ");
			stb.append(" , pay.payment_outstanding_balance ");
			stb.append(" , to_char(pay.payment_due_date, 'dd/MM/yyyy') AS payment_pay_date_next ");
			stb.append(" , acc.account_credit ");
			stb.append(" , CAST(payment_amount_paid AS NUMERIC) + CAST(payment_installment AS NUMERIC) AS total ");
			stb.append(" FROM customer cus ");
			stb.append(" JOIN account acc ON acc.customer_user_id = cus.customer_user_id ");
			stb.append(" JOIN payment pay ON pay.account_id = acc.account_id ");
			stb.append(" WHERE cus.customer_user_line_id = :lineId ");
			stb.append("  AND pay.status_id = 6 ");
			stb.append(" AND pay.payment_period >= (SELECT max(payment.payment_period) FROM payment WHERE payment.account_id = acc.account_id) - 2 ");

			MapSqlParameterSource parameters = new MapSqlParameterSource();
			parameters.addValue("lineId", userLog.getUserID());

			result = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(stb.toString(), parameters);
		} catch (EmptyResultDataAccessException ex) {
			log.error("Msg :: {}, Trace :: {}", ex.getMessage(), ex.getStackTrace());
		}
		System.out.println(result);
		return result;
	}

	public ArrayList<Map<String, Object>> searchPaid(UserLog userLog) {
		ArrayList<Map<String, Object>> result = null;
		try {
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			stb = new StringBuilder();

			stb.append(" SELECT pay.payment_period, pay.payment_amount_paid ");
			stb.append(" , pay.payment_installment, to_char(pay.payment_pay_date, 'dd/MM/yyyy') AS payment_pay_date ");
			stb.append(" , pay.payment_outstanding_balance ");
			stb.append(" , to_char(pay.payment_due_date, 'dd/MM/yyyy') AS payment_pay_date_next ");
			stb.append(" , acc.account_credit ");
			stb.append(" , CAST(payment_amount_paid AS NUMERIC) + CAST(payment_installment AS NUMERIC) AS total ");
			stb.append(" , cus.customer_first_name, cus.customer_last_name ");
			stb.append(" FROM customer cus ");
			stb.append(" JOIN account acc ON acc.customer_user_id = cus.customer_user_id ");
			stb.append(" JOIN payment pay ON pay.account_id = acc.account_id ");
			stb.append(" WHERE cus.customer_user_line_id = :lineId ");
			stb.append(" AND pay.status_id = 7 ");
			stb.append(" ORDER BY ASC pay.payment_due_date ");

			MapSqlParameterSource parameters = new MapSqlParameterSource();
			parameters.addValue("lineId", userLog.getUserID());

			result = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(stb.toString(), parameters);
		} catch (EmptyResultDataAccessException ex) {
			log.error("Msg :: {}, Trace :: {}", ex.getMessage(), ex.getStackTrace());
		}
		System.out.println(result);
		return result;
	}

}
