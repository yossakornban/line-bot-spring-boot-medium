package com.iphayao.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Stack;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iphayao.linebot.model.Food;
import com.iphayao.linebot.model.Holiday;
import com.iphayao.repository.Foods_Repo;;

@Service
public class FoodsService {
	
	@Autowired
	private Foods_Repo foods;
	
	ModelMapper modelMapper;

	
	public String ListAllFoods(){
		modelMapper = new ModelMapper();
		Stack<String> holi_list = new Stack<>();
		ArrayList<Map<String, Object>> foods_all = foods.foodsList();
		foods_all.forEach(record -> {
			Food holi = new Food();
			modelMapper.map(record, holi);
			holi_list.push("\n" + holi.getFood_id() + "  " + holi.getFood_name());
		});
		String Imr ="รายการอาหารค่ะ"+"/n"+ holi_list.toString();
		Imr = Imr.replace("[", "");
		Imr = Imr.replace("]", "");
		Imr = Imr.replace(",", "");
		return Imr;
		
		
	}
	
	
	
	
	
	
	
	
}
