package com.pico.communication.service;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.pico.communication.controller.LineBotController;
import com.pico.communication.model.EmailConfig;

//import com.pico.linebot.repository.ApprovePaymentRepository;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Data
@Service
public class EmailService {

	@Autowired
	private DataSource dataSource;

	@Autowired
	private LineBotController LineBotController;

	private NamedParameterJdbcTemplate jdbcTemplate = null;

	public void SendEmailService(EmailConfig emailConfig) {
		try {
			log.info("Prepare to send mail");
			log.info("send mail -------------- " + emailConfig.toString());

			// For set email target.

			// For set property(host, port).
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

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(emailConfig.getFrom()));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailConfig.getToEmail()));
			message.setSubject(emailConfig.getSubject());

			MimeBodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(emailConfig.getMessageText(), "text/html; charset=utf-8");
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);

			message.setContent(multipart);
			Transport.send(message);

		} catch (Exception e) {
			log.info(e.getMessage());
		}

	}
}
