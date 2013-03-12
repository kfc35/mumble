<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>kfc35 - ss2249 - hhc39 - CS5300 - Proj 1b</title>
</head>
<body>
<h1><%=getServletContext().getAttribute("message")%></h1>
<!-- The BasicSessionServlet is mapped to "/", so that's why action is "" -->
<form method="post" action=""><input type="submit" name="Replace" value="Replace"/>
	<input type="text" name="newMessage" size="40" maxLength="100" /></form>
	<!-- TODO look into maxLength for the message -->
<form method="get" action=""><input type="submit" name="Refresh" value="Refresh"/></form>
<form method="post" action=""><input type="submit" name="Log out" value="Log out"/></form>
<form method="post" action=""><input type="submit" name="Crash" value="Crash"/></form>
<p>This server's IPP port: <%=getServletContext().getAttribute("myIPP")%></p>
<p>Session Data found from: <%=getServletContext().getAttribute("sessionOrigin")%></p>
<p>IPP Primary~Secondary of this session data: <%=getServletContext().getAttribute("locations")%></p>
<p>Expires <%=getServletContext().getAttribute("expires")%></p>
<p>Discard Time: <%=getServletContext().getAttribute("discardTime")%></p>
<p>Member Set IPPs: </p>
</body>
</html>