// Import required java libraries
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

// Extend HttpServlet class
public class LoginServlet extends HttpServlet 
{

	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		String authCode = request.getParameter("code");
		String authState = request.getParameter("state");
		// Set response content type
		response.setContentType("text/html");

		// Actual logic goes here.
		PrintWriter out = response.getWriter();
		if(authCode != null && authCode.length() > 1)
		{	// try to login 
			out.println("<html><head><title>Authorizing . . .</title>\n");
			out.println("<script src='/static/authorize.js'></script>\n");
			out.println("<script>\n");
			out.println("\tvar authCode = '" + authCode + "';\n");
			out.println("\tvar authState = '" + authState + "';\n");
			out.println("</script>\n");
			out.println("</head><body onload='authorize(authCode,authState)'></body></html>");
			
		} else {
			out.println("<html>\n<head><title>Enter your login</title>\n");
			out.println("<script src='/static/authorize.js'></script>\n");
			out.println("</head>\n");
			out.println("<body onload='bypasslogin()'>\n");
			out.println("<form method=\"POST\">\n");
			out.println("Platform: <select name=\"platform\">\n");
			out.println("<option value=\"2\">PSN</option>\n");
			out.println("<option value=\"1\">XBox</option>\n");
			out.println("<select><br>\n");
			out.println("Gamertag/ID: <input type=\"text\" name=\"userid\"><br>\n");
			out.println("<input type=\"submit\" value=\" go \" >\n");
			out.println("</body>\n");
			out.println("</html>\n");
		}
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		String userid = request.getParameter("userid");
		if(userid != null)
		{	userid = userid.trim(); }
		String platform = request.getParameter("platform");
		
		if(userid == null || userid.length() < 1 || platform == null || platform.length() < 1)
		{
			response.sendRedirect("/");
		} else {
			response.sendRedirect("platform/" + platform + "/user/" + userid + "/");
		}
	}

	public void destroy()
	{
		// do nothing.
	}
}