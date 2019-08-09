package com.pico.communication.model;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import lombok.Data;

@Data
public class EmailConfig implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String host = "smtp.gmail.com";
	private String port = "587";
	private String user = "tiffa.etax@gmail.com"; 
	private String pass = "etax520124";
	private String from = "tiffa.etax@gmail.com";
	private String subject = "Testttttt";
	private String messageText = "eieieieieieieieiei";
	private List<String> mailTos;
	private List<File> attachments;
	
	public EmailConfig() {
		this.mailTos = new ArrayList<String>();
		this.attachments = new ArrayList<File>();
	}
	
	public String toJson() {
		return new Gson().toJson(this);
	}
	
	public void addAttachment(File file) {
		this.attachments.add(file);
	}
	
	public void clearAttachment() {
		this.attachments.clear();
	}
	
	public void addMailTo(String mailTo) {
		// mailTo = "yossakornban@hotmail.com";
		this.mailTos.add(mailTo);
	}
	
	public void clearMailTo() {
		this.mailTos.clear();
	}
}
