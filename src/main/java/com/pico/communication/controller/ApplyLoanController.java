package com.pico.communication.controller;

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

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;

import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import com.pico.communication.helper.RichMenuHelper;
import com.pico.communication.model.Customer;
import com.pico.communication.model.Register;
import com.pico.communication.service.ApprovePaymentService;
import com.pico.communication.service.ApproveService;
import com.pico.communication.service.LoanApprovalService;

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

    private LoanApprovalService loanAppRepo;

	@Autowired
	private LineMessagingClient lineMessagingClient;
	
    @PostMapping(path = "/regis")
    public void updateApprove(@RequestBody Register data) throws Exception {
        try {
        	System.out.println("******************* "+data);
//            loanAppRepo.approveLoan(data);
            LineBotController.push(data.getLine_user_id(),
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
        	String aaa = loanAppRepo.executePost();
        	System.out.println(aaa);
            log.info("==================");
            log.info("111 " + req);
            return "aaaaaaa";
        } catch (Exception e) {
            throw e;
        }
    }

    @PostMapping(path = "/register")
    public void register(@RequestBody Register data) throws Exception {
        try {
            if (data.getFlag()) {
            	String pathYamlHome = "asset/richmenu-pico.yml";
				String pathImageHome = "asset/pico-menu.jpg";
				RichMenuHelper.createRichMenu(lineMessagingClient, pathYamlHome, pathImageHome, data.getLine_user_id());
                LineBotController.push(data.getLine_user_id(), Arrays.asList(new TextMessage("การผูกสัญญาเพื่อนแท้กับไลน์สำเร็จ")));
            }else {
                LineBotController.push(data.getLine_user_id(), Arrays.asList(new TextMessage("ข้อมูลของคุณไม่มีในระบบ \n กรุณาขอสินเชื่อ\n หรือสอบถามข้อมูลเพิ่มเติมได้ที่  เมนูติดต่อเรา")));
            }

        } catch (DataIntegrityViolationException e) {
            throw e;
        }
    }

}
