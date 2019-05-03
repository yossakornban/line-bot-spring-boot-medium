package com.iphayao.linebot;

import java.util.ArrayList;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.iphayao.repository.Holiday_Repo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class DashboardFoodVoteController {
	@Autowired
	private DataSource dataSource;
	private NamedParameterJdbcTemplate jdbcTemplate = null;
	private StringBuilder stb = null;


	@RequestMapping("voteFood/dashboardFoodVote")
	public  static void main(String [] args ) {
		System.out.println("Raider Madmaninthailand@gmail.com");
	}
	
		

}