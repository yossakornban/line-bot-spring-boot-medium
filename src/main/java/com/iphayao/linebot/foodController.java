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
import java.util.Locale;
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
import com.iphayao.linebot.model.Food;
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
import com.iphayao.repository.Holiday_Repo;
import com.iphayao.repository.LineBot_Repo;
import com.iphayao.repository.Foods_Repo;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

import com.iphayao.LineApplication;

@Slf4j
@ComponentScan
@LineMessageHandler

public class foodController {

	@Autowired
	private LineMessagingClient lineMessagingClient;

	@Autowired
	private LineBot_Repo lineRepo;
	@Autowired
	private Foods_Repo foods;
	@Autowired
	private Holiday_Repo holiday;

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
		this.replyText(replyToken, event.getPostbackContent().getData().toString().replace("date", "")
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

	private static final DateFormat dateNow = new SimpleDateFormat("yyyy-MM-dd");
	private static final DateFormat dateNowHoliday = new SimpleDateFormat("dd/MM/yyyy");
	Date nowDate = new Date();

	private void handleTextContent(String replyToken, Event event, TextMessageContent content) throws IOException {
		UserLog userLog = userMap.get(event.getSource().getSenderId());
		if (userLog == null) {
			userLog = new UserLog(event.getSource().getSenderId(), status.DEFAULT);
			userMap.put(event.getSource().getSenderId(), userLog);
		}
		String text = content.getText();
		ModelMapper modelMapper = new ModelMapper();
		// userLog.setEmpCode(text.toString());
		userLog.setFoodName(text.toString());
		String empName = lineRepo.findEmp(text.toString());
		String foodName = foods.findFoods(text.toString());

		if (userLog.getStatusBot().equals(status.DEFAULT)) {
			switch (text) {
			case "ขอดูรายการอาหารทั้งหมดค่ะ": {
				Stack<String> holi_list = new Stack<>();
				ArrayList<Map<String, Object>> foods_all = foods.foodsList();
				foods_all.forEach(record -> {
					Food holi = new Food();
					modelMapper.map(record, holi);
					holi_list.push("\n" + holi.getFood_id() + "  " + holi.getFood_name());
				});
				String Imr = holi_list.toString();
				Imr = Imr.replace("[", "");
				Imr = Imr.replace("]", "");
				Imr = Imr.replace(",", "");
				this.reply(replyToken, Arrays.asList(new TextMessage("รายการอาหารทั้งหมดค่ะ  " + "\n" + Imr)));
				userLog.setStatusBot(status.DEFAULT);
				break;
			}
			
			
			case "โหวตอาหารประจำสัปดาห์": {
				String pathYamlHome = "asset/foodVote.yml";
				String pathImageHome = "asset/foodVote.jpg";
				RichMenuHelper.createRichMenu(lineMessagingClient, pathYamlHome, pathImageHome, userLog.getUserID());
				this.reply(replyToken, Arrays.asList(new TextMessage("เลือกเมนูที่ต้องการ ได้เลยค่ะ  ??")));
				userLog.setStatusBot(status.DEFAULT);
				break;
			}
			
			
			case "ย้อนกลับค่ะ": {
				String pathYamlHome = "asset/select_event.yml";
				String pathImageHome = "asset/select_event.jpg";
				RichMenuHelper.createRichMenu(lineMessagingClient, pathYamlHome, pathImageHome, userLog.getUserID());
				this.reply(replyToken, Arrays.asList(new TextMessage("เลือกเมนูที่ต้องการ ได้เลยค่ะ  ??")));
				userLog.setStatusBot(status.DEFAULT);
				break;
			}
			
			case "โหวตอาหาร": {
				foods.CountVote(userLog);
				if (userLog.getCountVout_CheckPossilibity() >= 10) {
					this.reply(replyToken, Arrays.asList(new TextMessage(
							"คุณโหวตอาหารครบ 10 รายการสำหรับอาทิตย์นี่เเล้วค่ะ   กรุณารออาทิตย์ถัดไปสำหรับการโหวตครั้งใหม่นะคะ")));
					userLog.setStatusBot(status.DEFAULT);
				} else {
					this.reply(replyToken,
							Arrays.asList(new TextMessage("ใส่ หมายเลขอาหาร ที่ต้องการโหวตได้เลยค่ะ  ??")));
					userLog.setStatusBot(status.VOTE_FOODS);
				}

				break;
			}
			default:
				this.reply(replyToken, Arrays.asList(new TextMessage("ไม่เข้าใจคำสั่ง")));
			}
		} else if (userLog.getStatusBot().equals(status.VOTE_FOODS)) {
			switch (text) {
			case "ย้อนกลับค่ะ": {
				String pathYamlHome = "asset/select_event.yml";
				String pathImageHome = "asset/select_event.jpg";
				RichMenuHelper.createRichMenu(lineMessagingClient, pathYamlHome, pathImageHome, userLog.getUserID());
				this.reply(replyToken, Arrays.asList(new TextMessage("เลือกเมนูที่ต้องการ ได้เลยค่ะ  ??")));
				userLog.setStatusBot(status.DEFAULT);
				break;
			}
			}
			foods.CountVote(userLog);
			if (foodName == null) {
				switch (text) {
				case "ขอดูรายการอาหารทั้งหมดค่ะ": {

					switch (text) {
					case "โหวตอาหาร": {
						foods.CountVote(userLog);
						if (userLog.getCountVout_CheckPossilibity() >= 10) {
							this.reply(replyToken, Arrays.asList(new TextMessage(
									"คุณโหวตอาหารครบ 10 รายการสำหรับอาทิตย์นี่เเล้วค่ะ   กรุณารออาทิตย์ถัดไปสำหรับการโหวตครั้งใหม่นะคะ")));
							userLog.setStatusBot(status.DEFAULT);
						} else {
							this.reply(replyToken,
									Arrays.asList(new TextMessage("ใส่ หมายเลขอาหาร ที่ต้องการโหวตได้เลยค่ะ  ??")));
							userLog.setStatusBot(status.VOTE_FOODS);
						}

						break;
					}
				}
					Stack<String> foodsList = new Stack<>();
					ArrayList<Map<String, Object>> foods_all = foods.foodsList();
					foods_all.forEach(record -> {
						Food foods = new Food();
						modelMapper.map(record, foods);
						foodsList.push("\n" + foods.getFood_id() + "  " + foods.getFood_name());
					});
					String Imr = foodsList.toString();
					Imr = Imr.replace("[", "");
					Imr = Imr.replace("]", "");
					Imr = Imr.replace(",", "");
					this.reply(replyToken, Arrays.asList(new TextMessage("รายการอาหารทั้งหมดค่ะ  " + "\n" + Imr)));
					userLog.setStatusBot(status.VOTE_FOODS);
					break;
				}
				}
				this.reply(replyToken,
						Arrays.asList(new TextMessage("ไม่พบรายาร อาหารดังกล่าว กรุณา ใส่รหัสอาหารอีกครั้งค่ะ")));
				userLog.setStatusBot(status.VOTE_FOODS);

			} else if (text != null && text == userLog.getFoodName()) {
				if (userLog.getCountVout_CheckPossilibity() >= 10) {
					this.reply(replyToken, Arrays.asList(new TextMessage(
							"คุณโหวตอาหารครบ 10 รายการสำหรับอาทิตย์นี่เเล้วค่ะ   กรุณารออาทิตย์ถัดไปสำหรับการโหวตครั้งใหม่นะคะ")));
					userLog.setStatusBot(status.DEFAULT);
				} else {
					// ----------------------------------------------------------------------------------------------------------Focus

					userLog.setFoodId(text.toString());
					foods.saveFood(userLog);
					Calendar c = Calendar.getInstance();
					Date now = new Date();
					SimpleDateFormat simpleDateformat = new SimpleDateFormat("MM");
					LocalDate today = LocalDate.now();
					// Go backward to get Monday
					LocalDate monday = today;
					while (monday.getDayOfWeek() != DayOfWeek.MONDAY) {
						monday = monday.minusDays(1);
					}
					// Go forward to get Sunday
					LocalDate sunday = today;
					while (sunday.getDayOfWeek() != DayOfWeek.SUNDAY) {
						sunday = sunday.plusDays(1);
					}
					int limitVOte = 9;
					int stopVote = limitVOte - userLog.getCountVout_CheckPossilibity();
					this.reply(replyToken, Arrays.asList(new TextMessage("คุณโหวต  " + "\n" + "( " + foodName + "  )"
							+ "\n" + "ประจำสัปดาห์ที่ " + DateTimeFormatter.ofPattern("dd", Locale.CHINA).format(monday)
							+ "-" + DateTimeFormatter.ofPattern("dd", Locale.CHINA).format(sunday) + "/"
							+ simpleDateformat.format(now) + "/" + c.get(Calendar.YEAR) + "\n"
							+ "เหลือสิทธ์ในการโหวตอีก" + stopVote + "ครั้ง")));
					userLog.setStatusBot(status.VOTE_FOODS);
				}

			} else {
				this.reply(replyToken, Arrays.asList(new TextMessage("นอน โว้ยยยย")));
				userLog.setStatusBot(status.VOTE_FOODS);
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
			userLog.setEmpCode(text.toString());
			if (empName != null) {

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
				lineRepo.register(userLog);
				userLog.setStatusBot(status.DEFAULT);
				String pathYamlHome = "asset/select_event.yml";
				String pathImageHome = "asset/select_event.jpg";
				RichMenuHelper.createRichMenu(lineMessagingClient, pathYamlHome, pathImageHome, userLog.getUserID());
				this.reply(replyToken, Arrays.asList(new TextMessage(
						"ลงทะเบียนสำเร็จ  " + "\n" + "กรุณา  เลือกเมนู ที่ต้องการทำรายการ ได้เลยค่ะ  ??")));
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
