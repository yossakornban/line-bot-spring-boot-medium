package com.pico.communication.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pico.communication.model.EmailConfig;
import com.pico.communication.utils.BeanUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EmailService {

	public void sendEmail(EmailConfig emailConfig) throws Exception {
		ArrayList<String> list = new ArrayList<String>() {
			{
				add("yossakornban@hotmail.com");
				add("tanitta9891@gmail.com");
				add("aoninstop@gmail.com");
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

		try {

			String mailToString = StringUtils.join(emailConfig.getMailTos(), ',');
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(emailConfig.getFrom()));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailToString));
			message.setSubject(emailConfig.getSubject());

			// creates message part
			MimeBodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(emailConfig.getMessageText(), "text/html");
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);

			if (BeanUtils.isNotEmpty(emailConfig.getAttachments())) {
				for (File file : emailConfig.getAttachments()) {
					addAttachment(multipart, file);
				}
			}

			message.setContent(multipart);
			Transport.send(message);

			log.info("Send Email to {} Done", mailToString);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	private static void addAttachment(Multipart multipart, File file) throws Exception {
		DataSource source = new FileDataSource(file);
		BodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setDataHandler(new DataHandler(source));
		messageBodyPart.setFileName(file.getName());
		multipart.addBodyPart(messageBodyPart);
	}
}
