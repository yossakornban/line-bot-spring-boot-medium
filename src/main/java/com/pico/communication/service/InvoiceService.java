package com.pico.communication.service;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
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
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.pico.communication.controller.LineBotController;
import com.pico.communication.model.EmailConfig;
import com.pico.communication.model.SendInvoice;
import com.pico.communication.utils.BeanUtils;
//import com.pico.linebot.repository.ApprovePaymentRepository;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Data
@Service

public class InvoiceService {

	@Autowired
	private LineBotController LineBotController;

	@Autowired
	private DataSource dataSource;
	private NamedParameterJdbcTemplate jdbcTemplate = null;

	private String PathReport = "http://pico.ssweb.ga:90/community";

	public void sendEmail(EmailConfig emailConfig, SendInvoice data) throws Exception {
		ArrayList<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

		try {

			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			StringBuilder sql = new StringBuilder();
			sql.append(
					" SELECT lih.invoice_no, lct.line_user_id, lct.first_name, lct.last_name, lih.pdf_path, lct.email ");
			sql.append(" , lid.PERIOD, lid.amount ");
			sql.append(
					" , EXTRACT(DAY FROM lih.due_date) || ' ' || loan.TimeStampToThaiMonth(lih.due_date) || ' ' || loan.TimeStampToThaiYear(lih.due_date) AS due_date ");
			sql.append(" FROM loan.lo_invoice_head lih ");
			sql.append(" JOIN loan.lo_invoice_datail lid ON lid.invoice_head_id = lih.invoice_head_id ");
			sql.append(" JOIN loan.lo_contract_head lch ON lih.contract_head_id = lch.contract_head_id ");
			sql.append(" JOIN loan.lo_customer lct ON lct.customer_code = lch.customer_code ");
			sql.append(
					" WHERE CAST(lih.due_date AS DATE) - 15 = COALESCE (CAST(now() AS DATE), CAST(:duedate AS DATE)) ");
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("duedate", data.getDuedate());
			result = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(sql.toString(), params);

			int i;
			int size = result.size();
			for (i = 0; i < size; i++) {

				if (data.getCommunicationType().equals("email") || data.getCommunicationType().equals("all")) {
					String email = result.get(i).get("email").toString();
					ArrayList<String> list = new ArrayList<String>() {
						{
							add(email);
//					add("tanitta9891@gmail.com");
//					add("aoninstop@gmail.com");
						}
					};
					emailConfig.setMailTos(list);
					System.out.println(emailConfig);
					Properties prop = new Properties();
					prop.put("mail.smtp.host", emailConfig.getHost());
					prop.put("mail.smtp.port", emailConfig.getPort());
					prop.put("mail.smtp.auth", "true");
					prop.put("mail.smtp.starttls.enable", "true"); // TLS

					Session session = Session.getInstance(prop, new javax.mail.Authenticator() {
						protected PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(emailConfig.getUser(), emailConfig.getPass());
						}
					});

					String mailToString = StringUtils.join(emailConfig.getMailTos(), ',');
					Message message = new MimeMessage(session);
					message.setFrom(new InternetAddress(emailConfig.getFrom()));
					message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailToString));
					message.setSubject(emailConfig.getSubject());

					// creates message part

					String messages = "<label>เรื่อง การจัดส่งใบแจ้งยอดบัญชี อิเล็กทรอนิกส์ </label> <br> "
							+ "<label>เรียน ท่านสมาชิก เพื่อนแท้ เงินด่วน</label> <br> "
							+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<label>ทางบริษัทฯ ได้แนบเอกสารในอีเมลฉบับนี้ ประกอบด้วย</label> <br> "
							+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;•  สำเนาใบแจ้งยอดบัญชี <br>"
							+ "<label>ท่านสามารถดูสำเนาใบแจ้งยอดบัญชีอิเล็กทรอนิกส์และเอกสารอื่นๆ โดยคลิกลิงค์ </label> <br> "
							+ PathReport + result.get(i).get("pdf_path").toString() + "<br><br><label>หมายเหตุ</label>"
							+ "<br><label>- จดหมายอิเล็กทรอนิกส์ฉบับนี้ เป็นการส่งจากระบบอัตโนมัติไม่สามารถตอบกลับได้ หากท่านต้องการติดต่อบริษัทฯ กรุณาติดต่อผ่านทาง  </label>"
							+ "<br> <label>Facebook : https://www.facebook.com/Pueantaeleasing</label>"
							+ "<br> <label>Website : http://www.pueantae-capital.com</label>"
							+ "<br> <label>สายด่วนโทร : 083-025-6646</label>" + "<br><br>"
							+ "<labe>ขอแสดงความนับถือ</label>" + "<br><labe>บริษัท เพื่อนแท้ แคปปิตอล จำกัด</label>";

					MimeBodyPart messageBodyPart = new MimeBodyPart();
					messageBodyPart.setContent(messages, "text/html; charset=utf-8");
					Multipart multipart = new MimeMultipart();
					multipart.addBodyPart(messageBodyPart);

//			if (BeanUtils.isNotEmpty(emailConfig.getAttachments())) {
//				for (File file : emailConfig.getAttachments()) {
//					addAttachment(multipart, file);
//				}
//			}
					message.setContent(multipart);
					Transport.send(message);
				} else if (data.getCommunicationType().equals("line") || data.getCommunicationType().equals("all")) {
					///// line

					NumberFormat mf = NumberFormat.getInstance(new Locale("en", "US"));
					mf.setMaximumFractionDigits(2);
					TextMessage tm = new TextMessage("เรียน คุณ " + result.get(i).get("first_name").toString() + " "
							+ result.get(i).get("last_name").toString() + "\n"
							+ "บริษัท เพื่อนแท้ แคปปิตอล จำกัด ขอแจ้งค่าเบี้ย ให้ท่านตามข้อมูลด้านล่าง \n" + "งวดที่: "
							+ result.get(i).get("period").toString() + "\n" + "ยอดชำระ: "
							+ mf.format(result.get(i).get("amount")) + " บาท\n" + "โปรดชำระเงินภายใน "
							+ result.get(i).get("due_date").toString());

					String originalContentUrl = "https://us-central1-poc-payment-functions.cloudfunctions.net/webApi/promptpay/0889920035/10.png";
					ImageMessage im = new ImageMessage(originalContentUrl, originalContentUrl);

					LineBotController.push(result.get(i).get("line_user_id").toString(), Arrays.asList(tm, im));
				}
			}
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

//	private static void addAttachment(Multipart multipart, File file) throws Exception {
//		DataSource source = new FileDataSource(file);
//		BodyPart messageBodyPart = new MimeBodyPart();
//		messageBodyPart.setDataHandler(new DataHandler(source));
//		messageBodyPart.setFileName(file.getName());
//		multipart.addBodyPart(messageBodyPart);
//	}
}
