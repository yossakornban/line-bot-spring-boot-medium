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
		this.replyText(replyToken, "Got postback data " + event.getPostbackContent().getData() + ", param "
				+ event.getPostbackContent().getParams().toString());
	}

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

	private static final DateFormat dateNow = new SimpleDateFormat("dd/MM/yyy ");// ----------------------------------------------------------------------------DateNow

	private void handleTextContent(String replyToken, Event event, TextMessageContent content) throws IOException {
		UserLog userLog = userMap.get(event.getSource().getSenderId());

		if (userLog == null) {
			userLog = new UserLog(event.getSource().getSenderId(), status.DEFAULT);
			userMap.put(event.getSource().getSenderId(), userLog);
		}

		System.out.println("+++++ " + userMap.get(event.getSource().getSenderId()).toString());

		String text = content.getText();
		ModelMapper modelMapper = new ModelMapper();

		if (userLog.getStatusBot().equals(status.DEFAULT)) {
			switch (text) {
			case "register": {
				this.reply(replyToken, Arrays.asList(new TextMessage("กรอก รหัสพนักงาน")));
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
			case "ขอลาหยุดครับผม": {

				System.out.println("Request to leaves");
				break;

			}
			// ------------------------------------------------------------Get
			// all
			// Holidays------------------------------------------------------------------------
			case "ขอทราบวันหยุดประจำปีค่ะ": {
				String pathYamlHome = "asset/sub_select_event.yml";
				String pathImageHome = "asset/sub_select_event.jpg";
				RichMenuHelper.createRichMenu(lineMessagingClient, pathYamlHome, pathImageHome, userLog.getUserID());

				Stack<String> holi_list = new Stack<>();
				ArrayList<Map<String, Object>> holiday_all = lineRepo.holidayList();
				holiday_all.forEach(record -> {
					Holiday holi = new Holiday();
					modelMapper.map(record, holi);

					holi_list.push("\n" + holi.getDate_holiday() + "  " + holi.getName_holiday());

				});
				String Imr = holi_list.toString();
				Imr = Imr.replace("[", "");
				Imr = Imr.replace("]", "");
				Imr = Imr.replace(",", "");
				this.reply(replyToken,
						Arrays.asList(new TextMessage("ข้อมูลวันหยุดประจำปี ได้เเล้วค่ะ ^^" + "\n" + Imr)));

				userLog.setStatusBot(status.DEFAULT);
				break;
			}
			// ----------------------------------------------------------------------------------------------------------------Find
			// Three day holiday
			case "ขอทราบวันหยุดที่จะมาถึงเร็วๆนี้ ค่ะ": {

				Stack<String> holiday_soon = new Stack<>();
				ArrayList<Map<String, Object>> soon_all = lineRepo.Holiday_soon();
				soon_all.forEach(record -> {
					Holiday getHolidaySoon = new Holiday();
					modelMapper.map(record, getHolidaySoon);

					holiday_soon.push("\n" + getHolidaySoon.getDate_holiday() + "  " + getHolidaySoon.getName_holiday());

				});
				System.out.print(soon_all);
				String show_soon = holiday_soon.toString();
				show_soon = show_soon.replace("[", "");
				show_soon = show_soon.replace("]", "");
				show_soon = show_soon.replace(",", "");
				this.reply(replyToken,
						Arrays.asList(new TextMessage("ข้อมูลวันหยุดประจำปี ได้เเล้วค่ะ ^^" + "\n" + show_soon)));

				
				userLog.setStatusBot(status.DEFAULT);
				break;
			}

			case "ย้อนกลับโว้ย": {
				String pathYamlHome = "asset/select_event.yml";
				String pathImageHome = "asset/select_event.jpg";
				RichMenuHelper.createRichMenu(lineMessagingClient, pathYamlHome, pathImageHome, userLog.getUserID());
				this.reply(replyToken, Arrays.asList(new TextMessage("กลับ มาให้ เเล้วไอ้ชาติหมา!")));
				userLog.setStatusBot(status.DEFAULT);
				break;
			}

			
//			threeday =threeday.replace("[", "");
//			threeday =threeday.replace("{", "");
//			threeday =threeday.replace("name_holiday=", "");
//			threeday =threeday.replace("}", "");
//			threeday =threeday.replace("]", "");
//			threeday =threeday.replace(",", "");
//			threeday =threeday.replace("(", "");
//			threeday =threeday.replace(")", "");
//			threeday =threeday.replace("to_date=", "");
//			threeday =threeday.replace("-", "/");
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
			case "leave": {
				String imageUrl = createUri("/static/buttons/1040.jpg");
				CarouselTemplate carouselTemplate = new CarouselTemplate(Arrays.asList(new CarouselColumn(imageUrl,
						"ประเภทการลา", "กรุณาเลือก", Arrays.asList(new MessageAction("ลากิจ", "1"),
								new MessageAction("ลาป่วย", "2"), new MessageAction("ลาพักร้อน", "3")))));
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
			case "Flex Back": {

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
										new DatetimePickerAction("Date", "action=sel&only=date", "date", "2017-06-18",
												"2100-12-31", "1900-01-01"),
										new DatetimePickerAction("Time", "action=sel&only=time", "time", "06:15",
												"23:59", "00:00")))));
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
			case "1": {
				log.info("Return echo message %s : %s", replyToken, text);
				userLog.setStatusBot(status.DEFAULT);
				break;
			}
			case "2": {
				log.info("Return echo message %s : %s", replyToken, text);
				userLog.setStatusBot(status.DEFAULT);
				break;
			}
			case "3": {

				userLog.setStatusBot(status.DEFAULT);
				break;
			}
			default:
				log.info("Return echo message %s : %s", replyToken, text);
				String imageUrl = createUri("/static/buttons/1040.jpg");
				CarouselTemplate carouselTemplate = new CarouselTemplate(Arrays.asList(new CarouselColumn(imageUrl,
						"ประเภทการลา", "กรุณาเลือก", Arrays.asList(new MessageAction("ลากิจ", "1"),
								new MessageAction("ลาป่วย", "2"), new MessageAction("ลาพักร้อน", "3")))));
				TemplateMessage templateMessage = new TemplateMessage("Carousel alt text", carouselTemplate);
				this.reply(replyToken, templateMessage);
				userLog.setStatusBot(status.Q11);
			}
		} else if (userLog.getStatusBot().equals(status.FINDEMP)) {
			userLog.setEmpCode(text.toString());
			String empName = lineRepo.findEmp(text.toString());
			if (empName != null) {
				ConfirmTemplate confirmTemplate = new ConfirmTemplate("ยืนยัน, คุณใช่ " + empName + " หรือไม่ ?",
						new MessageAction("ใช่ !", "Yes"), new MessageAction("ไม่ใช่ !", "No"));
				TemplateMessage templateMessage = new TemplateMessage("Confirm alt text", confirmTemplate);
				this.reply(replyToken, templateMessage);
				userLog.setStatusBot(status.FINDCONFIRM);
			} else {
				this.reply(replyToken,
						Arrays.asList(
								new TextMessage(
										"ไม่มีข้อมูลพนักเบื้องต้นในระบบ โปรดกรอกรหัสพนักงานให้ถูกต้อง หรือ ติดต่อผู้ดูแลระบบ  \n @line : http://line.naver.jp/ti/p/-AK9r2Na5E#~ "),
								new TextMessage("กรอก รหัสพนักงาน")));
				userLog.setStatusBot(status.FINDEMP);
			}

		} else if (userLog.getStatusBot().equals(status.FINDCONFIRM)) {
			switch (text) {
			case "Yes": {
				lineRepo.register(userLog);
				userLog.setStatusBot(status.DEFAULT);
				String pathYamlHome = "asset/select_event.yml";
				String pathImageHome = "asset/select_event.jpg";
				RichMenuHelper.createRichMenu(lineMessagingClient, pathYamlHome, pathImageHome, userLog.getUserID());
				this.reply(replyToken, Arrays.asList(new TextMessage("ลงทะเบียนสำเร็จ  ")));
				break;
			}
			case "No": {
				this.reply(replyToken, Arrays.asList(new TextMessage("พิมพ์ รหัสพนักงาน")));
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
		Path tempFile = Application.downloadedContentDir.resolve(fileName);
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
