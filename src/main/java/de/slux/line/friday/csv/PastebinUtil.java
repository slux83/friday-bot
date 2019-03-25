package de.slux.line.friday.csv;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class PastebinUtil {

	// default user agent to send requests with
	private final static String USER_AGENT = "Mozilla/5.0";
	private final static String SECRET_KEY = "53da4771a90a58b1d2b25dc6338d1c9a";
	private final static String PASTEBIN_POST_URL = "https://pastebin.com/api/api_post.php";

	public static String pasteData(String title, String content) throws IOException {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("api_dev_key", SECRET_KEY);
		parameters.put("api_option", "paste");
		parameters.put("api_paste_name", title);
		parameters.put("api_paste_private", "1");// unlisted
		parameters.put("api_paste_expire_date", "10M");
		parameters.put("api_paste_code", content);

		return makePostRequest(PASTEBIN_POST_URL, parameters);
	}

	/**
	 * Make post request for given URL with given parameters and save response
	 * into a string
	 *
	 * @param url
	 *            HTTPS link to send POST request
	 * @param parameters
	 *            POST request parameters.
	 */
	private static String makePostRequest(String url, Map<String, String> parameters) {
		try {
			StringBuilder sb = new StringBuilder();

			// we need this cookie to submit form
			String initialCookies = getUrlConnection(url, "").getHeaderField("Set-Cookie");
			HttpsURLConnection con = getUrlConnection(url, initialCookies);
			String urlParameters = processRequestParameters(parameters);

			// Send post request
			sendPostParameters(con, urlParameters);

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				sb.append(inputLine);
			}

			in.close();
			return sb.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Send POST parameters to given connection
	 *
	 * @param con
	 *            connection to set parameters on
	 * @param urlParameters
	 *            encoded URL POST parameters
	 * @throws IOException
	 */
	private static void sendPostParameters(URLConnection con, String urlParameters) throws IOException {
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();
	}

	/**
	 * Create HttpsURLConnection for given URL with given Cookies
	 *
	 * @param url
	 *            url to query
	 * @param cookies
	 *            cookies to use for this connection
	 * @return ready-to-use HttpURLConnection
	 * @throws IOException
	 */
	private static HttpsURLConnection getUrlConnection(String url, String cookies) throws IOException {
		HttpsURLConnection con = (HttpsURLConnection) new URL(url).openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Cookie", cookies);
		return con;
	}

	/**
	 * Convert given Map of parameters to URL-encoded string
	 *
	 * @param parameters
	 *            request parameters
	 * @return URL-encoded parameters string
	 */
	private static String processRequestParameters(Map<String, String> parameters) {
		StringBuilder sb = new StringBuilder();
		for (String parameterName : parameters.keySet()) {
			sb.append(parameterName).append('=').append(urlEncode(parameters.get(parameterName))).append('&');
		}
		return sb.substring(0, sb.length() - 1);
	}

	/**
	 * Encode given String with URLEncoder in UTF-8
	 *
	 * @param s
	 *            string to encode
	 * @return URL-encoded string
	 */
	private static String urlEncode(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// This is impossible, UTF-8 is always supported according to the
			// java standard
			throw new RuntimeException(e);
		}
	}
}