package com.pico.communication.service;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.pico.communication.constant.SystemConstant;
import com.pico.communication.controller.LineBotController;
import com.pico.communication.dao.InvoiceDao;
import com.pico.communication.dao.RcDao;
import com.pico.communication.model.EmailConfig;
import com.pico.communication.model.SendInvoice;
import com.pico.communication.model.SendReceipt;
import com.pico.communication.utils.BeanUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class RcService {

	@Autowired
	private RcDao rcDao;

	@Autowired
	private LineBotController lineBotController;

	@Autowired
	private EmailService emailService;

	public void rcCommunication(SendReceipt data) throws Exception {
		ArrayList<Map<String, Object>> results = rcDao.queryReceipt(data);
		EmailConfig emailConfig = null;
		for (Map<String, Object> result : results) {

			if ("email".equals(data.getCommunicationType())) {
				if (BeanUtils.isNotNull(result.get("email"))) {
					sendMail(emailConfig, result, data);
				}
			}

			if ("line".equals(data.getCommunicationType())) {
				if (BeanUtils.isNotNull(result.get("line_user_id"))) {
					sendLine(rcDao.queryLineReceipt(result.get("receipt_head_id").toString()), result, data);
				}
			}

			if ("all".equals(data.getCommunicationType())) {
				if (BeanUtils.isNotNull(result.get("email"))) {
					sendMail(emailConfig, result, data);
				}
				if (BeanUtils.isNotNull(result.get("line_user_id"))) {
					sendLine(rcDao.queryLineReceipt(result.get("receipt_head_id").toString()), result, data);
				}

			}
		}
	}

	private void sendMail(EmailConfig config, Map<String, Object> query, SendReceipt data) throws Exception {
		config = new EmailConfig();
		config.setToEmail(query.get("email").toString());
		StringBuilder messageTextEmail = new StringBuilder();

		messageTextEmail.append("<label>เรื่อง การจัดส่งใบเสร็จรับเงิน อิเล็กทรอนิกส์ </label><br>");
		messageTextEmail.append("<label>เรียน ท่านสมาชิก เพื่อนแท้ เงินด่วน</label> <br> ");
		messageTextEmail.append(
				"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<label>ทางบริษัทฯ ได้แนบเอกสารในอีเมลฉบับนี้ ประกอบด้วย</label><br>");
		messageTextEmail.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;•  สำเนาใบเสร็จรับเงิน <br>");
		messageTextEmail
				.append("<label>ท่านสามารถดูสำเนาใบเสร็จรับเงินอิเล็กทรอนิกส์และเอกสารอื่นๆ   </label><a href=");
		messageTextEmail.append(" %s%s> โดยคลิกลิงค์</a> <br>");
		messageTextEmail.append("<br><br><label>หมายเหตุ</label>");
		messageTextEmail.append(
				"<br><label>- จดหมายอิเล็กทรอนิกส์ฉบับนี้ เป็นการส่งจากระบบอัตโนมัติไม่สามารถตอบกลับได้ หากท่านต้องการติดต่อบริษัทฯ กรุณาติดต่อผ่านทาง  </label>");
		messageTextEmail.append("<br> <label>Facebook : https://www.facebook.com/Pueantaeleasing</label>");
		messageTextEmail.append("<br> <label>Website : http://www.pueantae-capital.com</label>");
		messageTextEmail.append("<br> <label>สายด่วนโทร : 083-025-6646</label>" + "<br><br>");
		messageTextEmail.append("<labe>ขอแสดงความนับถือ</label>" + "<br><labe>บริษัท เพื่อนแท้ แคปปิตอล จำกัด</label>");
		config.setMessageText(String.format(messageTextEmail.toString(), SystemConstant.PATHREPORT,
				query.get("pdf_path").toString()));
		emailService.SendEmailService(config);
		rcDao.updateStatusEmail(data.getReceiptNo());
	}

	private void sendLine(ArrayList<Map<String, Object>> querys, Map<String, Object> result, SendReceipt data)
			throws Exception {
		StringBuilder messageTextLine = new StringBuilder();
		NumberFormat mf = NumberFormat.getInstance(new Locale("en", "US"));
		mf.setMaximumFractionDigits(2);
		messageTextLine.append("บริษัท เพื่อนแท้ แคปปิตอล จำกัด ได้รับการชำระเงินเรียบร้อยแล้ว โดยมีรายละเอียด ดังนี้");
		for (Map<String, Object> query : querys) {
			messageTextLine.append(query.get("receipt_description_tha") + " \n");
			messageTextLine.append(query.get("receipt_amount").toString() + " \n");
			//
		}

		lineBotController.push(result.get("line_user_id").toString(),
				Arrays.asList(new TextMessage(messageTextLine.toString())));

		rcDao.updateStatusLine(data.getReceiptNo());
	}

}
