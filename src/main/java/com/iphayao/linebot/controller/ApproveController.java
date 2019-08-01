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

import com.iphayao.linebot.helper.RichMenuHelper;
import com.iphayao.linebot.model.Customer;
import com.iphayao.linebot.repository.ApprovePaymentRepository;
import com.iphayao.linebot.repository.ApproveRepository;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;

import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

// import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

import lombok.NonNull;

@ComponentScan
@LineMessageHandler
@CrossOrigin
@RestController
@RequestMapping(path = "/approve")

public class ApproveController {

    @Autowired
    private LineBotController LineBotController;

    @Autowired
    private ApproveRepository approveRepo;

    @Autowired
    private ApprovePaymentRepository approvePayRepo;

    @Autowired
    private LineMessagingClient lineMessagingClient;

    @PostMapping(path = "/submit")
    public void updateApprove(@RequestBody Customer data) throws Exception {
        String userId;
        String approveStatus;
        try {
            userId = approveRepo.approve(data);
            if (data.getApprove_status() == true) {
                approveStatus = "การขอสินเชื่อของคุณได้รับการ อนุมัติ เรียบร้อยแล้ว";
                String pathYamlHome = "asset/richmenu-pico.yml";
                String pathImageHome = "asset/pico-menu.jpg";
                RichMenuHelper.createRichMenu(lineMessagingClient, pathYamlHome, pathImageHome, userId);
            } else {
                approveStatus = "การขอสินเชื่อของคุณ ไม่ได้รับการอนุมัติ สามารถสอบถามเพิ่มเติมได้ที่ 02-222-2222";
            }

            LineBotController.push(userId, Arrays.asList(new TextMessage(approveStatus)));
        } catch (DataIntegrityViolationException e) {
            throw e;
        }
    }

    @PostMapping(path = "/submitpayment")
    public void updatePayApprove(@RequestBody Customer data) throws Exception {
        String userId;
        String approveStatus;
        try {
            if (data.getApprove_status() == true) {
                approveStatus = "หลักฐานการชำระเงินของคุณได้ผ่านการยืนยันเรียบร้อยแล้ว";
            } else {
                approveStatus = "หลักฐานการชำระเงินของคุณไม่ผ่านการยืนยัน สามารถสอบถามเพิ่มเติมได้ที่ 02-222-2222";
            }
            userId = approvePayRepo.approvePay(data);
            LineBotController.push(userId, Arrays.asList(new TextMessage(approveStatus)));
        } catch (DataIntegrityViolationException e) {
            throw e;
        }
    }

}
