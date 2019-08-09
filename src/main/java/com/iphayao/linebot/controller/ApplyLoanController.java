package com.iphayao.linebot.controller;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.servlet.http.HttpServletRequest;
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
import com.iphayao.linebot.repository.LoanApprovalRepository;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;

import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

// import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@ComponentScan
@LineMessageHandler
@CrossOrigin
@Slf4j
@RestController
@RequestMapping(path = "/apploan")

public class ApplyLoanController {

    @Autowired
    private LineBotController LineBotController;

    @Autowired
    private LoanApprovalRepository loanAppRepo;

    @PostMapping(path = "/regis")
    public void updateApprove(@RequestBody Register data) throws Exception {
        try {
            loanAppRepo.approveLoan(data);
            LineBotController.push(data.getCustomer_user_line_id(),
                    Arrays.asList(new TextMessage("เรียน คุณ " + data.getCustomer_first_name() +" "+ data.getCustomer_last_name() 
                    + "\n" + "ขณะนี้บริษัท เพื่อนแท้ แคปปิตอล จำกัด ได้รับเรื่องการขอสินเชื่อของท่านแล้ว " 
                    + "\n" + "ทางเราจะทำการพิจารณา และแจ้งผลตอบกลับโดยด่วนที่สุด "
                    + "\n" + "สามารถสอบถามข้อมูลเพิ่มเติมได้ที่ เมนูติดต่อเรา " 
           )));
        } catch (DataIntegrityViolationException e) {
            throw e;
        }
    }

    @PostMapping(path = "/testline")
    public String testline(HttpServletRequest req) throws Exception {
        try {
            log.info("==================");
            log.info("111 "+req);
           return "aaaaaaa";
        } catch (Exception e) {
            throw e;
        }
    }

}
