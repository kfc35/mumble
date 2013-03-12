package sessions;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import rpc.CS5300PROJ2RPCServer;

/**
 * Servlet implementation class BasicSessionServlet
 */
public class CS5300PROJ1Servlet extends HttpServlet {

	//ENUM is used to determine the actions of each request
	private enum REQUEST{REFRESH, REPLACE, LOGOUT}

	public static boolean DEBUG = false;
	public static boolean CRASH = false;
	private static final long serialVersionUID = 1L;
	public static final String DEFAULT_MESSAGE = "Hello, User!";
	private ConcurrentHashMap<CS5300PROJ2SessionId, CS5300PROJ1Session> sessionDataTable = 
			new ConcurrentHashMap<CS5300PROJ2SessionId, CS5300PROJ1Session>();
	private ConcurrentHashMap<CS5300PROJ2IPP, Integer> memberSet = 
			new ConcurrentHashMap<CS5300PROJ2IPP, Integer>();

	public static final long EXPIRY_TIME_FROM_CURRENT = 1000 * 120; //2 minutes 
	private Thread terminator = new Thread(new CS5300PROJ1Terminator(sessionDataTable));
	private CS5300PROJ2RPCServer rpcServerObj = new CS5300PROJ2RPCServer(sessionDataTable, memberSet);
	private Thread rpcServer = new Thread(rpcServerObj);
	private double callID;
	private CS5300PROJ2IPP myIPP;
	

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public CS5300PROJ1Servlet() {
		super();
		callID = Double.parseDouble(rpcServerObj.getPort()) * 10000;
		terminator.start();
		rpcServer.start();
		myIPP = new CS5300PROJ2IPP(rpcServerObj.getAddress(), rpcServerObj.getPort());
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 * 
	 * Only an original GET request or a Refresh will call this
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		CS5300PROJ1Session session = null;
		/*Process session information if applicable*/

		synchronized (sessionDataTable) {
			session = execute(request.getCookies(), REQUEST.REFRESH, null);
			if (null == session) { //we have to create the session
				session = createSession();
			}
		}

		populateJSP(REQUEST.REFRESH, request, response, session); 
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 * Only a logout and replace
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		CS5300PROJ1Session session = null;
		Enumeration<String> params = request.getParameterNames();
		String message = DEFAULT_MESSAGE;
		REQUEST type = REQUEST.LOGOUT;

		// If this request is a REPLACE request.
		while(params.hasMoreElements()) {
			String param = (String) params.nextElement();
			if (param.equals("newMessage")) {
				message = request.getParameter("newMessage");
				type = REQUEST.REPLACE;
			}
		}
		synchronized (sessionDataTable) {
			session = execute(request.getCookies(), type, message);
			if (null == session && type == REQUEST.REPLACE) {

				// The case when expired cookies still come back
				session = createSession();
				session.setMessage(message);
			}
		}
		populateJSP(type, request, response, session); 
	}

	
	/**
	 * 
	 * @param cookies from the httprequest
	 * @param session that will be found
	 * @param type of the request GET will be REFRESH always, POST will either be REPLACE or LOGOUT 
	 * @param message that should replace the current message
	 * @return whether such a session exists or not
	 */
	private CS5300PROJ1Session execute(Cookie[] cookies, REQUEST type, String message) {
		CS5300PROJ1Session session = null;
		if (cookies != null) {
			for (Cookie c : cookies) {
				if (c.getName().equals(CS5300PROJ2Cookie.COOKIE_NAME)) {
					CS5300PROJ2Cookie cookieCrisp = (CS5300PROJ2Cookie) c;
					session = sessionDataTable.get(cookieCrisp.getSessionID());
					if (session == null) { 
						/*this can happen if you stop the servlet, clearing the
						  concurrent hashmap, and then run it again -> Eclipse still has
						  the cookie! Make a new cookie now!*/
						break;
					}
					
					// If logout, then just remove the session
					if (type == REQUEST.LOGOUT) {
						sessionDataTable.remove(cookieCrisp.getSessionID());
					} else {
						session.incrementVersion();
						session.setEnd((new Date()).getTime() + EXPIRY_TIME_FROM_CURRENT);
						if (type == REQUEST.REPLACE) {
							session.setMessage(message);
						}
					}

					if (DEBUG) {
						System.out.println(type + " & Updated: " + session.toString());
					}
				}
			}
		}

		return session;
	}
	

	/**
	 * 
	 * @param m - message
	 * Create a new session and add it to the session table
	 * @return session
	 */
	private CS5300PROJ1Session createSession() {
		UUID uuid = UUID.randomUUID();
		CS5300PROJ2SessionId sid = new CS5300PROJ2SessionId(uuid.toString(), myIPP);
		CS5300PROJ1Session session = new CS5300PROJ1Session(sid);
		if (DEBUG) {
			System.out.println("Created a New Session: " + session.toString());
		}
		sessionDataTable.put(sid, session); 
		return session;
	}
	

	/**
	 * 
	 * @param request
	 * @param response
	 * @param session
	 * @throws IOException
	 * @throws ServletException
	 * 
	 * A function that's brought out from the mess that was GET and POST
	 * 
	 */
	private void populateJSP(REQUEST type, HttpServletRequest request, 
			HttpServletResponse response, CS5300PROJ1Session session) 
			throws IOException, ServletException {
		CS5300PROJ2Cookie cookieToSend;
		if (type == REQUEST.LOGOUT) {
			
			// Creates a cookie to send to the client to erase all past cookies
			cookieToSend = new CS5300PROJ2Cookie();
			cookieToSend.setMaxAge(0);
			response.addCookie(cookieToSend);
			PrintWriter out = response.getWriter();
			out.println("Bye");
		} else {
			cookieToSend = session.getCookie();
			cookieToSend.setMaxAge((int) (EXPIRY_TIME_FROM_CURRENT / 1000));
			response.addCookie(cookieToSend);

			// Set all the JSP attributes
			getServletContext().setAttribute("message", session.getMessage());
			getServletContext().setAttribute("locations", session.getLocations());
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			//TODO this is the client cookie's expiration time.. (not the session's expiration time)
			getServletContext().setAttribute("expires", dateFormat.format(new Date()));
			getServletContext().setAttribute("myIPP", myIPP.getIP() + ":" + myIPP.getPort());
			CS5300PROJ2IPP originIPP = session.getCookie().getSessionID().getOriginIPP();
			getServletContext().setAttribute("sessionOrigin", originIPP.toString());
			CS5300PROJ2Location locations = session.getCookie().getIpp();
			getServletContext().setAttribute("sessionPrimary", locations.getPrimaryIPP().toString());
			getServletContext().setAttribute("sessionSecondary", locations.getBackupIPP().toString());

			getServletContext().setAttribute("discardTime", dateFormat.format(new Date(session.getEnd())));
			
			//TODO Make a list accessible in JSP of member set
			RequestDispatcher rd = request.getRequestDispatcher("/CS5300PROJ1index.jsp");
			rd.forward(request, response);
		}
	}

}
