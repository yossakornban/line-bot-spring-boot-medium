package com.iphayao.linebot.controller;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iphayao.linebot.model.Customer;
import com.iphayao.linebot.repository.ApproveRepository;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;

import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;

// import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

import lombok.NonNull;

@ComponentScan
// @LineMessageHandler
@CrossOrigin
@RestController
@RequestMapping(path = "/approve")

public class ApproveController {

    @Autowired
    private ApproveRepository approveRepo ;
	private LineMessagingClient lineMessagingClient;

    @PostMapping(path = "/submit")
    public void updateApprove(@RequestBody Customer data) throws Exception {
       String userId; 
       String approveStatus; 
        try {
            if(data.getApprove_status()){
                approveStatus = "อนุมัติ";
            }else{
                approveStatus = "ไม่อนุมัติ";
            }
            userId = approveRepo.approve(data);
            this.pushById(userId, Arrays.asList(new TextMessage(approveStatus)));
        } catch (DataIntegrityViolationException e) {
            throw e;
        }
    }
    
    private void pushById(@NonNull String userId, @NonNull List<Message> messages) {
		try {
			lineMessagingClient.pushMessage(new PushMessage(userId, messages)).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}
}
