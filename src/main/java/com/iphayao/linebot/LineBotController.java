package com.iphayao.linebot;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.google.common.io.ByteStreams;
import com.iphayao.linebot.flex.CatalogueFlexMessageSupplier;
import com.iphayao.linebot.flex.NewsFlexMessageSupplier;
import com.iphayao.linebot.flex.ReceiptFlexMessageSupplier;
import com.iphayao.linebot.flex.RestaurantFlexMessageSupplier;
import com.iphayao.linebot.flex.RestaurantMenuFlexMessageSupplier;
import com.iphayao.linebot.flex.TicketFlexMessageSupplier;
import com.iphayao.linebot.helper.RichMenuHelper;
import com.iphayao.linebot.model.Employee;
import com.iphayao.linebot.model.Entity;
import com.iphayao.linebot.model.Holiday;
import com.iphayao.linebot.model.UserLog;
import com.iphayao.linebot.model.UserLog.status;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.client.MessageContentResponse;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.DatetimePickerAction;
import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.PostbackEvent;
import com.linecorp.bot.model.event.message.ImageMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.StickerMessage;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.template.CarouselColumn;
import com.linecorp.bot.model.message.template.CarouselTemplate;
import com.linecorp.bot.model.message.template.ConfirmTemplate;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

import com.iphayao.LineApplication;

@Slf4j
@ComponentScan
@LineMessageHandler

public class LineBotController {
	@Autowired
	private LineMessagingClient lineMessagingClient;

	@Autowired
	private LineRepository lineRepo;

	// private status userLog.setStatusBot(status.DEFAULT); // Default status
	private Map<String, UserLog> userMap = new HashMap<String, UserLog>();

	@EventMapping
	public void handleTextMessage(MessageEvent<TextMessageContent> event) throws IOException {
		log.info(event.toString());
		TextMessageContent message = event.getMessage();
		handleTextContent(event.getReplyToken(), event, message);
	}

	@EventMapping
	public void handlePostbackEvent(PostbackEvent event) {
		String replyToken = event.getReplyToken();
			   replyToken = replyToken.replace("date", "");
		this.replyText(replyToken,event.getPostbackContent().getData().toString() .replace("date", "")
				+ event.getPostbackContent().getParams().toString());
	}
//	public void handlePostbackEvent(PostbackEvent event) {
//		String replyToken = event.getReplyToken();
//		this.replyText(replyToken, "Got postback data " + event.getPostbackContent().getData() + ", param "
//				+ event.getPostbackContent().getParams().toString());
//	}

	@EventMapping
	public void handleOtherEvent(Event event) {
		log.info("Received message(Ignored): {}", event);
	}

	@EventMapping
	public void handleImageMessage(MessageEvent<ImageMessageContent> event) {
		log.info(event.toString());
		ImageMessageContent content = event.getMessage();
		String replyToken = event.getReplyToken();

		try {
			MessageContentResponse response = lineMessagingClient.getMessageContent(content.getId()).get();
			DownloadedContent jpg = saveContent("jpg", response);
			DownloadedContent previewImage = createTempFile("jpg");

			system("convert", "-resize", "240x", jpg.path.toString(), previewImage.path.toString());

			reply(replyToken, new ImageMessage(jpg.getUri(), previewImage.getUri()));

		} catch (InterruptedException | ExecutionException e) {
			reply(replyToken, new TextMessage("Cannot get image: " + content));
			throw new RuntimeException(e);
		}

	}

	private static final DateFormat dateNow = new SimpleDateFormat("yyyy-MM-dd");
	private static final DateFormat dateNowHoliday = new SimpleDateFormat("dd/MM/yyyy");
	Date nowDate = new Date();

	

	private void handleTextContent(String replyToken, Event event, TextMessageContent content) throws IOException {
		UserLog userLog = userMap.get(event.getSource().getSenderId());

		if (userLog == null) {
			userLog = new UserLog(event.getSource().getSenderId(), status.DEFAULT);
			userMap.put(event.getSource().getSenderId(), userLog);
		}

		System.out.println("+++++ " + userMap.get(event.getSource().getSenderId()).toString());

		String text = content.getText();
		ModelMapper modelMapper = new ModelMapper();
		userLog.setEmpCode(text.toString());
		String empName = lineRepo.findEmp(text.toString());// ------------------------------------------------------------String

		;
		if (userLog.getStatusBot().equals(status.DEFAULT)) {
			switch (text) {
			case "ลงทะเบียน": {
				this.reply(replyToken,
						Arrays.asList(new TextMessage("กรุณากรอก รหัสพนักงาน" + "\n" + "เพื่อยืนยันตัวตนค่ะ")));
				userLog.setStatusBot(status.FINDEMP);
				break;
			}
			case "list": {
				ArrayList<Map<String, Object>> list = lineRepo.list();
				list.forEach(record -> {
					Entity en = new Entity();
					modelMapper.map(record, en);
					this.push(replyToken, Arrays.asList(new TextMessage(en.getMessage())));
				});
				userLog.setStatusBot(status.DEFAULT);
				break;

			}

			case "ควย": {
				this.reply(replyToken, Arrays.asList(new TextMessage(
						"ควยพ่อมึงดิ เดี๋ยวกูก็เอาปืนยิงหัวพ่อมึงหรอก ใส่รหัสพนักงานมา  แล้วทำห่าอะไรก็ทำไป!!!")));

				userLog.setStatusBot(status.FINDEMP);
				break;

			}
			case "สัส": {
				this.reply(replyToken, Arrays.asList(new TextMessage(
						"สัส !พ่อมึงดิ เดี๋ยวกูก็เอาปืนยิงหัวพ่อมึงหรอก ใส่รหัสพนักงานมา  แล้วทำห่าอะไรก็ทำไป!!!")));

				userLog.setStatusBot(status.FINDEMP);
				break;

			}
			case "ไอ้สัส": {
				this.reply(replyToken, Arrays.asList(new TextMessage(
						"ไอ้สัส !พ่อมึงดิ เดี๋ยวกูก็เอาปืนยิงหัวพ่อมึงหรอก ใส่รหัสพนักงานมา  แล้วทำห่าอะไรก็ทำไป!!!")));

				userLog.setStatusBot(status.FINDEMP);
				break;

			}
			case "ไอ้เหี้ย": {
				this.reply(replyToken, Arrays.asList(new TextMessage(
						"พ่อมึงดิ เดี๋ยวกูก็เอาปืนยิงหัวพ่อมึงหรอก ใส่รหัสพนักงานมา  แล้วทำห่าอะไรก็ทำไป!!!")));

				userLog.setStatusBot(status.FINDEMP);
				break;

			}

			// Holidays------------------------------------------------------------------------
			case "ขอทราบ ข้อมูลวันหยุดค่ะ": {
				String pathYamlHome = "asset/sub_select_event.yml";
				String pathImageHome = "asset/sub_select_event.jpg";
				RichMenuHelper.createRichMenu(lineMessagingClient, pathYamlHome, pathImageHome, userLog.getUserID());
				this.reply(replyToken, Arrays.asList(new TextMessage("เลือกเมนูที่ต้องการ ได้เลยค่ะ  😊")));
				userLog.setStatusBot(status.DEFAULT);
				break;
			}
			case "ขอทราบวันหยุด ทั้งหมดภายในปีนี้ค่ะ": {

				Stack<String> holi_list = new Stack<>();
				ArrayList<Map<String, Object>> holiday_all = lineRepo.holidayList();
				holiday_all.forEach(record -> {
					Holiday holi = new Holiday();
					modelMapper.map(record, holi);
					holi_list.push("\n" + "➤ " + holi.getDate_holiday() + "  " + holi.getName_holiday());
				});
				
				String Imr = holi_list.toString();
				Imr = Imr.replace("[", "");
				Imr = Imr.replace("]", "");
				Imr = Imr.replace(",", "");
				this.reply(replyToken,
						Arrays.asList(new TextMessage("ข้อมูลวันหยุดประจำปี ทั้งหมดค่ะ  " + "\n" + Imr)));
				userLog.setStatusBot(status.DEFAULT);
				break;
			}

			case "ขอทราบวันหยุด ที่จะถึงเร็วๆนี้ค่ะ": {
				Date nowDate = new Date();
				Stack<String> holi_list = new Stack<>();
				ArrayList<Map<String, Object>> holiday_all = lineRepo.Holiday_Soon();
				holiday_all.forEach(record -> {
					Holiday holi = new Holiday();
					modelMapper.map(record, holi);
					holi_list.push("\n" + holi.getDate_holiday() + "   " + holi.getName_holiday());

				});
				String day1 = holiday_all.get(0).toString();
				String day2 = holiday_all.get(1).toString();
				String day3 = holiday_all.get(2).toString();
				day1 = day1.replace("2019-01-01", "01/01/2019");
				day1 = day1.replace("2019-02-05", "05/02/2019");
				day1 = day1.replace("2019-02-19", "19/02/2019");
				day1 = day1.replace("2019-04-08", "08/04/2019");
				day1 = day1.replace("2019-04-15", "15/04/2019");
				day1 = day1.replace("2019-04-16", "16/04/2019");
				day1 = day1.replace("2019-05-01", "01/05/2019");
				day1 = day1.replace("2019-07-20", "20/07/2019");
				day1 = day1.replace("2019-07-16", "16/07/2019");
				day1 = day1.replace("2019-07-29", "29/07/2019");
				day1 = day1.replace("2019-08-12", "12/08/2019");
				day1 = day1.replace("2019-10-14", "14/10/2019");
				day1 = day1.replace("2019-10-23", "23/10/2019");
				day1 = day1.replace("2019-12-5", "05/12/2019");
				day1 = day1.replace("2019-12-10", "10/12/2019");
				day1 = day1.replace("2019-12-31", "31/12/2019");
				// -------------------------------------------------
				day2 = day2.replace("2019-01-01", "01/01/2019");
				day2 = day2.replace("2019-02-05", "05/02/2019");
				day2 = day2.replace("2019-02-19", "19/02/2019");
				day2 = day2.replace("2019-02-08", "08/02/2019");
				day2 = day2.replace("2019-04-15", "15/04/2019");
				day2 = day2.replace("2019-04-16", "16/04/2019");
				day2 = day2.replace("2019-05-01", "01/05/2019");
				day2 = day2.replace("2019-07-20", "20/07/2019");
				day2 = day2.replace("2019-07-16", "16/07/2019");
				day2 = day2.replace("2019-07-29", "29/07/2019");
				day2 = day2.replace("2019-08-12", "12/08/2019");
				day2 = day2.replace("2019-10-14", "14/10/2019");
				day2 = day2.replace("2019-10-23", "23/10/2019");
				day2 = day2.replace("2019-12-5", "05/12/2019");
				day2 = day2.replace("2019-12-10", "10/12/2019");
				day2 = day2.replace("2019-12-31", "31/12/2019");
				// -------------------------------------------------
				day3 = day3.replace("2019-01-01", "01/01/2019");
				day3 = day3.replace("2019-02-05", "05/02/2019");
				day3 = day3.replace("2019-02-19", "19/02/2019");
				day3 = day3.replace("2019-02-08", "08/02/2019");
				day3 = day3.replace("2019-04-15", "15/04/2019");
				day3 = day3.replace("2019-04-16", "16/04/2019");
				day3 = day3.replace("2019-05-01", "01/05/2019");
				day3 = day3.replace("2019-07-20", "20/07/2019");
				day3 = day3.replace("2019-07-16", "16/07/2019");
				day3 = day3.replace("2019-07-29", "29/07/2019");
				day3 = day3.replace("2019-08-12", "12/08/2019");
				day3 = day3.replace("2019-10-14", "14/10/2019");
				day3 = day3.replace("2019-10-23", "23/10/2019");
				day3 = day3.replace("2019-12-5", "05/12/2019");
				day3 = day3.replace("2019-12-10", "10/12/2019");
				day3 = day3.replace("2019-12-31", "31/12/2019");
				// -------------------------------------------------

				day1 = day1.replace("{", "");
				day1 = day1.replace("}", "");
				day1 = day1.replace("to_date=", "");
				day1 = day1.replace("name_holiday=", "");
				day1 = day1.replace("=", "");
				day1 = day1.replace(",", " ");
				day2 = day2.replace("{", "");
				day2 = day2.replace("}", "");
				day2 = day2.replace("to_date=", "");
				day2 = day2.replace("name_holiday=", " ");
				day2 = day2.replace("=", "");
				day2 = day2.replace(",", " ");
				day3 = day3.replace("{", "");
				day3 = day3.replace("}", "");
				day3 = day3.replace("to_date=", "");
				day3 = day3.replace("name_holiday=", " ");
				day3 = day3.replace("=", "");
				day3 = day3.replace(",", " ");

				this.reply(replyToken,
						Arrays.asList(new TextMessage("วันที่ปัจจุบัน คือ  " + " " + dateNowHoliday.format(nowDate) + "\n"
								+ "\n" + "วันหยุดที่จะถึงเร็วๆนี้ ได้เเก่ " + "\n" + "➤ " + day1 + "\n" + "➤ " + day2
								+ "\n" + "➤ " + day3)));
				userLog.setStatusBot(status.DEFAULT);
				break;
			}

			case "ย้อนกลับค่ะ": {
				String pathYamlHome = "asset/select_event.yml";
				String pathImageHome = "asset/select_event.jpg";
				RichMenuHelper.createRichMenu(lineMessagingClient, pathYamlHome, pathImageHome, userLog.getUserID());
				this.reply(replyToken, Arrays.asList(new TextMessage("เลือกเมนูที่ต้องการ ได้เลยค่ะ  😁")));
				userLog.setStatusBot(status.DEFAULT);
				break;
			}
			case "profile": {
				String userId = event.getSource().getUserId();
				if (userId != null) {
					lineMessagingClient.getProfile(userId).whenComplete((profile, throwable) -> {
						if (throwable != null) {
							this.replyText(replyToken, throwable.getMessage());
							return;
						}
						this.reply(replyToken,
								Arrays.asList(new TextMessage(
										"Display name : " + profile.getDisplayName() + "\n Status message : "
												+ profile.getStatusMessage() + "\n User ID : " + profile.getUserId())));
					});
				}
				userLog.setStatusBot(status.DEFAULT);
				break;
			}
			// case "leave": {
			// String imageUrl = createUri("/static/buttons/1040.jpg");
			// CarouselTemplate carouselTemplate = new CarouselTemplate(
			// Arrays.asList(new CarouselColumn(imageUrl, "ประเภทการลา",
			// "กรุณาเลือก ประเภทการลา ด้วยค่ะ",
			// Arrays.asList(new MessageAction("ลากิจ", "ลากิจครับ"),
			// new MessageAction("ลาป่วย", "ลาป่วยครับ"),
			// new MessageAction("ลาพักร้อน", "ลาหักร้อนครับ")))));
			// TemplateMessage templateMessage = new TemplateMessage("Carousel
			// alt text", carouselTemplate);
			// this.reply(replyToken, templateMessage);
			// userLog.setStatusBot(status.Q11);
			// break;
			// }
			case "ขอลาหยุดครับผม": {
				String imageUrl = createUri("/static/buttons/1040.jpg");
				CarouselTemplate carouselTemplate = new CarouselTemplate(
						Arrays.asList(new CarouselColumn(imageUrl, "ประเภทการลา", "กรุณาเลือก ประเภทการลา ด้วยค่ะ",
								Arrays.asList(new MessageAction("ลากิจ", "ลากิจครับ"),
										new MessageAction("ลาป่วย", "ลาป่วยครับ"),
										new MessageAction("ลาพักร้อน", "ลาพักร้อนครับ")))));
				TemplateMessage templateMessage = new TemplateMessage("Carousel alt text", carouselTemplate);
				this.reply(replyToken, templateMessage);
				userLog.setStatusBot(status.Q11);
				break;

			}
			case "help": {
				this.reply(replyToken, Arrays.asList(new TextMessage(
						"โปรดเลือกรายการ \n พิมพ์  profile : ดูข้อมูล Profile  \n พิมพ์  list : ดู Agenda \n พิมพ์  add : เพิ่ม Agenda")));
				userLog.setStatusBot(status.DEFAULT);
				;
				break;
			}
			case "Flex": {
				String pathYamlHome = "asset/richmenu-home.yml";
				String pathImageHome = "asset/richmenu-home.jpg";
				RichMenuHelper.createRichMenu(lineMessagingClient, pathYamlHome, pathImageHome, userLog.getUserID());
				break;
			}
			case "สอบถาม ข้อมูลทั่วไป": {

				RichMenuHelper.deleteRichMenu(lineMessagingClient, userLog.getUserID());
				break;
			}

			case "Flex Restaurant": {
				this.reply(replyToken, new RestaurantFlexMessageSupplier().get());
				break;
			}
			case "Flex Menu": {
				this.reply(replyToken, new RestaurantMenuFlexMessageSupplier().get());
				break;
			}
			case "Flex Receipt": {
				this.reply(replyToken, new ReceiptFlexMessageSupplier().get());
				break;
			}
			case "Flex News": {
				this.reply(replyToken, new NewsFlexMessageSupplier().get());
				break;
			}
			case "Flex Ticket": {
				this.reply(replyToken, new TicketFlexMessageSupplier().get());
				break;
			}
			case "Flex Catalogue": {
				this.reply(replyToken, new CatalogueFlexMessageSupplier().get());
				break;
			}
			case "carousel": {
				String imageUrl = createUri("/static/buttons/1040.jpg");
				CarouselTemplate carouselTemplate = new CarouselTemplate(Arrays.asList(
						new CarouselColumn(imageUrl, "hoge", "fuga",
								Arrays.asList(new URIAction("Go to line.me", "https://line.me"),
										new URIAction("Go to line.me", "https://line.me"),
										new PostbackAction("Say hello1", "hello こんにちは", "hello こんにちは"))),
						new CarouselColumn(imageUrl, "hoge", "fuga",
								Arrays.asList(new PostbackAction("言 hello2", "hello こんにちは", "hello こんにちは"),
										new PostbackAction("言 hello2", "hello こんにちは", "hello こんにちは"),
										new MessageAction("Say message", "Rice=米"))),
						new CarouselColumn(imageUrl, "Datetime Picker", "Please select a date, time or datetime",
								Arrays.asList(
										new DatetimePickerAction("Datetime", "action=sel", "datetime",
												"2017-06-18T06:15", "2100-12-31T23:59", "1900-01-01T00:00"),
										new DatetimePickerAction("Date", "action=sel&only=date", "date", "18-06-2017",
												"31-12-2100", "01-01-1900"),
										new DatetimePickerAction("Time", "action=sel&only=time", "time", "06:15",
												"23:59", "00:00")))));
				TemplateMessage templateMessage = new TemplateMessage("Carousel alt text", carouselTemplate);
				this.reply(replyToken, templateMessage);
				break;
			}
			case "Vote": {
				String imageUrl = createUri("data:image/jpeg;base64,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");
				CarouselTemplate carouselTemplate = new CarouselTemplate(Arrays.asList(
					
						new CarouselColumn(imageUrl, "ข้าวผัด", "เพิ่มพลังความคิด วิตามีน B12 ให้โปรเเกรมเมอร์",
								Arrays.asList(new PostbackAction("言 hello2", "hello こんにちは", "hello こんにちは"),
										new MessageAction("โหวต  ข้าวผัด", "ข้าวผัด ครับ")))));
						
				TemplateMessage templateMessage = new TemplateMessage("Carousel alt text", carouselTemplate);
				this.reply(replyToken, templateMessage);
				break;
			}
			default:
				this.reply(replyToken, Arrays.asList(new TextMessage("ไม่เข้าใจคำสั่ง")));
			}

		} else if (userLog.getStatusBot().equals(status.SAVE)) {
			switch (text) {
			case "cancel": {
				this.reply(replyToken, Arrays.asList(new TextMessage("ยกเลิกสำเร็จ ")));
				userLog.setStatusBot(status.DEFAULT);
				break;
			}
			default:

			}
		} else if (userLog.getStatusBot().equals(status.Q11)) {

			switch (text) {
//------------------------------------------------------------------------------------------------------------------Focus
			case "ลากิจครับ": {
				
				String imageUrl = createUri("/static/buttons/1040.jpg");

				CarouselTemplate carouselTemplate = new CarouselTemplate(Arrays.asList(
	
						new CarouselColumn(imageUrl, "วันลา  เริ่มต้น ", "กรุณา กำหนดวันลา เริ่มต้นด้วยค่ะ"+"\n"+"(ไม่สามารถเกลับมาเเก้ไขได้!!)",
								Arrays.asList(

										new DatetimePickerAction("กำหนดวัน", "วันลา  เริ่มต้นของคุณคือ ", "date",
												dateNow.format(nowDate), "2100-12-31", dateNow.format(nowDate)
												)))));
	
				TemplateMessage templateMessage = new TemplateMessage("Carousel alt text", carouselTemplate);
							
				
				this.reply(replyToken, templateMessage);
			
				//log.info("Return echo message %s : %s", replyToken, text);
				this.reply(replyToken, Arrays.asList(new TextMessage("หนุกหนานลากิจ")));
				userLog.setStatusBot(status.DEFAULT);
				break;
			}
			case "ลาป่วยครับ": {
				log.info("Return echo message %s : %s", replyToken, text);
				this.reply(replyToken, Arrays.asList(new TextMessage("หนุกหนาน ลาป่วย")));
				userLog.setStatusBot(status.DEFAULT);
				break;
			}
			case "ลาพักร้อนครับ": {
				this.reply(replyToken, Arrays.asList(new TextMessage("หนุกหนาน พักร้อน")));
				userLog.setStatusBot(status.DEFAULT);
				break;
			}

			case "ขอทราบวันหยุด ทั้งหมดภายในปีนี้ค่ะ": {

				Stack<String> holi_list = new Stack<>();
				ArrayList<Map<String, Object>> holiday_all = lineRepo.holidayList();
				holiday_all.forEach(record -> {
					Holiday holi = new Holiday();
					modelMapper.map(record, holi);
					holi_list.push("\n" + "➤ " + holi.getDate_holiday() + "  " + holi.getName_holiday());
				});
			
				String Imr = holi_list.toString();
				Imr = Imr.replace("[", "");
				Imr = Imr.replace("]", "");
				Imr = Imr.replace(",", "");
				this.reply(replyToken,
						Arrays.asList(new TextMessage("ข้อมูลวันหยุดประจำปี ทั้งหมดค่ะ  " + "\n" + Imr)));
				userLog.setStatusBot(status.DEFAULT);
				break;
			}
			default:
				String imageUrl = createUri("/static/buttons/1040.jpg");
				CarouselTemplate carouselTemplate = new CarouselTemplate(
						Arrays.asList(new CarouselColumn(imageUrl, "ประเภทการลา", "กรุณาเลือก ประเภทการลา ด้วยค่ะ",
								Arrays.asList(new MessageAction("ลากิจ", "รอ Flow ของลากิจครับ"),
										new MessageAction("ลาป่วย", "รอ Flow ลาป่วยครับ"),
										new MessageAction("ลาพักร้อน", "รอ Flow ลาหักร้อนครับ")))));
				TemplateMessage templateMessage = new TemplateMessage("Carousel alt text", carouselTemplate);
				this.reply(replyToken, templateMessage);
				userLog.setStatusBot(status.DEFAULT);
				break;
			}
		} else if (userLog.getStatusBot().equals(status.FINDEMP)) {

			if (empName != null) {
				lineRepo.register(userLog);
				ConfirmTemplate confirmTemplate = new ConfirmTemplate("ยืนยัน, คุณใช่ " + empName + " หรือไม่ ?",
						new MessageAction("ใช่ !", "ใช่"), new MessageAction("ไม่ใช่ !", "ไม่ใช่"));

				TemplateMessage templateMessage = new TemplateMessage("Confirm alt text", confirmTemplate);
				this.reply(replyToken, templateMessage);
				userLog.setStatusBot(status.FINDCONFIRM);
			} else {
				this.reply(replyToken, Arrays.asList(new TextMessage(

						"ไม่มีข้อมูลพนักงานเบื้องต้นในระบบ โปรดกรอกรหัสพนักงานให้ถูกต้อง หรือ ติดต่อผู้ดูแลระบบ  \n @line : http://line.naver.jp/ti/p/-AK9r2Na5E#~ "),
						new TextMessage("กรุณากรอก รหัสพนักงาน ให้ถูกต้อง" + "\n" + "เพื่อยืนยันตัวตนอีกครั้งค่ะ")));
				;

				userLog.setStatusBot(status.FINDEMP);
			}

		} else if (userLog.getStatusBot().equals(status.FINDCONFIRM)) {
			switch (text) {
			case "ใช่": {
				userLog.setStatusBot(status.DEFAULT);
				String pathYamlHome = "asset/select_event.yml";
				String pathImageHome = "asset/select_event.jpg";
				RichMenuHelper.createRichMenu(lineMessagingClient, pathYamlHome, pathImageHome, userLog.getUserID());
				this.reply(replyToken, Arrays.asList(new TextMessage(
						"ลงทะเบียนสำเร็จ  " + "\n" + "กรุณา  เลือกเมนู ที่ต้องการทำรายการ ได้เลยค่ะ  😊")));
				break;
			}
			case "ไม่ใช่": {
				this.reply(replyToken, Arrays.asList(new TextMessage(
						"กรุณากรอก รหัสพนักงาน ของตัวเองให้ถูกต้อง" + "\n" + "เพื่อยืนยันตัวตนอีกครั้งค่ะ")));
				userLog.setStatusBot(status.FINDEMP);
				break;
			}
			default:
				log.info("Return echo message %s : %s", replyToken, text);
			}
		} else {
			this.push(event.getSource().getSenderId(), Arrays.asList(new TextMessage("บอทหลับอยู่")));
			this.reply(replyToken, new StickerMessage("1", "17"));
		}

		userMap.put(event.getSource().getSenderId(), userLog);

	}

	private void replyText(@NonNull String replyToken, @NonNull String message) {
		if (replyToken.isEmpty()) {
			throw new IllegalArgumentException("replyToken is not empty");
		}

		if (message.length() > 1000) {
			message = message.substring(0, 1000 - 2) + "...";
		}
		this.reply(replyToken, new TextMessage(message));
	}

	private void reply(@NonNull String replyToken, @NonNull Message message) {
		reply(replyToken, Collections.singletonList(message));
	}

	private void push(@NonNull String replyToken, @NonNull List<Message> messages) {
		try {
			lineMessagingClient.pushMessage(new PushMessage(replyToken, messages)).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	private void reply(@NonNull String replyToken, @NonNull List<Message> messages) {
		try {
			lineMessagingClient.replyMessage(new ReplyMessage(replyToken, messages)).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	private void system(String... args) {
		ProcessBuilder processBuilder = new ProcessBuilder(args);
		try {
			Process start = processBuilder.start();
			int i = start.waitFor();
			log.info("result: {} => {}", Arrays.toString(args), i);
		} catch (InterruptedException e) {
			log.info("Interrupted", e);
			Thread.currentThread().interrupt();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static DownloadedContent saveContent(String ext, MessageContentResponse response) {
		log.info("Content-type: {}", response);
		DownloadedContent tempFile = createTempFile(ext);
		try (OutputStream outputStream = Files.newOutputStream(tempFile.path)) {
			ByteStreams.copy(response.getStream(), outputStream);
			log.info("Save {}: {}", ext, tempFile);
			return tempFile;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
			
		}
	}

	private static DownloadedContent createTempFile(String ext) {
		String fileName = LocalDateTime.now() + "-" + UUID.randomUUID().toString() + "." + ext;
		Path tempFile = LineApplication.downloadedContentDir.resolve(fileName);
		tempFile.toFile().deleteOnExit();
		return new DownloadedContent(tempFile, createUri("/downloaded/" + tempFile.getFileName()));

	}

	private static String createUri(String path) {
		return ServletUriComponentsBuilder.fromCurrentContextPath().path(path).toUriString();
	}

	@Value
	public static class DownloadedContent {
		Path path;
		String uri;
	}
}
