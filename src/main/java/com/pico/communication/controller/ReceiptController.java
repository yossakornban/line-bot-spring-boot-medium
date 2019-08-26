package com.pico.communication.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pico.communication.model.EmailConfig;
import com.pico.communication.model.SendReceipt;
import com.pico.communication.service.ReceiptService;

@CrossOrigin
@RestController
@RequestMapping(path = "/receipt")

public class ReceiptController {

	@Autowired
	private ReceiptService receiptService;

	@PostMapping("/sendReceipt")
	public void sendReceipt(@RequestBody SendReceipt data) throws Exception {
		try {
			EmailConfig emailConfig = new EmailConfig();
			receiptService.sendEmail(emailConfig, data);
		} catch (DataIntegrityViolationException e) {
			throw e;
		}

	}

}
