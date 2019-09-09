package com.pico.communication.dao;

import java.util.ArrayList;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.pico.communication.model.SendInvoice;
import com.pico.communication.utils.BeanUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class InvoiceDao {
	
	@Autowired
	private DataSource dataSource;
	private NamedParameterJdbcTemplate jdbcTemplate = null;
	
	public ArrayList<Map<String, Object>> queryInvoice(SendInvoice data) {
		log.info("Query Invoice Data");
		jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT lih.invoice_no, lct.line_user_id, lct.first_name, lct.last_name, lih.pdf_path, lct.email ");
		sql.append(" , lcp.period, lih.total_amount, lih.print_status ");
		sql.append(
				" , EXTRACT(DAY FROM lih.due_date) || ' ' || loan.TimeStampToThaiMonth(lih.due_date) || ' ' || loan.TimeStampToThaiYear(lih.due_date) AS due_date ");
		sql.append(" FROM loan.lo_invoice_head lih ");
		sql.append(" JOIN loan.lo_contract_period lcp ON lih.contract_period_id = lcp.contract_period_id ");
		sql.append(" JOIN loan.lo_contract_head lch ON lih.contract_head_id = lch.contract_head_id ");
		sql.append(" JOIN loan.lo_customer lct ON lct.customer_code = lch.customer_code ");
		sql.append(" WHERE 1=1 ");
		
		if (BeanUtils.isNotEmpty(data.getInvoiceNo())) {
			sql.append(" AND lih.invoice_no = :invoiceNo ");
		}
		
		if (BeanUtils.isNull(data.getInvoiceNo())) {
			sql.append(
					" AND lih.invoice_status = '1' AND lih.paid_status = '1' AND lih.print_status = '2' AND lih.email_status = '1' AND lih.line_status = '1' ");
		}

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("duedate", data.getDuedate());
		if (BeanUtils.isNotEmpty(data.getInvoiceNo())) {
			params.addValue("invoiceNo", data.getInvoiceNo());
		}
		
		return (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(sql.toString(), params);
	}
	
	public void updateStatusEmail(String invoiceNo) {
		log.info("Query Invoice Data");
		jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		StringBuilder sql = new StringBuilder();
		sql.append(" UPDATE loan.lo_invoice_head");
		sql.append(" SET email_status='2', updated_by= 'Communication-Service', updated_date = now() ");
		sql.append(" WHERE 1 = 1 ");
		
		if (BeanUtils.isNotEmpty(invoiceNo)) {
			sql.append(" AND invoice_no = :invoiceNo ");
		}
		
		if (BeanUtils.isNull(invoiceNo)) {
			sql.append(
					" AND invoice_status = '1' AND paid_status = '1' AND print_status = '2' AND email_status = '1' ");
		}

		MapSqlParameterSource params = new MapSqlParameterSource();
		if (BeanUtils.isNotEmpty(invoiceNo)) {
			params.addValue("invoiceNo", invoiceNo);
		}
		 jdbcTemplate.update(sql.toString(), params);
	}
	
	public void updateStatusLine(String invoiceNo) {
		log.info("Query Invoice Data");
		jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		StringBuilder sql = new StringBuilder();
		sql.append(" UPDATE loan.lo_invoice_head");
		sql.append(" SET line_status='2', updated_by= 'Communication-Service', updated_date = now() ");
		sql.append(" WHERE 1 = 1 ");
		
		if (BeanUtils.isNotEmpty(invoiceNo)) {
			sql.append(" AND invoice_no = :invoiceNo ");
		}
		
		if (BeanUtils.isNull(invoiceNo)) {
			sql.append(
					" AND invoice_status = '1' AND paid_status = '1' AND print_status = '2' AND line_status = '1' ");
		}

		MapSqlParameterSource params = new MapSqlParameterSource();
		if (BeanUtils.isNotEmpty(invoiceNo)) {
			params.addValue("invoiceNo", invoiceNo);
		}
		 jdbcTemplate.update(sql.toString(), params);
	}

}
