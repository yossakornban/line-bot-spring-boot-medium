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
        String text;
        String name = "สมศรี";
        String limit = "100,000";
        String interest = "2,000";
        String period = "36";
        try {
            userId = approveRepo.approve(data);
            if (data.getApprove_status() == true) {
                text =  "เรียน คุณ "+ name + "\n";
                text += "บริษัท เพื่อนแท้ แคปปิตอล จำกัด ขออนุญาติแจ้งผลการขอสินเชื่อของท่านคือ ผ่านการอนุมัติ \n";
                text +="โดยมีข้อมูลให้ท่านพิจารณาดังนี้ \n";
                text +="วงเงินที่อนุมัติ : "+ limit +" บาท \n";
                text +="ดอกเบี้ย/เดือน : "+ interest + "\n";
                text +="จำนวนงวด : "+ period + " งวด \n";
                text +="ท่านสามารถดำเนินเรื่องเอกสารโดยไปที่สาขาของ บริษัท เพื่อนแท้ แคปปิตอล จำกัด ได้เลยค่ะ \n";
                text +="สามารถสอบถามข้อมูลเพิ่มเติมได้ที่ เมนูติดต่อเรา";
            } else {
                text = "บริษัท เพื่อนแท้ แคปปิตอล จำกัด ขออนุญาติแจ้งผลการขอสินเชื่อของท่านคือ ไม่ผ่านการอนุมัติ \n";
                text +="สามารถสอบถามข้อมูลเพิ่มเติมได้ที่  เมนูติดต่อเรา";
            }

            LineBotController.push(userId, Arrays.asList(new TextMessage(text)));
        } catch (DataIntegrityViolationException e) {
            throw e;
        }
    }

    @PostMapping(path = "/submitpayment")
    public void updatePayApprove(@RequestBody Customer data) throws Exception {
        String userId;
        String text;
        String name = "สมศรี";
        try {
            userId = approveRepo.approve(data);
            if (data.getApprove_status() == true) {
                text =  "เรียน คุณ "+ name + "\n";
                text += "บริษัท เพื่อนแท้ แคปปิตอล จำกัด ขออนุญาติแจ้งผลการขอสินเชื่อของท่านคือ ผ่านการอนุมัติ \n";
                String pathYamlHome = "asset/richmenu-pico.yml";
                String pathImageHome = "asset/pico-menu.jpg";
                RichMenuHelper.createRichMenu(lineMessagingClient, pathYamlHome, pathImageHome, userId);
            } else {
                text = "บริษัท เพื่อนแท้ แคปปิตอล จำกัด ขออนุญาติแจ้งผลการขอสินเชื่อของท่านคือ ไม่ผ่านการอนุมัติ \n";
                text +="สามารถสอบถามข้อมูลเพิ่มเติมได้ที่  เมนูติดต่อเรา";
            }
            userId = approvePayRepo.approvePay(data);
            LineBotController.push(userId, Arrays.asList(new TextMessage(text)));
        } catch (DataIntegrityViolationException e) {
            throw e;
        }
    }

}
