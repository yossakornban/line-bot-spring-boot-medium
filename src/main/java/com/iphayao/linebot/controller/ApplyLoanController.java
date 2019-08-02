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
import com.iphayao.linebot.model.Register;
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
@RequestMapping(path = "/apploan")

public class ApplyLoanController {

    @Autowired
    private LineBotController LineBotController;

    @Autowired
    private ApproveRepository approveRepo;

    @Autowired
    private ApprovePaymentRepository approvePayRepo;

    @Autowired
    private LineMessagingClient lineMessagingClient;

    @PostMapping(path = "/regis")
    public void updateApprove(@RequestBody Register data) throws Exception {
        String userId;
        String approveStatus;
        try {
            // userId = approveRepo.approve(data);
            // if (data.getApprove_status() == true) {
            //     approveStatus = "การขอสินเชื่อของคุณได้รับการ อนุมัติ เรียบร้อยแล้ว";
            //     String pathYamlHome = "asset/richmenu-home.yml";
            //     String pathImageHome = "asset/richmenu-home.jpg";
            //     RichMenuHelper.createRichMenu(lineMessagingClient, pathYamlHome, pathImageHome, userId);
            // } else {
            //     approveStatus = "การขอสินเชื่อของคุณ ไม่ได้รับการอนุมัติ สามารถสอบถามเพิ่มเติมได้ที่ 02-222-2222";
            // }

            LineBotController.push(data.getCustomer_user_line_id(), Arrays.asList(new TextMessage("กรุณารอการตอบกลับภายใน 1 วันทำการ")));
        } catch (DataIntegrityViolationException e) {
            throw e;
        }
    }

}
