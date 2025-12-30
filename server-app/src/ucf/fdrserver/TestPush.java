package ucf.fdrserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import ucf.firebase.FcmV1Client;
import ucf.firebase.Notification;
import ucf.firebase.PushNotifHelper;
import ucf.fdrssutil.MySQLConfig;

/**
 * Debug endpoint to verify push notifications end-to-end.
 *
 * Usage:
 *  GET/POST /FRServer/TestPush?adminid=admin
 *
 * Returns JSON with per-token HTTP v1 results and legacy fallback results.
 */
@WebServlet("/TestPush")
public class TestPush extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();

		String root = getServletContext().getRealPath("/");
		MySQLConfig.userinfo_path = root + "user_config.txt";

		String adminid = request.getParameter("adminid");
		if (adminid == null) adminid = "";
		adminid = adminid.trim();
		if (adminid.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.print(new JSONObject().put("error", "missing adminid").toString());
			return;
		}

		JSONObject res = new JSONObject();
		res.put("adminid", adminid);

		try {
			Connection con = MySQLConfig.getConnection();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("select token from device_lists where adminid='" + adminid + "'");

			int tokenCount = 0;
			JSONObject perToken = new JSONObject();

			String saPath = root + "/FaceRecognition-966aee651648.json";
			FcmV1Client v1 = null;
			try {
				v1 = FcmV1Client.fromServiceAccountJsonPath(saPath);
				res.put("v1_ready", true);
			} catch (Throwable t) {
				res.put("v1_ready", false);
				res.put("v1_error", String.valueOf(t));
			}

			// legacy payload (multicast)
			Notification legacyNotif = new Notification();
			legacyNotif.setTitle("GT-FR Test");
			legacyNotif.setMessageBody("Test push from server");
			legacyNotif.addDataAttribute("name", "GT-FR Test");
			legacyNotif.addDataAttribute("time", String.valueOf(System.currentTimeMillis()));
			legacyNotif.addDataAttribute("group", "test");
			legacyNotif.addDataAttribute("image", "");

			while (rs.next()) {
				tokenCount++;
				String token = rs.getString("token");
				if (token == null) token = "";
				token = token.trim();
				if (token.isEmpty()) continue;

				JSONObject one = new JSONObject();
				if (v1 != null) {
					try {
						Map<String, String> data = new HashMap<>();
						data.put("name", "GT-FR Test");
						data.put("time", String.valueOf(System.currentTimeMillis()));
						data.put("group", "test");
						data.put("image", "");
						FcmV1Client.SendResult r = v1.sendToToken(token, "GT-FR Test", "Test push from server", data);
						one.put("v1_http", r.httpCode);
						if (r.httpCode >= 200 && r.httpCode < 300) one.put("v1_ok", true);
						else one.put("v1_err", r.errorBody);
					} catch (Throwable t) {
						one.put("v1_ok", false);
						one.put("v1_err", String.valueOf(t));
					}
				}

				legacyNotif.addDeviceToSend(token);
				perToken.put(token, one);
			}

			res.put("token_count", tokenCount);
			res.put("per_token", perToken);

			try {
				// legacy FCM (may be disabled)
				var fr = new PushNotifHelper().sendNotificationToDevice(legacyNotif);
				res.put("legacy_http", fr.getFCMResponseCode());
				res.put("legacy_ok", fr.getSuccessMessage());
				res.put("legacy_err", fr.getErrorMessage());
			} catch (Throwable t) {
				res.put("legacy_err", String.valueOf(t));
			}

			out.print(res.toString());
		} catch (Throwable t) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			out.print(new JSONObject().put("error", String.valueOf(t)).toString());
		}
	}
}


