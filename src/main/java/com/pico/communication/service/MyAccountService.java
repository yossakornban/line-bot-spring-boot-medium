package com.pico.communication.service;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
//import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.pico.communication.controller.LineBotController;
import com.pico.communication.model.UserLog;
import com.pico.communication.utils.BeanUtils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Service
public class MyAccountService {

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
	
	@Autowired
	private LineBotController LineBotController;

	public ArrayList<Map<String, Object>> searchHis(UserLog userLog) {
		ArrayList<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		ArrayList<Map<String, Object>> receipt_head_id = new ArrayList<Map<String, Object>>();
		ArrayList<Map<String, Object>> contract_head_id = new ArrayList<Map<String, Object>>();
		try {

			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			StringBuilder sql1 = new StringBuilder();
			StringBuilder sql2 = new StringBuilder();
			StringBuilder sql3 = new StringBuilder();

			sql1 = new StringBuilder();
			sql1.append(" SELECT lch.contract_head_id ");
			sql1.append(" FROM loan.lo_customer lct ");
			sql1.append(" JOIN loan.lo_contract_head lch ON lct.customer_code = lch.customer_code  ");
			sql1.append(" WHERE lct.line_user_id = :lineId ");

			MapSqlParameterSource parameter1 = new MapSqlParameterSource();
			parameter1.addValue("lineId", userLog.getUserID());
			contract_head_id = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(sql1.toString(), parameter1);

			sql2 = new StringBuilder();
			sql2.append(" SELECT lrh.receipt_head_id ");
			sql2.append(" FROM loan.lo_receipt_head lrh ");
			sql2.append(" JOIN loan.lo_invoice_head lih ON lih.invoice_head_id = lrh.ref_invoice_head_id  ");
			sql2.append(" WHERE lih.contract_head_id = :contract_head_id ");

			MapSqlParameterSource parameter2 = new MapSqlParameterSource();
			parameter2.addValue("contract_head_id", contract_head_id.get(0).get("contract_head_id"));
			receipt_head_id = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(sql2.toString(), parameter2);

			NumberFormat mf = NumberFormat.getInstance(new Locale("en", "US"));
			mf.setMaximumFractionDigits(2);
			int i;
			int size = receipt_head_id.size();
			System.out.println("aaaaaaaaaaaaaaaaaaaa "+size);
			if (size > 0) {
				for (i = 0; i < size; i++) {

					sql3.append(" SELECT receipt_description_tha, 'จำนวน ' || REPLACE(TO_CHAR(receipt_amount, '9,999,999.99'), ' ', '')|| '  บาท' AS receipt_amount ");
					sql3.append(" FROM loan.lo_receipt_detail ");
					sql3.append(" WHERE receipt_head_id = :receipt_head_id ");

					MapSqlParameterSource parameter3 = new MapSqlParameterSource();
					parameter3.addValue("receipt_head_id", receipt_head_id.get(0).get("receipt_head_id"));
					result = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(sql3.toString(), parameter3);

					int x;
					int sizeDetail = result.size();
					String detail = "";
					detail += "บริษัท เพื่อนแท้ แคปปิตอล จำกัด ขอแจ้งประวัติการชำระเงิน ดังนี้\n";
					for (x = 0; x < sizeDetail; x++) {
						detail += (String) result.get(x).get("receipt_description_tha") + " \n"
								+ result.get(x).get("receipt_amount") + "\n";
					}

					LineBotController.push(userLog.getUserID(), Arrays.asList(new TextMessage(detail)));

				}
			} else {
				LineBotController.push(userLog.getUserID(), Arrays.asList(new TextMessage("ไม่มีประวัติการชำระ")));
			}

		} catch (EmptyResultDataAccessException e) {
			e.printStackTrace();
		}
		return result;
	}

	public ArrayList<Map<String, Object>> searchPaid(UserLog userLog) {
		ArrayList<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		ArrayList<Map<String, Object>> contract_head_id = new ArrayList<Map<String, Object>>();
		try {

			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			StringBuilder sql = new StringBuilder();
			StringBuilder stb = new StringBuilder();

			stb = new StringBuilder();
			stb.append(" SELECT lch.contract_head_id ");
			stb.append(" FROM loan.lo_customer lct ");
			stb.append(" JOIN loan.lo_contract_head lch ON lct.customer_code = lch.customer_code  ");
			stb.append(" WHERE lct.line_user_id = :lineId ");

			MapSqlParameterSource parameters = new MapSqlParameterSource();
			parameters.addValue("lineId", userLog.getUserID());
			contract_head_id = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(stb.toString(), parameters);

			sql.append(
					" SELECT lih.invoice_no, lct.line_user_id, lct.first_name, lct.last_name, lih.pdf_path, lct.email ");
			sql.append(" , lcp.period, lih.total_amount ");
			sql.append(
					" , EXTRACT(DAY FROM lih.due_date) || ' ' || loan.TimeStampToThaiMonth(lih.due_date) || ' ' || loan.TimeStampToThaiYear(lih.due_date) AS due_date ");
			sql.append(" FROM loan.lo_invoice_head lih ");
			sql.append(" JOIN loan.lo_contract_period lcp ON lih.contract_period_id = lcp.contract_period_id ");
			sql.append(" JOIN loan.lo_contract_head lch ON lih.contract_head_id = lch.contract_head_id ");
			sql.append(" JOIN loan.lo_customer lct ON lct.customer_code = lch.customer_code ");
			sql.append(" WHERE 1=1 ");
			sql.append(" AND lih.invoice_status = '1' ");
			sql.append(" AND lih.contract_head_id = :contract_head_id ");

			MapSqlParameterSource params = new MapSqlParameterSource();
			if(!contract_head_id.isEmpty()) {
				params.addValue("contract_head_id", contract_head_id.get(0).get("contract_head_id"));
				result = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(sql.toString(), params);
			}else {
				result = null;
			}

		} catch (EmptyResultDataAccessException e) {
			e.printStackTrace();
		}
		return result;
	}

}
