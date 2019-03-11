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
import java.util.List;
import java.util.Map;
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
import com.iphayao.linebot.model.Entity;
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
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ComponentScan
@LineMessageHandler
public class LineBotController {
	@Autowired
	private LineMessagingClient lineMessagingClient;

	@Autowired
	private LineRepository lineRepo;
	
	private status statusBot = status.CLOSE; // Default status
	private String userID = "";

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

	private void handleTextContent(String replyToken, Event event, TextMessageContent content) throws IOException {
		UserLog userLog = new UserLog();
		userLog.setUserID(event.getSource().getSenderId());

		
		String text = content.getText();
//		userID = event.getSource().getSenderId();
		ModelMapper modelMapper = new ModelMapper();

//	    public static void main(String[] args) {
//	        Task t = new Task("Washing up");
//	        t.setPriority(Priority.HIGH);
//	        System.out.println(t.getName()); // Washing up
//	        System.out.println(t.getPriority().toString()); // This gets the string of HIGH
//	        System.out.println(t.getPriority().ordinal()); // this gives 4
//	    }

		switch (text) {
		case "call": {
			this.push(event.getSource().getSenderId(), Arrays.asList(new TextMessage("สวัสดีจ้า ยินดีให้บริการรรรร")));
			this.reply(replyToken, new StickerMessage("1", "17"));
//			statusBot = status.CALL;
			userLog.setStatusBot(status.CALL);
			break;
		}
		case "close": {
			this.push(event.getSource().getSenderId(), Arrays.asList(new TextMessage("ไปละน้าาาา บายยยยย")));
			this.reply(replyToken, new StickerMessage("1", "108"));
			userLog.setStatusBot(status.CLOSE);
//			statusBot = status.CLOSE;
			break;
		}
		default:
			log.info("Return echo message %s : %s", replyToken, text); 
		}
		
		System.out.println("11111 "+event.getSource().getSenderId());
		System.out.println("22222 "+userLog.getUserID());
		System.out.println("44444 "+userLog.getStatusBot());

		if (userLog.getUserID() == event.getSource().getSenderId() && userLog.getStatusBot().equals(status.CALL)) {
			switch (text) {
			case "add": {
				this.reply(replyToken,
						Arrays.asList(new TextMessage("Format การเพิ่ม Agenda "),
								new TextMessage("วาระการประชุม " + " ห้องประชุม" + " วันและเวลา" + " โดย "),
								new TextMessage("พิมพ์   cancel : ยกเลิก ")));
				statusBot = status.SAVE;
				break;
			}
			case "list": {
				ArrayList<Map<String, Object>> list = lineRepo.list();
				list.forEach(record -> {
					Entity en = new Entity();
					modelMapper.map(record, en);
					this.push(replyToken, Arrays.asList(new TextMessage(en.getMessage())));
				});
				statusBot = status.CALL;
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
				statusBot = status.CALL;
				break;
			}
			case "leave": {
				String imageUrl = createUri("/static/buttons/1040.jpg");
				CarouselTemplate carouselTemplate = new CarouselTemplate(Arrays.asList(new CarouselColumn(imageUrl,
						"ประเภทการลา", "กรุณาเลือก", Arrays.asList(new MessageAction("ลากิจ", "1"),
								new MessageAction("ลาป่วย", "2"), new MessageAction("ลาพักร้อน", "3")))));
				TemplateMessage templateMessage = new TemplateMessage("Carousel alt text", carouselTemplate);
				this.reply(replyToken, templateMessage);
				statusBot = status.Q11;
				break;
			}
			case "help": {
				this.reply(replyToken, Arrays.asList(new TextMessage(
						"โปรดเลือกรายการ \n พิมพ์  profile : ดูข้อมูล Profile  \n พิมพ์  list : ดู Agenda \n พิมพ์  add : เพิ่ม Agenda")));
				statusBot = status.CALL;;
				break;
			}
			case "Flex": {
				String pathYamlHome = "asset/richmenu-home.yml";
				String pathImageHome = "asset/richmenu-home.jpg";
				RichMenuHelper.createRichMenu(lineMessagingClient, pathYamlHome, pathImageHome, userID);
				break;
			}
			case "Flex Back": {

				RichMenuHelper.deleteRichMenu(lineMessagingClient, userID);
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
				log.info("Return echo message %s : %s", replyToken, text);
			}

		} else if (statusBot.equals(status.SAVE)) {
			switch (text) {
			case "cancel": {
				this.reply(replyToken, Arrays.asList(new TextMessage("ยกเลิกสำเร็จ ")));
				statusBot = status.CALL;
				break;
			}
			default:
				lineRepo.save(text.toString());
				this.reply(replyToken, Arrays.asList(new TextMessage("บันทึกสำเร็จ ")));
				statusBot = status.CALL;
			}
		} else if (statusBot.equals(status.Q11)) {
			switch (text) {
			case "1": {
				log.info("Return echo message %s : %s", replyToken, text);
				statusBot = status.CALL;
				break;
			}
			case "2": {
				log.info("Return echo message %s : %s", replyToken, text);
				statusBot = status.CALL;
				break;
			}
			case "3": {
				log.info("Return echo message %s : %s", replyToken, text);
				statusBot = status.CALL;
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
				statusBot = status.Q11;
			}
		} else {
			this.push(event.getSource().getSenderId(), Arrays.asList(new TextMessage("บอทหลับอยู่")));
			this.reply(replyToken, new StickerMessage("1", "17"));
		}

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
