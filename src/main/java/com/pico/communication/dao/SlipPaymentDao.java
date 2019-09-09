package com.pico.communication.dao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.linecorp.bot.client.MessageContentResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SlipPaymentDao {

	@Autowired
	private DataSource dataSource;
	private NamedParameterJdbcTemplate jdbcTemplate = null;
	private File txtFile = null;
	private FileOutputStream fop = null;
	private final String path = System.getProperty("catalina.base") + "/webapps/ROOT/receive/";

	public void saveSlipPayment(byte[] content, String userId) throws Exception {
		ArrayList<Map<String, Object>> result = null;
		ArrayList<Map<String, Object>> result1 = null;
		try {
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			StringBuilder stb = new StringBuilder();
			stb.append(" SELECT lch.contract_head_id ");
			stb.append(" FROM lo_customer lct ");
			stb.append(" JOIN lo_contract_head lch ON lct.customer_code = lch.customer_code  ");
			stb.append(" WHERE lct.line_user_id = :lineId ");

			MapSqlParameterSource parameters = new MapSqlParameterSource();
			parameters.addValue("lineId", userId);
			result = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(stb.toString(), parameters);

			String fileName = saveSlipPaymentToServer(content, result.get(0).get("contract_head_id").toString());

			stb.setLength(0);

			stb.append(" INSERT INTO loan.lo_payment_slip");
			stb.append(
					" (contract_head_id, slip_image, created_by, created_date, created_program, updated_by, updated_date, updated_program) ");
			stb.append(
					" VALUES(:contract_head_id, :slip, 'Communication-Service', now(),  'Communication-Service', 'Communication-Service', now(), 'Communication-Service') ");

			parameters.addValue("contract_head_id", result.get(0).get("contract_head_id"));
			parameters.addValue("slip", fileName);
			jdbcTemplate.update(stb.toString(), parameters);

		} catch (EmptyResultDataAccessException ex) {
			log.error("Msg :: {}, Trace :: {}", ex.getMessage(), ex.getStackTrace());
		}
	}

	public String saveSlipPaymentToServer(byte[] content, String headId) throws Exception {
		String fileName = headId + System.currentTimeMillis() + "." + "jpg";
		try {

			txtFile = new File(path + fileName);
			fop = new FileOutputStream(txtFile, true);
			txtFile.createNewFile();
			txtFile.setReadable(true, false);
			fop.write(content);
			if (fop != null) {
				fop.flush();
				fop.close();
			}
			return fileName;

		} catch (Exception ex) {
			log.error("Msg :: {}, Trace :: {}", ex.getMessage(), ex.getStackTrace());
		}
		return fileName;
	}

}
