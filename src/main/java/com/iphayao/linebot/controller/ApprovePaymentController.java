 package com.iphayao.linebot.controller;

import java.util.ArrayList;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iphayao.linebot.repository.ApprovePaymentRepository;
import com.iphayao.linebot.repository.ApprovePaymentRepository.ModelUpdate;

 @CrossOrigin
 @RestController
 @RequestMapping(path = "/approvepaymant")
 public class ApprovePaymentController {

     @Autowired
     private ApprovePaymentRepository approvePayRepo;

     @GetMapping(path = "/search")
     public ArrayList<Map<String, Object>> searchPaymant(@RequestParam(value = "keyword") String keyword) throws Exception {
    	 return approvePayRepo.searchPaymant(keyword);
     }
     
     @GetMapping(path = "/searchUpdate")
     public Map<String, Object> searchPaymantUpdate(@RequestParam(value = "paymentId") Integer paymentId) throws Exception {
    	 return approvePayRepo.searchPaymantUpdate(paymentId);
     }
     
     @PutMapping(path = "/update")
     public Map<String, Object> update(@RequestBody ModelUpdate model) throws Exception {
    	 return approvePayRepo.Update(model);
     }

 }
