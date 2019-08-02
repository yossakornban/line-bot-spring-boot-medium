// package com.iphayao.linebot.controller;
//
// import java.util.Arrays;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.dao.DataIntegrityViolationException;
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.iphayao.linebot.model.Customer;
//import com.iphayao.linebot.repository.ApprovePaymentRepository;
//import com.linecorp.bot.client.LineMessagingClient;
//import com.linecorp.bot.model.message.TextMessage;
//import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
//
// @CrossOrigin
// @RestController
// @RequestMapping(path = "/approve")
// public class ApprovePaymentController {
//	 
//     @Autowired
//     private ApprovePaymentRepository approvePayRepo;
//
//     @PostMapping(path = "/submitpayment")
//     public void updateApprove(@RequestBody Customer data) throws Exception {
//        String userId; 
//        String approveStatus; 
//         try {
//             if(data.getApprove_status() == true){
//                 approveStatus = "หลักฐานการชำระเงินของคุณได้ผ่านการยืนยันเรียบร้อยแล้ว";
//             }else{
//                 approveStatus = "หลักฐานการชำระเงินของคุณไม่ผ่านการยืนยัน สามารถสอบถามเพิ่มเติมได้ที่ 02-222-2222";
//             }
////             userId = approvePayRepo.approve(data);
////             LineBotController.push(userId, Arrays.asList(new TextMessage(approveStatus)));
//         } catch (DataIntegrityViolationException e) {
//             throw e;
//         }
//     }
//
// }
