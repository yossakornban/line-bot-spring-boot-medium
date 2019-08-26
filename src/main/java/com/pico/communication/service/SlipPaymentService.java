package com.pico.communication.service;

import java.util.ArrayList;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
//import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.pico.communication.model.UserLog;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Service
public class SlipPaymentService {

	@Autowired
	private DataSource dataSource;
	private NamedParameterJdbcTemplate jdbcTemplate = null;
	private StringBuilder stb = null;
	private StringBuilder stb1 = null;

	public void saveSlipPayment(String UserID, String encoded) {
		ArrayList<Map<String, Object>> result = null;
		ArrayList<Map<String, Object>> result1 = null;
		try {
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			stb = new StringBuilder();
			stb.append(" SELECT lch.contract_head_id ");
			stb.append(" FROM lo_customer lct ");
			stb.append(" JOIN lo_contract_head lch ON lct.customer_code = lch.customer_code  ");
			stb.append(" WHERE lct.line_user_id = :lineId ");

			MapSqlParameterSource parameters = new MapSqlParameterSource();
			parameters.addValue("lineId", UserID);
			result = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(stb.toString(), parameters);

			stb.setLength(0);

			stb.append(" INSERT INTO loan.lo_payment_slip");
			stb.append(" (contract_head_id, lo_slip_image, create_by, create_date, create_program, update_by, update_date, update_program) ");
			stb.append(" VALUES(:contract_head_id, :slip, 'Communication-Service', now(),  'Communication-Service', 'Communication-Service', now(), 'Communication-Service') ");
			
			parameters.addValue("contract_head_id", result.get(0).get("contract_head_id"));
			parameters.addValue("slip", "data:image/jpeg;base64," + encoded);
			jdbcTemplate.update(stb.toString(), parameters);

		} catch (EmptyResultDataAccessException ex) {
			log.error("Msg :: {}, Trace :: {}", ex.getMessage(), ex.getStackTrace());
		}
	}

}