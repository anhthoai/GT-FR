package ucf.firebase;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

import org.json.JSONObject;

/**
 * Minimal FCM HTTP v1 client (no external deps).
 * Uses OAuth2 access token generated from a Firebase service account JSON.
 *
 * This avoids relying on legacy FCM server keys (which may be disabled).
 */
public class FcmV1Client {
	private static final String SCOPE = "https://www.googleapis.com/auth/firebase.messaging";

	private final String projectId;
	private final String clientEmail;
	private final String tokenUri;
	private final PrivateKey privateKey;

	private volatile String accessToken;
	private volatile long accessTokenExpiresAtMs;

	public FcmV1Client(String projectId, String clientEmail, String tokenUri, PrivateKey privateKey) {
		this.projectId = projectId;
		this.clientEmail = clientEmail;
		this.tokenUri = tokenUri;
		this.privateKey = privateKey;
	}

	public static FcmV1Client fromServiceAccountJsonPath(String jsonPath) throws Exception {
		String json = readAll(new FileInputStream(jsonPath));
		JSONObject obj = new JSONObject(json);
		String projectId = obj.getString("project_id");
		String clientEmail = obj.getString("client_email");
		String tokenUri = obj.getString("token_uri");
		String privateKeyPem = obj.getString("private_key");

		PrivateKey privateKey = parsePkcs8PrivateKey(privateKeyPem);
		return new FcmV1Client(projectId, clientEmail, tokenUri, privateKey);
	}

	public SendResult sendToToken(String token, String title, String body, Map<String, String> data) throws Exception {
		String at = getAccessToken();

		JSONObject message = new JSONObject();
		message.put("token", token);

		// IMPORTANT:
		// We intentionally send DATA-ONLY messages (no "notification" object) so Flutter's
		// onBackgroundMessage handler runs and can persist the event + show a rich local notification.
		JSONObject d = new JSONObject();
		if (data != null && !data.isEmpty()) {
			for (Map.Entry<String, String> e : data.entrySet()) {
				d.put(e.getKey(), e.getValue());
			}
		}
		if (title != null) d.put("title", title);
		if (body != null) d.put("body", body);
		message.put("data", d);

		// Ensure Android receives data promptly.
		JSONObject android = new JSONObject();
		android.put("priority", "HIGH");
		message.put("android", android);

		JSONObject payload = new JSONObject();
		payload.put("message", message);

		URL url = new URL("https://fcm.googleapis.com/v1/projects/" + projectId + "/messages:send");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setUseCaches(false);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Authorization", "Bearer " + at);
		conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

		try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8))) {
			w.write(payload.toString());
		}

		int code = conn.getResponseCode();
		String okBody = readInputStream(conn.getInputStream());
		String errBody = readInputStream(conn.getErrorStream());
		return new SendResult(code, okBody, errBody);
	}

	private String getAccessToken() throws Exception {
		long now = System.currentTimeMillis();
		String cur = accessToken;
		if (cur != null && now < (accessTokenExpiresAtMs - 60_000)) {
			return cur;
		}
		synchronized (this) {
			now = System.currentTimeMillis();
			cur = accessToken;
			if (cur != null && now < (accessTokenExpiresAtMs - 60_000)) {
				return cur;
			}
			TokenResponse tr = fetchAccessToken();
			accessToken = tr.accessToken;
			accessTokenExpiresAtMs = tr.expiresAtMs;
			return accessToken;
		}
	}

	private TokenResponse fetchAccessToken() throws Exception {
		long nowSec = Instant.now().getEpochSecond();
		long expSec = nowSec + 3600;

		JSONObject header = new JSONObject();
		header.put("alg", "RS256");
		header.put("typ", "JWT");

		JSONObject claims = new JSONObject();
		claims.put("iss", clientEmail);
		claims.put("scope", SCOPE);
		claims.put("aud", tokenUri);
		claims.put("iat", nowSec);
		claims.put("exp", expSec);

		String jwtUnsigned = base64Url(header.toString()) + "." + base64Url(claims.toString());
		byte[] sig = sign(jwtUnsigned.getBytes(StandardCharsets.UTF_8), privateKey);
		String jwt = jwtUnsigned + "." + base64Url(sig);

		String body = "grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Ajwt-bearer&assertion=" + urlEncode(jwt);
		URL url = new URL(tokenUri);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setUseCaches(false);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

		try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8))) {
			w.write(body);
		}

		int code = conn.getResponseCode();
		String resp = readInputStream(code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream());
		if (code < 200 || code >= 300) {
			throw new IOException("OAuth token request failed: " + code + " body=" + resp);
		}

		JSONObject json = new JSONObject(resp);
		String accessToken = json.getString("access_token");
		long expiresIn = json.optLong("expires_in", 3600);
		return new TokenResponse(accessToken, System.currentTimeMillis() + (expiresIn * 1000));
	}

	private static byte[] sign(byte[] input, PrivateKey pk) throws Exception {
		Signature s = Signature.getInstance("SHA256withRSA");
		s.initSign(pk);
		s.update(input);
		return s.sign();
	}

	private static PrivateKey parsePkcs8PrivateKey(String pem) throws Exception {
		String cleaned = pem
				.replace("-----BEGIN PRIVATE KEY-----", "")
				.replace("-----END PRIVATE KEY-----", "")
				.replaceAll("\\s+", "");
		byte[] der = Base64.getDecoder().decode(cleaned);
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(der);
		return KeyFactory.getInstance("RSA").generatePrivate(spec);
	}

	private static String base64Url(String s) {
		return base64Url(s.getBytes(StandardCharsets.UTF_8));
	}

	private static String base64Url(byte[] bytes) {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private static String urlEncode(String s) {
		// Minimal URL encoding for query/form usage (RFC 3986-ish)
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '-' || c == '_' || c == '.' || c == '~') {
				out.append(c);
			} else {
				out.append('%');
				out.append(String.format("%02X", (int) c));
			}
		}
		return out.toString();
	}

	private static String readAll(InputStream is) throws IOException {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) sb.append(line).append("\n");
			return sb.toString();
		}
	}

	private static String readInputStream(InputStream inputStream) {
		if (inputStream == null) return "";
		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
			StringBuilder s = new StringBuilder();
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				s.append(line);
			}
			return s.toString();
		} catch (IOException e) {
			return "";
		}
	}

	public static class SendResult {
		public final int httpCode;
		public final String successBody;
		public final String errorBody;

		public SendResult(int httpCode, String successBody, String errorBody) {
			this.httpCode = httpCode;
			this.successBody = successBody;
			this.errorBody = errorBody;
		}
	}

	private static class TokenResponse {
		final String accessToken;
		final long expiresAtMs;

		TokenResponse(String accessToken, long expiresAtMs) {
			this.accessToken = accessToken;
			this.expiresAtMs = expiresAtMs;
		}
	}
}


