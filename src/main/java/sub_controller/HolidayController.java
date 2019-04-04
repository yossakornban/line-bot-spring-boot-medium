package sub_controller;

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
import com.iphayao.linebot.LineBotController;
import com.iphayao.linebot.LineRepository;
import com.iphayao.linebot.LineBotController.DownloadedContent;
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

public class HolidayController {

	@Autowired
	private LineMessagingClient lineMessagingClient;

	@Autowired
	private LineRepository lineRepo;

	// private status userLog.setStatusBot(status.DEFAULT); // Default status
	private Map<String, UserLog> userMap = new HashMap<String, UserLog>();

	

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

	public void handleTextContent(String replyToken, Event event, TextMessageContent content, String string) throws IOException {
		UserLog userLog = userMap.get(event.getSource().getSenderId());
		if (userLog == null) {
			userLog = new UserLog(event.getSource().getSenderId(), status.DEFAULT);
			userMap.put(event.getSource().getSenderId(), userLog);
		}
		
		ModelMapper modelMapper = new ModelMapper();
		
		System.out.println("7777777777"+string);
		System.out.println(string);

		if (userLog.getStatusBot().equals(status.DEFAULT)) {
			System.out.println("In Switch Case");
			switch (string) {
			case "ขอทราบ ข้อมูลวันหยุดค่ะ": {
				System.out.println("I am Emperor"+string);
				String pathYamlHome = "asset/sub_select_event.yml";
				String pathImageHome = "asset/sub_select_event.jpg";
				RichMenuHelper.createRichMenu(lineMessagingClient, pathYamlHome, pathImageHome, userLog.getUserID());
				this.reply(replyToken, Arrays.asList(new TextMessage("เลือกเมนูที่ต้องการ ได้เลยค่ะ  😊")));
				System.out.println("Pass Raider Madman");
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
						Arrays.asList(new TextMessage("วันที่ปัจจุบัน คือ  " + " " + dateNowHoliday.format(nowDate)
								+ "\n" + "\n" + "วันหยุดที่จะถึงเร็วๆนี้ ได้เเก่ " + "\n" + "➤ " + day1 + "\n" + "➤ "
								+ day2 + "\n" + "➤ " + day3)));
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

			default:
				this.reply(replyToken, Arrays.asList(new TextMessage("ไม่เข้าใจคำสั่ง")));
			}
		}  else {
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
