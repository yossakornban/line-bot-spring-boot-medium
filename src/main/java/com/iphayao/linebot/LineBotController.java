package com.iphayao.linebot;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

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
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.client.MessageContentResponse;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.DatetimePickerAction;
import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.ImageMessageContent;
import com.linecorp.bot.model.event.message.LocationMessageContent;
import com.linecorp.bot.model.event.message.StickerMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.LocationMessage;
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

	@EventMapping
	public void handleTextMessage(MessageEvent<TextMessageContent> event) throws IOException {
		log.info(event.toString());
		TextMessageContent message = event.getMessage();
		handleTextContent(event.getReplyToken(), event, message);
	}

	@EventMapping
	public void handleStickerMessage(MessageEvent<StickerMessageContent> event) {
		log.info(event.toString());
		StickerMessageContent message = event.getMessage();
		reply(event.getReplyToken(), new StickerMessage(message.getPackageId(), message.getStickerId()));
	}

	@EventMapping
	public void handleLocationMessage(MessageEvent<LocationMessageContent> event) {
		log.info(event.toString());
		LocationMessageContent message = event.getMessage();
		reply(event.getReplyToken(),
				new LocationMessage((message.getTitle() == null) ? "Location replied" : message.getTitle(),
						message.getAddress(), message.getLatitude(), message.getLongitude()));
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
		String text = content.getText();
		String userId = event.getSource().getSenderId();

		log.info("Got text message from %s : %s", replyToken, text);

		switch (text) {
		case "Flex": {
			String pathYamlHome = "asset/richmenu-home.yml";
			String pathImageHome = "asset/richmenu-home.jpg";
//			String pathImageFlex = new ClassPathResource("asset/richmenu-home.png").getFile().getAbsolutePath();
//			String pathConfigFlex = new ClassPathResource("asset/richmenu-home.yml").getFile().getAbsolutePath();
			RichMenuHelper.createRichMenu(lineMessagingClient, pathYamlHome, pathImageHome, userId);
			break;
		}
		case "Flex Back": {

			RichMenuHelper.deleteRichMenu(lineMessagingClient, userId);
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
            String imageUrl = createUri("/asset/1040.jpg");
            CarouselTemplate carouselTemplate = new CarouselTemplate(
                    Arrays.asList(
                            new CarouselColumn(imageUrl, "hoge", "fuga", Arrays.asList(
                                    new URIAction("Go to line.me",
                                                  "https://line.me"),
                                    new URIAction("Go to line.me",
                                                  "https://line.me"),
                                    new PostbackAction("Say hello1",
                                                       "hello こんにちは")
                            )),
                            new CarouselColumn(imageUrl, "hoge", "fuga", Arrays.asList(
                                    new PostbackAction("言 hello2",
                                                       "hello こんにちは",
                                                       "hello こんにちは"),
                                    new PostbackAction("言 hello2",
                                                       "hello こんにちは",
                                                       "hello こんにちは"),
                                    new MessageAction("Say message",
                                                      "Rice=米")
                            )),
                            new CarouselColumn(imageUrl, "Datetime Picker",
                                               "Please select a date, time or datetime", Arrays.asList(
                                    new DatetimePickerAction("Datetime",
                                                             "action=sel",
                                                             "datetime",
                                                             "2017-06-18T06:15",
                                                             "2100-12-31T23:59",
                                                             "1900-01-01T00:00"),
                                    new DatetimePickerAction("Date",
                                                             "action=sel&only=date",
                                                             "date",
                                                             "2017-06-18",
                                                             "2100-12-31",
                                                             "1900-01-01"),
                                    new DatetimePickerAction("Time",
                                                             "action=sel&only=time",
                                                             "time",
                                                             "06:15",
                                                             "23:59",
                                                             "00:00")
                            ))
                    ));
            TemplateMessage templateMessage = new TemplateMessage("Carousel alt text", carouselTemplate);
            this.reply(replyToken, templateMessage);
            break;
        }
		case "Profile": {
			if (userId != null) {
				lineMessagingClient.getProfile(userId).whenComplete((profile, throwable) -> {
					if (throwable != null) {
						this.replyText(replyToken, throwable.getMessage());
						return;
					}
					this.reply(replyToken,
							Arrays.asList(new TextMessage("Display name: " + profile.getDisplayName()),
									new TextMessage("Status message: " + profile.getStatusMessage()),
									new TextMessage("User ID: " + profile.getUserId())));
				});
			}
			break;
		}
		default:
			log.info("Return echo message %s : %s", replyToken, text);
			this.replyText(replyToken, text);
		}
	}

//	private void handleStickerContent(String replyToken, StickerMessageContent content) {
//		reply(replyToken, new StickerMessage(content.getPackageId(), content.getStickerId()));
//	}

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
