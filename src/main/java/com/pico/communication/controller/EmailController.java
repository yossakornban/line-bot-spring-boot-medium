package com.pico.communication.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pico.communication.model.EmailConfig;
import com.pico.communication.service.EmailService;

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
