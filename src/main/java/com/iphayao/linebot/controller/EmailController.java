package com.iphayao.linebot.controller;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iphayao.linebot.helper.RichMenuHelper;
import com.iphayao.linebot.model.Customer;
import com.iphayao.linebot.model.EmailConfig;
import com.iphayao.linebot.repository.ApprovePaymentRepository;
import com.iphayao.linebot.repository.ApproveRepository;
import com.iphayao.linebot.repository.EmailService;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping(path = "/send")

public class EmailController {

	@Autowired
	private EmailService emailService;

	@GetMapping("/email")
	public void line03Search() throws Exception {
		try {
			EmailConfig emailConfig = new EmailConfig();
			emailService.sendEmail(emailConfig);
		} catch (DataIntegrityViolationException e) {
			throw e;
		}

	}

}
