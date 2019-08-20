package com.pico.communication.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pico.communication.model.EmailConfig;
import com.pico.communication.model.SendInvoice;
import com.pico.communication.service.InvoiceService;

@CrossOrigin
@RestController
@RequestMapping(path = "/invoice")

public class InvoiceController {

	@Autowired
	private InvoiceService invoiceService;

	@PostMapping("/sendInvoice")
	public void line03Search(@RequestBody SendInvoice data) throws Exception {
		try {
			EmailConfig emailConfig = new EmailConfig();
			invoiceService.sendEmail(emailConfig, data);
		} catch (DataIntegrityViolationException e) {
			throw e;
		}

	}

}
