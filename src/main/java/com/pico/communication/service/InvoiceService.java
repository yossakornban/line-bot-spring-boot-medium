package com.pico.communication.service;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.pico.communication.constant.SystemConstant;
import com.pico.communication.controller.LineBotController;
import com.pico.communication.dao.InvoiceDao;
import com.pico.communication.model.EmailConfig;
import com.pico.communication.model.SendInvoice;
import com.pico.communication.utils.BeanUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class InvoiceService {

	@Autowired
	private InvoiceDao ivDao;

	@Autowired
	private LineBotController lineBotController;

	@Autowired
	private EmailService emailService;

	public void ivCommunication(SendInvoice data) throws Exception {
		ArrayList<Map<String, Object>> results = ivDao.queryInvoice(data);
		log.info("---------------- " + results.toString());
		EmailConfig emailConfig = null;
		for (Map<String, Object> result : results) {

			if (data.getCommunicationType().equals("email")) {
				if (BeanUtils.isNotNull(result.get("email"))) {
					sendMail(emailConfig, result, data);
				}
			}

			if (data.getCommunicationType().equals("line")) {
				if (BeanUtils.isNotNull(result.get("line_user_id"))) {
					sendLine(result, data);
				}
			}

			if (data.getCommunicationType().equals("all")) {
				log.info("Email------------- " + BeanUtils.isNotNull(result.get("email")));
				if (BeanUtils.isNotNull(result.get("email"))) {
					sendMail(emailConfig, result, data);
				}
				log.info("Line------------- " + BeanUtils.isNotNull(result.get("line_user_id")));
				if (BeanUtils.isNotNull(result.get("line_user_id"))) {
					sendLine(result, data);
				}

			}
		}
	}

	private void sendMail(EmailConfig config, Map<String, Object> query, SendInvoice data) throws Exception {
		config = new EmailConfig();
		config.setToEmail(query.get("email").toString());
		StringBuilder messageTextEmail = null;
		messageTextEmail = new StringBuilder();

		messageTextEmail.append("<label>เรื่อง การจัดส่งใบแจ้งยอดบัญชี อิเล็กทรอนิกส์ </label><br>");
		messageTextEmail.append("<label>เรียน ท่านสมาชิก เพื่อนแท้ เงินด่วน</label> <br> ");
		messageTextEmail.append(
				"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<label>ทางบริษัทฯ ได้แนบเอกสารในอีเมลฉบับนี้ ประกอบด้วย</label><br>");
		messageTextEmail.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;•  สำเนาใบแจ้งยอดบัญชี <br>");
		messageTextEmail
				.append("<label>ท่านสามารถดูสำเนาใบแจ้งยอดบัญชีอิเล็กทรอนิกส์และเอกสารอื่นๆ   </label><a href=");
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
		ivDao.updateStatusEmail(data.getInvoiceNo());
	}

	private void sendLine(Map<String, Object> query, SendInvoice data) throws Exception {
		StringBuilder messageTextLine = new StringBuilder();
		NumberFormat mf = NumberFormat.getInstance(new Locale("en", "US"));
		mf.setMaximumFractionDigits(2);
		messageTextLine.append("เรียน คุณ %s ");
		messageTextLine.append("%s \n");
		messageTextLine.append("บริษัท เพื่อนแท้ แคปปิตอล จำกัด ขอแจ้งค่าเบี้ย ให้ท่านตามข้อมูลด้านล่าง \n");
		messageTextLine.append("งวดที่: %s" + "\n" + "ยอดชำระ: %s บาท\n");
		messageTextLine.append("โปรดชำระเงินภายใน  %s");

		String originalContentUrl = "https://us-central1-poc-payment-functions.cloudfunctions.net/webApi/promptpay/0889920035/10.png";
		ImageMessage im = new ImageMessage(originalContentUrl, originalContentUrl);
		lineBotController.push(query.get("line_user_id").toString(),
				Arrays.asList(
						new TextMessage(String.format(messageTextLine.toString(), query.get("first_name").toString(),
								query.get("last_name").toString(), query.get("period").toString(),
								mf.format(query.get("total_amount")), query.get("due_date").toString())),
						im));

		ivDao.updateStatusLine(data.getInvoiceNo());
	}

}
