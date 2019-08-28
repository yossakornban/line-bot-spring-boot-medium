package com.pico.communication.service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.sql.DataSource;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
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

	public String sendGet() throws Exception {

		URL url = new URL("https://picos.ssweb.ga/identity/.well-known/openid-configuration/jwks");
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

		String inputLine;
		while ((inputLine = in.readLine()) != null)
			System.out.println(inputLine);
		in.close();

//		String url = "https://picos.ssweb.ga/identity/.well-known/openid-configuration/jwks";
//		System.out.println("********************************************");
//		System.out.println(url);
//		HttpClient client = new DefaultHttpClient();
//		HttpGet request = new HttpGet(url);
//
//		// add request header
//		request.addHeader("User-Agent", "Mozilla/4.76");
//
//		HttpResponse response = client.execute(request);
//
//		System.out.println("\nSending 'GET' request to URL : " + url);
//		System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
//		System.out.println("Response Code : " + response.getStatusLine().getReasonPhrase());
//		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
//
//		StringBuffer result = new StringBuffer();
//		String line = "";
//		while ((line = rd.readLine()) != null) {
//			result.append(line);
//		}
//
//		System.out.println(result.toString());
		return inputLine;

	}

	public static String executePost() {

//		byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
//		int    postDataLength = postData.length;
//		String request        = "http://example.com/index.php";
//		URL    url            = new URL( request );
//		HttpURLConnection conn= (HttpURLConnection) url.openConnection();           
//		conn.setDoOutput( true );
//		conn.setInstanceFollowRedirects( false );
//		conn.setRequestMethod( "POST" );
//		conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded"); 
//		conn.setRequestProperty( "charset", "utf-8");
//		conn.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
//		conn.setUseCaches( false );
//		try( DataOutputStream wr = new DataOutputStream( conn.getOutputStream())) {
//		   wr.write( postData );
//		}
//		

//		String urlParameters = "grant_type=password&scope=openid pico.profile offline_access loan system&username=admin&password=admin&client_id=spa";
		HttpURLConnection connection = null;

		try {
			// Create connection
			URL url = new URL("http://pico.ssweb.ga/identity/.well-known/openid-configuration/jwks");
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
			connection.addRequestProperty("User-Agent", "Mozilla/4.76");
//			connection.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));
			connection.setRequestProperty("Content-Language", "en-US");

			connection.setUseCaches(false);
			connection.setDoOutput(true);

			// Send request
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
//		    wr.writeBytes(urlParameters);
			wr.close();

			// Get Response
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
			String line;
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();
			return response.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
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
