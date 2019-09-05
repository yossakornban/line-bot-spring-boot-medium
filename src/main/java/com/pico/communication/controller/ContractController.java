package com.pico.communication.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.linecorp.bot.client.MessageContentResponse;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.ImageMessageContent;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.pico.communication.model.Register;

import lombok.extern.slf4j.Slf4j;

@CrossOrigin
@RestController
@Slf4j
@RequestMapping(path = "/contract")
public class ContractController {

	@Autowired
	private LineBotController lineBotController;

	private File txtFile = null;
	private FileOutputStream fop = null;
	private final String path = System.getProperty("catalina.base")+"/webapps/ROOT/receive/";

	@PostMapping("/confirm")
	public void sendReceipt(@RequestBody Register data) throws Exception {

		StringBuilder messageText = new StringBuilder();
		messageText.append("วงเงินของท่านได้รับการอนุมัติเรียบร้อยแล้ว");
		lineBotController.push(data.getLine_user_id(), Arrays.asList(new TextMessage(messageText.toString())));

	}
}
