package com.pico.communication.service;

import java.util.ArrayList;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
//import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.pico.communication.model.Register;
import com.pico.communication.model.UserLog;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Service
public class LoanApprovalService {

	@Autowired
	private DataSource dataSource;
	private NamedParameterJdbcTemplate jdbcTemplate = null;
	private StringBuilder stb = null;

	/* Loan approval ขออนุมัติสินเชื่อ */
	public void approveLoan(Register data) {
	}

	public void saveFirstName(UserLog userLog, String firstName) {
	}

	public void saveLastName(UserLog userLog, String lastName) {
	}

	public void saveTel(UserLog userLog, String telPhone) {
	}

	public void saveEmail(UserLog userLog, String email) {
	}

	public void saveSalary(UserLog userLog, String salary) {
	}

	public void saveCreditType(UserLog userLog, String creditType) {
	}

}
