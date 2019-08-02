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
public class SlipPaymentRepository {

	@Autowired
	private DataSource dataSource;
	private NamedParameterJdbcTemplate jdbcTemplate = null;
	private StringBuilder stb = null;

	public void saveSlipPayment(String UserID, String encoded) {
		ArrayList<Map<String, Object>> result = null;
		try {
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			stb = new StringBuilder();
			stb.append(" SELECT acc.account_id ");
			stb.append(" FROM customer cus ");
			stb.append(" JOIN account acc ON acc.customer_user_id = cus.customer_user_id ");
			stb.append(" WHERE cus.customer_user_line_id = :lineId ");

			MapSqlParameterSource parameters = new MapSqlParameterSource();
			parameters.addValue("lineId",UserID);
			result = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(stb.toString(), parameters);

			stb.setLength(0);
			stb.append(" INSERT INTO slip_payment( account_id, slip)");
			stb.append(" VALUES ( :account_id, :slip) ");

			parameters.addValue("account_id", null);
			parameters.addValue("slip", "image/jpeg;base64,"+encoded);
			jdbcTemplate.update(stb.toString(), parameters);

		} catch (EmptyResultDataAccessException ex) {
			log.error("Msg :: {}, Trace :: {}", ex.getMessage(), ex.getStackTrace());
		}
	}

}