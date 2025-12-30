package ucf.fdrserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.json.JSONObject;

import java.sql.Connection;
import ucf.fdrssutil.MySQLConfig;
import ucf.fdrssutil.globalUtil;

/**
 * Servlet implementation class Search
 */
@WebServlet("/GroupSearch")
public class GroupSearch extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection  con;
	Format formatter = new SimpleDateFormat("yyyy-MM-dd");
	String root_path="";
	String personimgdir = "";
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GroupSearch() {
        super();       
    }
    public void init()
    {
    	personimgdir = root_path+"personimgs/";
    	root_path = getServletContext().getRealPath("/");
    	MySQLConfig.userinfo_path=root_path+"user_config.txt";
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		String userid = (String) session.getAttribute("userid");
		String is_mobile_str = (String) request.getParameter("mobile");
		int is_mobile = 0;
		if (is_mobile_str!=null){
			is_mobile = Integer.parseInt(is_mobile_str);	
		}
		if (userid==null) {
			userid = (String) request.getParameter("userid");// due to mobile
		}
		if (userid==null) {
			request.setAttribute("Error!", "Session losted!!!");
			RequestDispatcher mDispatcher = request.getRequestDispatcher("/index.jsp");
			mDispatcher.forward(request, response);
			return;
		}
		
		String sql = "";
		String group_name= (String) request.getParameter("group_name");
		if(group_name!=null)
			sql+="name like '%"+group_name+"%'";
		
		String adminid = (String) request.getParameter("adminid");
		if (adminid != null)
		{
			if(sql!="") sql+=" AND adminid='"+adminid+"'";
			else sql="adminid='"+adminid+"'";
		}

		PrintWriter out = response.getWriter();
		if(sql!=""){
			sql = "select * from group_info where "+sql;
			
		}
		else
			sql = "select * from group_info where 1";
		
		int id;
    	con = MySQLConfig.getConnection();
    	
    	globalUtil.delFolderContent(personimgdir);
		String json_str="{searchlists : [";
		try {
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				int i=0;
				
				while (rs.next()==true){
					id=rs.getInt("id");
					group_name = rs.getString("name");
					adminid = rs.getString("adminid");
					
					if (i>0) json_str+=",";
					json_str+="{";
					json_str+="group_name : '"+group_name+"',";
					json_str+="adminid : '"+adminid+"',";
					json_str+="id : "+id+",";
					json_str+="}";
					i++;
				}
				json_str+="]}";	
				if (i==0){
					json_str="{searchlists : none}";
				}
				JSONObject obj = new JSONObject(json_str);
				out.print(obj);
				LogIn.m_logger.info("GroupSearch.java" + json_str);
			} catch (SQLException e) {
				json_str="{searchlists : none}";
				JSONObject obj = new JSONObject(json_str);
				out.print(obj);
				e.printStackTrace();
			} catch (org.json.JSONException e){
				LogIn.m_logger.debug(json_str);
				e.printStackTrace();	
			}
		
	}
}
