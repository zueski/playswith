// Import required java libraries
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

// Extend HttpServlet class
public class UserServlet extends HttpServlet 
{

	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		String[] path = request.getRequestURI().split("/");
		if(path == null || path.length < 3)
		{	response.sendRedirect("/"); return; }
		String platform = path[2];
		String user = path[4];
		
		if("true".equals(request.getParameter("doAuth")))
		{	response.sendRedirect(getRedirectForAuth(response, user, platform)); return; }
		
		response.setContentType("text/html");
		
		PrintWriter out = response.getWriter();
		out.write("<html><head>");
		out.write("<title>Playswith " + user + "</title>\n");
		out.write("<script src=\"/static/authorize.js\"></script>\n");
		out.write("<script src=\"/static/user.js\"></script>\n");
		out.write("</head>\n<body onload=\"bootstrapUser()\">\n");
		// header
		out.write("<table><tr>");
		out.write("<td><div style='height: 96px; width: 96px;'><span style='height: 96px; width: 96px;' id='user'></span></div></td>");
		out.write("<td><select id='chars'></select></td>");
		out.write("</tr></table>");
		out.write("</body>");
		out.write("</html>");
	}
	
	private String getRedirectForAuth(HttpServletResponse response, String user, String platform)
		throws ServletException, IOException
	{
		String state = java.util.Base64.getEncoder().encodeToString(("{\"user\":\""+user+"\",\"platform\":\""+platform+"\"}").getBytes(java.nio.charset.Charset.forName("UTF8")));
		return DestinyAPI.authURL+"?state="+state;
	}
	
	public void destroy()
	{
		// do nothing.
	}
}