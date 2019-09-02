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
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.pico.communication.controller.LineBotController;
import com.pico.communication.model.EmailConfig;
import com.pico.communication.model.SendReceipt;
import com.pico.communication.utils.BeanUtils;
//import com.pico.linebot.repository.ApprovePaymentRepository;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Data
@Service

public class ReceiptService {

	@Autowired
	private LineBotController LineBotController;

	@Autowired
	private EmailService EmailService;
	
	@Autowired
	private DataSource dataSource;
	private NamedParameterJdbcTemplate jdbcTemplate = null;

	private String PathReport = "https://picos.ssweb.ga/community";

	public void sendEmail(EmailConfig emailConfig, SendReceipt data) throws Exception {
		ArrayList<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		ArrayList<Map<String, Object>> resultline = new ArrayList<Map<String, Object>>();
		StringBuilder messageTextEmail = new StringBuilder();
		StringBuilder messageTextLine = new StringBuilder();
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
		System.out.println(sql.toString());
		result = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(sql.toString(), params);

		int i;
		int size = result.size();
		for (i = 0; i < size; i++) {
			emailConfig = new EmailConfig();
			if (data.getCommunicationType().equals("email") || data.getCommunicationType().equals("all")) {

				log.info("Send Mail Receipt");
				emailConfig.setToEmail(result.get(i).get("email").toString());

				messageTextEmail.append( "<label>เรื่อง การจัดส่งใบเสร็จรับเงิน อิเล็กทรอนิกส์ </label> <br> ");
				messageTextEmail.append( "<label>เรียน ท่านสมาชิก เพื่อนแท้ เงินด่วน</label> <br> ");
				messageTextEmail.append( "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<label>ทางบริษัทฯ ได้แนบเอกสารในอีเมลฉบับนี้ ประกอบด้วย</label> <br> ");
				messageTextEmail.append( "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;•  สำเนาใบเสร็จรับเงิน <br>");
				messageTextEmail.append( "<label>ท่านสามารถดูสำเนาใบเสร็จรับเงินอิเล็กทรอนิกส์และเอกสารอื่นๆ   </label><a href="+ PathReport + result.get(i).get("pdf_path").toString() + "> โดยคลิกลิงค์</a> <br> ");
				messageTextEmail.append( "<br><br><label>หมายเหตุ</label>");
				messageTextEmail.append( "<br><label>- จดหมายอิเล็กทรอนิกส์ฉบับนี้ เป็นการส่งจากระบบอัตโนมัติไม่สามารถตอบกลับได้ หากท่านต้องการติดต่อบริษัทฯ กรุณาติดต่อผ่านทาง  </label>");
				messageTextEmail.append( "<br> <label>Facebook : https://www.facebook.com/Pueantaeleasing</label>");
				messageTextEmail.append( "<br> <label>Website : http://www.pueantae-capital.com</label>");
				messageTextEmail.append( "<br> <label>สายด่วนโทร : 083-025-6646</label>" + "<br><br>");
				messageTextEmail.append( "<labe>ขอแสดงความนับถือ</label>" + "<br><labe>บริษัท เพื่อนแท้ แคปปิตอล จำกัด</label>");
				
//				String messages = "<label>เรื่อง การจัดส่งใบเสร็จรับเงิน อิเล็กทรอนิกส์ </label> <br> "
//						+ "<label>เรียน ท่านสมาชิก เพื่อนแท้ เงินด่วน</label> <br> "
//						+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<label>ทางบริษัทฯ ได้แนบเอกสารในอีเมลฉบับนี้ ประกอบด้วย</label> <br> "
//						+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;•  สำเนาใบเสร็จรับเงิน <br>"
//						+ "<label>ท่านสามารถดูสำเนาใบเสร็จรับเงินอิเล็กทรอนิกส์และเอกสารอื่นๆ   </label><a href="
//						+ PathReport + result.get(i).get("pdf_path").toString() + "> โดยคลิกลิงค์</a> <br> "
//						+ "<br><br><label>หมายเหตุ</label>"
//						+ "<br><label>- จดหมายอิเล็กทรอนิกส์ฉบับนี้ เป็นการส่งจากระบบอัตโนมัติไม่สามารถตอบกลับได้ หากท่านต้องการติดต่อบริษัทฯ กรุณาติดต่อผ่านทาง  </label>"
//						+ "<br> <label>Facebook : https://www.facebook.com/Pueantaeleasing</label>"
//						+ "<br> <label>Website : http://www.pueantae-capital.com</label>"
//						+ "<br> <label>สายด่วนโทร : 083-025-6646</label>" + "<br><br>"
//						+ "<labe>ขอแสดงความนับถือ</label>" + "<br><labe>บริษัท เพื่อนแท้ แคปปิตอล จำกัด</label>";

				emailConfig.setMessageText(messageTextEmail.toString());
				EmailService.SendEmailService(emailConfig);

				jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
				StringBuilder sqlemail = new StringBuilder();
				sqlemail.append(" UPDATE loan.lo_receipt_head");
				sqlemail.append(" SET email_status='2', updated_by= 'Communication-Service', updated_date = now() ");
				sqlemail.append(" WHERE 1 = 1 ");
				sqlemail.append(" AND receipt_no = :receiptNo  ");

				MapSqlParameterSource paramsemail = new MapSqlParameterSource();
				paramsemail.addValue("receiptNo", data.getReceiptNo());
				jdbcTemplate.update(sqlemail.toString(), paramsemail);
				if (data.getCommunicationType().equals("line") || data.getCommunicationType().equals("all")) {
					log.info("Send Message Line Receipt");
					NumberFormat mf = NumberFormat.getInstance(new Locale("en", "US"));
					mf.setMaximumFractionDigits(2);
					StringBuilder sql3 = new StringBuilder();
					sql3.append(" SELECT description, receipt_amount ");
					sql3.append(" FROM loan.lo_receipt_detail ");
					sql3.append(" WHERE receipt_head_id = :receipt_head_id ");

					MapSqlParameterSource parameter3 = new MapSqlParameterSource();
					parameter3.addValue("receipt_head_id", result.get(i).get("receipt_head_id"));
					resultline = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(sql3.toString(),
							parameter3);

					int x;
					int sizeDetail = resultline.size();
					String detail = "";
					for (x = 0; x < sizeDetail; x++) {
						messageTextLine.append((String) resultline.get(x).get("description") + " จำนวน ");
//						detail += (String) resultline.get(x).get("description") + " จำนวน ";
//								+ mf.format(result.get(x).get("receipt_amount")) + " บาท \n";
					}
					StringBuilder sqllinn = new StringBuilder();
					sqllinn.append(" UPDATE loan.lo_receipt_head ");
					sqllinn.append(" SET line_status='2', updated_by= 'Communication-Service', updated_date = now() ");
					sqllinn.append(" WHERE 1 = 1 ");
					sqllinn.append(" AND receipt_no = :receiptNo  ");
					MapSqlParameterSource paramsline = new MapSqlParameterSource();
					paramsline.addValue("receiptNo", data.getReceiptNo());
					jdbcTemplate.update(sqllinn.toString(), paramsline);

					LineBotController.push((String) result.get(i).get("line_user_id"),
							Arrays.asList(new TextMessage(messageTextLine.toString())));

				}

			}
////			if (BeanUtils.isNotEmpty(emailConfig.getAttachments())) {
////				for (File file : emailConfig.getAttachments()) {
////					addAttachment(multipart, file);
////				}
////			}
//					message.setContent(multipart);
//					Transport.send(message);
//				}
//				if (data.getCommunicationType().equals("line") || data.getCommunicationType().equals("all")) {
//					///// line
//
//					NumberFormat mf = NumberFormat.getInstance(new Locale("en", "US"));
//					mf.setMaximumFractionDigits(2);
//					StringBuilder sql3 = new StringBuilder();
//					sql3.append(" SELECT description, receipt_amount ");
//					sql3.append(" FROM loan.lo_receipt_detail ");
//					sql3.append(" WHERE receipt_head_id = :receipt_head_id ");
//
//					MapSqlParameterSource parameter3 = new MapSqlParameterSource();
//					parameter3.addValue("receipt_head_id", result.get(i).get("receipt_head_id"));
//					resultline = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(sql3.toString(),
//							parameter3);
//
//					int x;
//					int sizeDetail = resultline.size();
//					String detail = "";
//					for (x = 0; x < sizeDetail; x++) {
//						detail += (String) resultline.get(x).get("description") + " จำนวน ";
////								+ mf.format(result.get(x).get("receipt_amount")) + " บาท \n";
//					}
//					StringBuilder sqllinn = new StringBuilder();
//					sqllinn.append(" UPDATE loan.lo_receipt_head ");
//					sqllinn.append(" SET line_status='2', updated_by= 'Communication-Service', updated_date = now() ");
//					sqllinn.append(" WHERE 1 = 1 ");
//					sqllinn.append(" AND receipt_no = :receiptNo  ");
//					MapSqlParameterSource paramsline = new MapSqlParameterSource();
//					paramsline.addValue("receiptNo", data.getReceiptNo());
//					jdbcTemplate.update(sqllinn.toString(), paramsline);
//
//					LineBotController.push((String) result.get(i).get("line_user_id"),
//							Arrays.asList(new TextMessage(detail)));
//
//				}
//			}
//		} catch (MessagingException e) {
//			e.printStackTrace();
//		}
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
