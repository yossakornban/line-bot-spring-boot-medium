package com.pico.communication.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.linecorp.bot.model.message.TextMessage;
import com.pico.communication.model.ModelUpdate;
import com.pico.communication.model.Register;
import com.pico.communication.service.ApprovePaymentService;

 @CrossOrigin
 @RestController
 @RequestMapping(path = "/approvepaymant")
 public class ApprovePaymentController {

	 @Autowired
     private ApprovePaymentService approvePayRepo;
     
     @Autowired
     private LineBotController LineBotController;

     @GetMapping(path = "/search")
     public ArrayList<Map<String, Object>> searchPaymant(@RequestParam(value = "keyword") String keyword) throws Exception {
    	 return approvePayRepo.searchPaymant(keyword);
     }
     
     @GetMapping(path = "/searchUpdate")
     public Map<String, Object> searchPaymantUpdate(@RequestParam(value = "paymentId") Integer paymentId) throws Exception {
    	 return approvePayRepo.searchPaymantUpdate(paymentId);
     }
     
     @PutMapping(path = "/update")
     public boolean update(@RequestBody Register model) throws Exception {
         StringBuilder text = new StringBuilder();
                	 text.append("บริษัท เพื่อนแท้ แคปปิตอล จำกัด ได้รับชำระเงินเรียบร้อยแล้ว ขอบคุณค่ะ");
                	 LineBotController.push(model.getLine_user_id(), Arrays.asList(new TextMessage(text.toString())));
    	 return true;
     }
     
 }
