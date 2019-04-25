package com.iphayao.service;

import java.util.ArrayList;
import java.util.Map;
import java.util.Stack;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iphayao.linebot.model.Holiday;
import com.iphayao.repository.LineRepository;

@Service
public class HolidayController {
	
	@Autowired
	private LineRepository lineRepo;
	
	ModelMapper modelMapper;

	public String setTest(String name) {
		String text = "I will "+name;
		return text;
	}
	
	public String getAllHoliday() {
		modelMapper = new ModelMapper();
		Stack<String> holi_list = new Stack<>();
		ArrayList<Map<String, Object>> holiday_all = lineRepo.holidayList();
		holiday_all.forEach(record -> {
			Holiday holi = new Holiday();
			modelMapper.map(record, holi);
			holi_list.push("\n" + "? " + holi.getDate_holiday() + "  " + holi.getName_holiday());
		});

		String Imr = holi_list.toString();
		Imr = Imr.replace("[", "");
		Imr = Imr.replace("]", "");
		Imr = Imr.replace(",", "");
		
		return Imr;
	}
}
