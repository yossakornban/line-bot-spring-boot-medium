package com.pico.communication.service;

import java.util.Arrays;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
//import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.linecorp.bot.model.message.TextMessage;
import com.pico.communication.controller.LineBotController;
import com.pico.communication.dao.SlipPaymentDao;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class SlipPaymentService {

	@Autowired
	private DataSource dataSource;
	
	@Autowired
	private SlipPaymentDao slipPaymentDao;
	
	@Autowired
	private LineBotController lineBotController;
	
	private NamedParameterJdbcTemplate jdbcTemplate = null;
	private StringBuilder stb = null;
	private StringBuilder stb1 = null;
	
	public void slipPayment(byte[] content, String userId) throws Exception {
		slipPaymentDao.saveSlipPayment(content,userId);
		sendLine(userId);
	}
	
	public void sendLine(String userId) throws Exception {
		lineBotController.push(userId, Arrays.asList(new TextMessage("ระบบได้รับหลักฐานการชำระเงินของท่าน เรียบร้อยแล้ว กรุณารอการตอบกลับ จากทางเจ้าหน้าที่")));
	}

}