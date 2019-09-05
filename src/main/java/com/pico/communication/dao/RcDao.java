package com.pico.communication.dao;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.pico.communication.model.SendReceipt;
import com.pico.communication.utils.BeanUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RcDao {
	@Autowired
	private DataSource dataSource;
	private NamedParameterJdbcTemplate jdbcTemplate = null;
	
	public ArrayList<Map<String, Object>> queryReceipt(SendReceipt data) {
		log.info("Query Receipt Data");
		jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		StringBuilder sql = new StringBuilder();
		sql.append(
				" SELECT lrh.receipt_no, lrh.receipt_head_id ,lct.line_user_id, lct.first_name, lct.last_name, lrh.pdf_path, lct.email ");
		sql.append(" , lrh.receipt_total_amount  ");
		sql.append(" FROM loan.lo_receipt_head lrh  ");
		sql.append(" JOIN loan.lo_invoice_head lih ON lrh.ref_invoice_head_id = lih.invoice_head_id ");
		sql.append(" JOIN loan.lo_contract_head lch ON lih.contract_head_id = lch.contract_head_id  ");
		sql.append(" JOIN loan.lo_customer lct ON lct.customer_code = lch.customer_code  ");
		sql.append(" WHERE 1=1 ");
		sql.append(" AND lrh.receipt_no = :receiptNo  ");

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("receiptNo", data.getReceiptNo());
		return (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(sql.toString(), params);
	}
	
	public ArrayList<Map<String, Object>> queryLineReceipt(String receiptHeadId) {
		log.info("Query Receipt Data");
		jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		NumberFormat mf = NumberFormat.getInstance(new Locale("en", "US"));
		mf.setMaximumFractionDigits(2);
		StringBuilder sql3 = new StringBuilder();
		sql3.append(" SELECT receipt_description_tha, 'จำนวน ' || REPLACE(TO_CHAR(receipt_amount, '9,999,999.99'), ' ', '')|| '  บาท' AS receipt_amount ");
		sql3.append(" FROM loan.lo_receipt_detail ");
		sql3.append(" WHERE CAST(receipt_head_id AS VARCHAR) = :receipt_head_id ");

		MapSqlParameterSource parameter3 = new MapSqlParameterSource();
		parameter3.addValue("receipt_head_id", receiptHeadId);
		return (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(sql3.toString(),
				parameter3);
	}
	
	public void updateStatusEmail(String receiptNo) {
		log.info("Update StatusEmail Receipt Data");
		jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		StringBuilder sqlemail = new StringBuilder();
		sqlemail.append(" UPDATE loan.lo_receipt_head");
		sqlemail.append(" SET email_status='2', updated_by= 'Communication-Service', updated_date = now() ");
		sqlemail.append(" WHERE 1 = 1 ");
		sqlemail.append(" AND receipt_no = :receiptNo  ");

		MapSqlParameterSource paramsemail = new MapSqlParameterSource();
		paramsemail.addValue("receiptNo", receiptNo);
		jdbcTemplate.update(sqlemail.toString(), paramsemail);
	}
	
	public void updateStatusLine(String receiptNo) {
		log.info("Update StatusLine Receipt Data");
		StringBuilder sqllinn = new StringBuilder();
		sqllinn.append(" UPDATE loan.lo_receipt_head ");
		sqllinn.append(" SET line_status='2', updated_by= 'Communication-Service', updated_date = now() ");
		sqllinn.append(" WHERE 1 = 1 ");
		sqllinn.append(" AND receipt_no = :receiptNo  ");
		MapSqlParameterSource paramsline = new MapSqlParameterSource();
		paramsline.addValue("receiptNo", receiptNo);
		jdbcTemplate.update(sqllinn.toString(), paramsline);
	}

}
