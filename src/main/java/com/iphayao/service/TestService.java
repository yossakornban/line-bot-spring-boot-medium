package com.iphayao.service;

import org.springframework.stereotype.Service;

@Service
public class TestService {

	public String setTest(String name) {
		String text = "I will" + name;
		return text;
	}
	
}
