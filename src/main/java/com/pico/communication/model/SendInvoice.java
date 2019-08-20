package com.pico.communication.model;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import lombok.Data;

@Data
public class SendInvoice implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String duedate;
	private String communicationType;
	private String invoiceNo;
	}

