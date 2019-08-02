package com.iphayao.linebot.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iphayao.linebot.helper.RichMenuHelper;
import com.iphayao.linebot.model.Customer;
import com.iphayao.linebot.repository.ApprovePaymentRepository;
import com.iphayao.linebot.repository.ApproveRepository;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
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
    
    
    @GetMapping("/search")
	public ArrayList<Map<String, Object>> line03Search(@RequestParam(value = "countrySearch") String countrySearch) throws Exception {
		return approveRepo.line03Search(countrySearch);
	}
    
    @PostMapping(path = "/approveWaitDoc")
	public void rt01Save(@RequestBody Customer data) throws Throwable {
		try {
			Customer cusResults = new Customer();
			cusResults =  approveRepo.approveWaitDoc(data);
			
			String text;
            if (data.getApprove_status() == true) {
                text =  "เรียน คุณ "+ cusResults.getCustomer_name() + "\n";
                text += "บริษัท เพื่อนแท้ แคปปิตอล จำกัด ขออนุญาติแจ้งผลการขอสินเชื่อของท่านคือ ผ่านการอนุมัติ \n";
                text +="โดยมีข้อมูลให้ท่านพิจารณาดังนี้ \n";
                text +="วงเงินที่อนุมัติ : "+ cusResults.getAccount_credit() +" บาท \n";
                text +="ดอกเบี้ย/เดือน : "+ cusResults.getAccount_interest() + "\n";
                text +="จำนวนงวด : "+ cusResults.getAccount_period() + " งวด \n";
                text +="ท่านสามารถดำเนินเรื่องเอกสารโดยไปที่สาขาของ บริษัท เพื่อนแท้ แคปปิตอล จำกัด ได้เลยค่ะ \n";
                text +="สามารถสอบถามข้อมูลเพิ่มเติมได้ที่ เมนูติดต่อเรา";
            } else {
                text = "บริษัท เพื่อนแท้ แคปปิตอล จำกัด ขออนุญาติแจ้งผลการขอสินเชื่อของท่านคือ ไม่ผ่านการอนุมัติ \n";
                text +="สามารถสอบถามข้อมูลเพิ่มเติมได้ที่  เมนูติดต่อเรา";
            }

            LineBotController.push(cusResults.getCustomer_user_line_id(), Arrays.asList(new TextMessage(text)));
			
		} catch (DataIntegrityViolationException e) {
			throw e;
		}
	}
    
    @PostMapping(path = "/submit")
    public void updateApprove(@RequestBody Customer data) throws Exception {
    	log.info("<--Start getCustomer_user_id.-----------{}-->", data.getCustomer_user_id());
    	log.info("<--Start getAccount_credit.-----------{}-->", data.getAccount_credit());
    	log.info("<--Start getAccount_period.-----------{}-->", data.getAccount_period());
    	log.info("<--Start getAccount_interest.-----------{}-->", data.getAccount_interest());
    	
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
