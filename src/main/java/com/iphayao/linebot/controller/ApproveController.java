package com.iphayao.linebot.controller;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
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
	public ArrayList<Map<String, Object>> line03Search(@RequestParam(value = "countrySearch") String countrySearch)
			throws Exception {
		return approveRepo.line03Search(countrySearch);
	}

	@PostMapping(path = "/approveWaitDoc")
	public void approveWaitDoc(@RequestBody Customer data) throws Throwable {
		try {
			Map<String, Object> cusResults = new HashMap<String, Object>();
			cusResults = approveRepo.approveWaitDoc(data);
			
			NumberFormat mf = NumberFormat.getInstance(new Locale("en", "US"));
			mf.setMaximumFractionDigits(2);
			
			String text;
			if (cusResults.get("account_status").toString().equals("2")) {
				text = "เรียน คุณ " + (String) cusResults.get("customer_name") + "\n";
				text += "บริษัท เพื่อนแท้ แคปปิตอล จำกัด ขออนุญาติแจ้งผลการขอสินเชื่อของท่านคือ ผ่านการอนุมัติ \n";
				text += "โดยมีข้อมูลให้ท่านพิจารณาดังนี้ \n";
				text += "วงเงินที่อนุมัติ : " + mf.format(cusResults.get("account_credit")) + " บาท \n";
				text += "ดอกเบี้ย/เดือน : " + mf.format(cusResults.get("interest_bht")) + " % \n";
				text += "จำนวนงวด : " + cusResults.get("account_period") + " งวด \n";
				text += "ท่านสามารถดำเนินเรื่องเอกสารโดยไปที่สาขาของ บริษัท เพื่อนแท้ แคปปิตอล จำกัด ได้เลยค่ะ \n";
				text += "สามารถสอบถามข้อมูลเพิ่มเติมได้ที่ เมนูติดต่อเรา";
			} else {
				text = "บริษัท เพื่อนแท้ แคปปิตอล จำกัด ขออนุญาติแจ้งผลการขอสินเชื่อของท่านคือ ไม่ผ่านการอนุมัติ \n";
				text += "สามารถสอบถามข้อมูลเพิ่มเติมได้ที่  เมนูติดต่อเรา";
			}

			LineBotController.push((String) cusResults.get("customer_user_line_id"), Arrays.asList(new TextMessage(text)));

		} catch (DataIntegrityViolationException e) {
			throw e;
		}
	}

	@PostMapping(path = "/approvePayment")
	public void updateApprovePayment(@RequestBody Customer data) throws Exception {
		try {
			Map<String, Object> cusResults = new HashMap<String, Object>();
			cusResults = approveRepo.approvePayment(data);

			String text;
			if (cusResults.get("account_status").toString().equals("7")) {
				text = "เรียน คุณ " + (String) cusResults.get("customer_name") + "\n";
				text += "บริษัท เพื่อนแท้ แคปปิตอล จำกัด ได้โอนเงินให้กับท่านเรียบร้อยแล้ว \n";
				String pathYamlHome = "asset/richmenu-pico.yml";
				String pathImageHome = "asset/pico-menu.jpg";
				RichMenuHelper.createRichMenu(lineMessagingClient, pathYamlHome, pathImageHome, (String) cusResults.get("customer_user_line_id"));
			} else {
				text = "บริษัท เพื่อนแท้ แคปปิตอล จำกัด ขออนุญาติแจ้งผลการขอสินเชื่อของท่านคือ ไม่ผ่านการอนุมัติ \n";
				text += "สามารถสอบถามข้อมูลเพิ่มเติมได้ที่  เมนูติดต่อเรา";
			}
			LineBotController.push((String) cusResults.get("customer_user_line_id"), Arrays.asList(new TextMessage(text)));
		} catch (DataIntegrityViolationException e) {
			throw e;
		}
	}

}
