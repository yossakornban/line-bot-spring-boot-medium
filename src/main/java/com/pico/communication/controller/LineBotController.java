package com.pico.communication.controller;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.io.File;
import java.io.FileOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.google.common.io.ByteStreams;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.client.MessageContentResponse;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.PostbackEvent;
import com.linecorp.bot.model.event.message.ImageMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.StickerMessage;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.flex.component.Box;
import com.linecorp.bot.model.message.flex.component.Button;
import com.linecorp.bot.model.message.flex.component.Spacer;
import com.linecorp.bot.model.message.flex.component.Text;
import com.linecorp.bot.model.message.flex.container.Bubble;
import com.linecorp.bot.model.message.flex.unit.FlexFontSize;
import com.linecorp.bot.model.message.flex.unit.FlexLayout;
import com.linecorp.bot.model.message.flex.unit.FlexMarginSize;
import com.linecorp.bot.model.message.template.CarouselColumn;
import com.linecorp.bot.model.message.template.CarouselTemplate;
import com.linecorp.bot.model.message.template.ConfirmTemplate;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import com.pico.communication.Application;
import com.pico.communication.helper.RichMenuHelper;
import com.pico.communication.model.UserLog;
import com.pico.communication.model.UserLog.status;
import com.pico.communication.service.LineService;
import com.pico.communication.service.MyAccountService;
import com.pico.communication.service.SlipPaymentService;
import com.pico.communication.utils.BeanUtils;

import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ComponentScan
@LineMessageHandler
@CrossOrigin
@RestController

public class LineBotController {
	@Autowired
	private LineMessagingClient lineMessagingClient;

	@Autowired
	private LineService lineRepo;

	@Autowired
	private MyAccountService myAccountService;

	@Autowired
	private SlipPaymentService slipPaymentService;

	private File txtFile = null;
	private FileOutputStream fop = null;
	private final String path = System.getProperty("catalina.base") + "/webapps/ROOT/receive/";
//	/home/pico/workspace/dev/backend/content/Receive
	// @Autowired
	// LineSignatureValidator lineSignatureValidator;

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
	public void handleImageMessage(MessageEvent<ImageMessageContent> event) throws Exception {
		
		ImageMessageContent content = event.getMessage();
		MessageContentResponse response = lineMessagingClient.getMessageContent(content.getId()).get();
		byte[] contentInBytes = IOUtils.toByteArray(response.getStream());
		slipPaymentService.slipPayment(contentInBytes, event.getSource().getUserId(), false);
		
	}

	public FlexMessage getFlexMessage(String UserID) {
		final Box bodyBlock = createBodyBlock();
		final Box footerBlock = createFooterBox(UserID);
		final Bubble bubble = Bubble.builder().body(bodyBlock).footer(footerBlock).build();
		return new FlexMessage("Please provide information", bubble);
	}

	private Box createBodyBlock() {
		final Text title = Text.builder().text("กรุณาบันทึกข้อมูลของท่านให้เราทราบ").weight(Text.TextWeight.REGULAR)
				.size(FlexFontSize.Md).build();
		return Box.builder().layout(FlexLayout.VERTICAL).contents(asList(title)).build();
	}

	private Box createFooterBox(String UserID) {
		final Spacer spacer = Spacer.builder().size(FlexMarginSize.XL).build();
		final Button button = Button.builder().style(Button.ButtonStyle.PRIMARY).color("#ffd006")
				.action(new URIAction("กรุณากดปุ่ม", "https://picos.ssweb.ga/lots02;user_line_id=" + UserID)).build();
		return Box.builder().layout(FlexLayout.VERTICAL).contents(asList(spacer, button)).build();
	}

	public FlexMessage getFlexRegister(String UserID) {
		final Box bodyBlock = createBodyBlockRegister();
		final Box footerBlock = createFooterBoxRegister(UserID);
		final Bubble bubble = Bubble.builder().body(bodyBlock).footer(footerBlock).build();
		return new FlexMessage("Please provide information", bubble);
	}

	private Box createBodyBlockRegister() {
		final Text title = Text.builder().text("กรุณากรอกเลขประจำตัวประชาชน").weight(Text.TextWeight.REGULAR)
				.size(FlexFontSize.Md).build();
		return Box.builder().layout(FlexLayout.VERTICAL).contents(asList(title)).build();
	}

	private Box createFooterBoxRegister(String UserID) {
		final Spacer spacer = Spacer.builder().size(FlexMarginSize.XL).build();
		final Button button = Button.builder().style(Button.ButtonStyle.PRIMARY).color("#ffd006")
				.action(new URIAction("กรุณากดปุ่ม", "https://picos.ssweb.ga/register;user_line_id=" + UserID)).build();
		return Box.builder().layout(FlexLayout.VERTICAL).contents(asList(spacer, button)).build();
	}

	private void handleTextContent(String replyToken, Event event, TextMessageContent content) throws IOException {
		UserLog userLog = userMap.get(event.getSource().getSenderId());
		if (userLog == null) {
			userLog = new UserLog(event.getSource().getSenderId(), status.DEFAULT);
			userMap.put(event.getSource().getSenderId(), userLog);
		}

		String text = content.getText();

		if (userLog.getStatusBot().equals(status.DEFAULT)) {
			switch (text) {
			case "ประเมินสินเชื่อ": {
				// ConfirmTemplate confirmTemplate = new ConfirmTemplate("1.กรุณาระบุคำนำหน้า",
				// new MessageAction("นาย", "นาย"), new MessageAction("นางสาว", "นางสาว"));
				// TemplateMessage templateMessage = new TemplateMessage("Confirm alt text",
				// confirmTemplate);

				this.reply(replyToken, getFlexMessage(userLog.getUserID()));
				// userLog.setStatusBot(status.SavePrefix);
				break;
			}
			case "ผูกบัญชี": {
				// ConfirmTemplate confirmTemplate = new ConfirmTemplate("1.กรุณาระบุคำนำหน้า",
				// new MessageAction("นาย", "นาย"), new MessageAction("นางสาว", "นางสาว"));
				// TemplateMessage templateMessage = new TemplateMessage("Confirm alt text",
				// confirmTemplate);

				this.reply(replyToken, getFlexRegister(userLog.getUserID()));
				// userLog.setStatusBot(status.SavePrefix);
				break;
			}
			case "ชำระ": {
				ConfirmTemplate confirmTemplate = new ConfirmTemplate("เลือก",
						new MessageAction("ชำระเบี้ย", "ชำระเบี้ย"), new MessageAction("แจ้งโอนเงิน", "แจ้งโอนเงิน"));
				TemplateMessage templateMessage = new TemplateMessage("Payment", confirmTemplate);
				this.reply(replyToken, templateMessage);
				break;
			}
			case "ชำระเบี้ย": {
				// ArrayList<Map<String, Object>> result =
				// myAccountService.searchMyAccount(userLog);
				NumberFormat mf = NumberFormat.getInstance(new Locale("en", "US"));
				mf.setMaximumFractionDigits(2);
				ArrayList<Map<String, Object>> result = myAccountService.searchPaid(userLog);
				log.info("---------------- " + result.toString());
				System.out.println("---------------- " + result.size());
				if (result.size() > 0) {
					System.out.println(result);
					String name = (String) result.get(0).get("first_name") + " "
							+ (String) result.get(0).get("last_name");
					String Period = result.get(0).get("period").toString();
					userLog.setPeriod(Period);
					String AmountPaid = mf.format(result.get(0).get("total_amount"));
					String lastDate = (String) result.get(0).get("due_date");

					TextMessage tm = new TextMessage("เรียน คุณ " + name + "\n"
							+ "บริษัท เพื่อนแท้ แคปปิตอล จำกัด ขอแจ้งค่าเบี้ย ให้ท่านตามข้อมูลด้านล่าง \n" + "งวดที่:"
							+ Period + "\n" + "ยอดชำระ: " + AmountPaid + " บาท\n" + "โปรดชำระเงินภายใน: " + lastDate);

					String originalContentUrl = "https://us-central1-poc-payment-functions.cloudfunctions.net/webApi/promptpay/0889920035/10.png";
					ImageMessage im = new ImageMessage(originalContentUrl, originalContentUrl);

					this.reply(replyToken, Arrays.asList(tm, im));
				} else {

					TextMessage tm = new TextMessage("ยังไม่มีงวดที่ต้องชำระ สอบถามข้อมูลเพิ่มเติมได้ที่เมนูติดต่อเรา ");
					log.info(userLog.getPeriod());
					userLog.setPeriod(null);
					this.reply(replyToken, Arrays.asList(tm));
				}
				log.info("Return echo message %s : %s", replyToken, text);
				break;
			}
			case "แจ้งโอนเงิน": {
				if(BeanUtils.isNotEmpty(userLog.getPeriod()) ) {
					
					try {
						slipPaymentService.slipPayment(null, event.getSource().getUserId(), true);
					} catch (Exception e) {
						e.printStackTrace();
					}
					this.reply(replyToken,
							Arrays.asList(new TextMessage("กรุณาส่งหลักฐานชำระเงิน")));
				} else {
					this.reply(replyToken,
							Arrays.asList(new TextMessage("ยังไม่มีงวดที่ต้องชำระ สอบถามข้อมูลเพิ่มเติมได้ที่เมนูติดต่อเรา")));
					
				}
		
				break;
			}
			case "Flex Back": {

				RichMenuHelper.deleteRichMenu(lineMessagingClient, userLog.getUserID());
				break;
			}

			case "ประวัติการชำระ": {
				// this.push(userLog.getUserID(), Arrays.asList(new TextMessage(
				// " บริษัท เพื่อนแท้ แคปปิตอล จำกัด ขออนุญาติแจ้งประวัติชำระเบี้ย
				// ตามข้อมูลด้านล่าง")));
				myAccountService.searchHis(userLog);
				break;
			}
//			case "carousel": {
//				String imageUrl = createUri("asset/1040.jpg");
//				CarouselTemplate carouselTemplate = new CarouselTemplate(
//						Arrays.asList(new CarouselColumn("", "hoge", "fuga",
//								Arrays.asList(new PostbackAction("言 hello2", "hello こんにちは", "hello こんにちは"),
//										new PostbackAction("言 hello2", "hello こんにちは", "hello こんにちは"),
//										new MessageAction("Say message", "Rice=米")))));
//				TemplateMessage templateMessage = new TemplateMessage("Carousel alt text", carouselTemplate);
//				this.reply(userLog.getUserID(), templateMessage);
//				break;
//			}
			case "Flex": {
				String pathYamlHome = "asset/richmenu-pico.yml";
				String pathImageHome = "asset/pico-menu.jpg";
				RichMenuHelper.createRichMenu(lineMessagingClient, pathYamlHome, pathImageHome, userLog.getUserID());
				break;
			}
			default:
				this.push(userLog.getUserID(), Arrays.asList(new TextMessage("ไม่เข้าใจคำสั่ง")));
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
				this.reply(replyToken, Arrays.asList(new TextMessage(
						"ไม่มีข้อมูลพนักเบื้องต้นในระบบ โปรดกรอกรหัสพนักงานให้ถูกต้อง หรือ ติดต่อผู้ดูแลระบบ  \n @line : http://line.naver.jp/ti/p/-AK9r2Na5E#~ "),
						new TextMessage("กรอก รหัสพนักงาน")));
				userLog.setStatusBot(status.FINDEMP);
			}

		} else if (userLog.getStatusBot().equals(status.FINDCONFIRM)) {
			switch (text) {
			case "Yes": {
				lineRepo.register(userLog, replyToken);
				userLog.setStatusBot(status.DEFAULT);
				this.reply(replyToken, Arrays.asList(new TextMessage("ลงทะเบียนสำเร็จ")));
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

	public void push(@NonNull String replyToken, @NonNull List<Message> messages) {
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

//	private void processWriteFile() throws Exception {
//		txtFile = new File(String.format(pathZip, processDate, midMod100)
//				.concat(String.format("%s-%s", mid, processDate)).concat(".txt"));
//		fop = new FileOutputStream(txtFile, true);
//		logger.info("txtFile---------------------" + txtFile);
//		if (!txtFile.exists()) {
//			txtFile.createNewFile();
//		}
//		byte[] contentInBytes = fileAndZip.toString().getBytes();
//		fop.write(contentInBytes);
//		if (fop != null) {
//			fop.flush();
//			fop.close();
//		}
//	}

	private static DownloadedContent saveContent(String ext, MessageContentResponse response) throws IOException {
		DownloadedContent tempFile = createTempFile(ext);
		try (OutputStream outputStream = Files.newOutputStream(tempFile.path)) {
			ByteStreams.copy(response.getStream(), outputStream);
			log.info("Save {}: {}", ext, tempFile);
			return tempFile;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static DownloadedContent createTempFile(String ext) throws IOException {
		String fileName = UUID.randomUUID().toString() + "." + ext;
		System.out.println("9999999999999 " + fileName);
		Path pathToFile = Files.createTempDirectory("line-bot");
		Path tempFile = pathToFile.resolve(fileName);
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
