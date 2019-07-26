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

	public ArrayList<Map<String, Object>> searchMyAccount(UserLog userLog) {
		ArrayList<Map<String, Object>> result = null;
		try {
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			stb = new StringBuilder();

			stb.append(" SELECT pay.payment_period, pay.payment_amount_paid, pay.payment_principle, ");
			stb.append(
					" pay.payment_installment, to_char(pay.payment_pay_date, 'dd/MM/yyyy') AS payment_pay_date, pay.payment_outstanding_balance, to_char(pay.payment_pay_date_next, 'dd/MM/yyyy') AS payment_pay_date_next ");
			stb.append(" FROM customer cus ");
			stb.append(" JOIN account acc ON acc.customer_user_id = cus.customer_user_id ");
			stb.append(" JOIN payment pay ON pay.account_id = acc.account_id ");
			stb.append(" WHERE cus.customer_user_line_id = :lineId ");

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
