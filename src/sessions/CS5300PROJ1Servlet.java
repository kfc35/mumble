package sessions;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import rpc.CS5300PROJ2RPCClient;
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
	//The value is the latest callID received by this member

	/**Time Variables for timeouts**/
	public static final long DELTA = 1000 * 3; //3 secs
	public static final long SESSION_TIMEOUT_SECS = 1000 * 120; //120 secs = 2 mins
	public static final long EXPIRY_TIME_FROM_CURRENT = SESSION_TIMEOUT_SECS + DELTA;
	public static final long GAMMA = 100; //0.1 secs
	public static final long DISCARD_TIME_FROM_CURRENT = SESSION_TIMEOUT_SECS + 2 * DELTA + GAMMA;
	
	private Thread terminator = new Thread(new CS5300PROJ1Terminator(sessionDataTable));
	private CS5300PROJ2RPCServer rpcServerObj = new CS5300PROJ2RPCServer(sessionDataTable, memberSet);
	private Thread rpcServer = new Thread(rpcServerObj);
	
	private int callID;
	private CS5300PROJ2IPP myIPP;
	private int numSessions = 0;


	/**
	 * @throws UnknownHostException 
	 * @see HttpServlet#HttpServlet()
	 */
	public CS5300PROJ1Servlet() throws UnknownHostException {
		super();
		callID = Integer.parseInt(rpcServerObj.getLocalPort()) * 10000;
		terminator.start();
		rpcServer.start();
		myIPP = new CS5300PROJ2IPP(rpcServerObj.getLocalAddress(), rpcServerObj.getLocalPort());
		if (DEBUG) {
			System.out.println(myIPP.toString());
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 * 
	 * Only an original GET request or a Refresh will call this
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		if (CRASH) {
			return;
		}
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
		if (CRASH) {
			return;
		}
		else if (request.getParameter("Crash") != null) {
			CRASH = true;
			return; 
		}
		CS5300PROJ1Session session = null;
		@SuppressWarnings("unchecked")
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
	 * @throws NumberFormatException 
	 * @throws IOException 
	 */
	private CS5300PROJ1Session execute(Cookie[] cookies, REQUEST type, String message) 
			throws NumberFormatException, IOException {

		CS5300PROJ1Session session = getSession(cookies);

		// If a new session
		if (session == null || session.getEnd() == -1) {
			return session;
		}

		// Do as the operations asked 
		// If logout, then just remove the session
		if (type == REQUEST.LOGOUT) {
			sessionDataTable.remove(session.getSessionID());
		} else {
			if (type == REQUEST.REPLACE) {
				session.setMessage(message);
			}

			session.incrementVersion();
			session.setPrimaryIPP(myIPP);
			synchronized (memberSet) { 
				if (memberSet.size() == 0) {
					session.setBackupIPP(null);
				} else {
					// Find a backup
					for (Object o : memberSet.keySet().toArray()) {
						CS5300PROJ2IPP ipp = (CS5300PROJ2IPP) o;
						session.setBackupIPP(ipp);
						CS5300PROJ2RPCClient client = new CS5300PROJ2RPCClient(callID++, session.getCookie(), false, rpcServerObj.getLocalPort());
						session.setEnd((new Date()).getTime() + DISCARD_TIME_FROM_CURRENT);
						if (client.write(session, session.getEnd())) {
							sessionDataTable.put(session.getSessionID(), session);
							return session;
						}
						memberSet.remove(ipp);
					}
				}
			}

			// No backups found
			session.setBackupIPP(null);
			session.setEnd((new Date()).getTime() + DISCARD_TIME_FROM_CURRENT);
		}
		return session;
	}

	private CS5300PROJ1Session getSession(Cookie[] cookies) 
			throws NumberFormatException, IOException {
		CS5300PROJ1Session session = null;

		if (cookies != null) {
			for (Cookie c : cookies) {
				if (c.getName().equals(CS5300PROJ2Cookie.COOKIE_NAME)) {
					CS5300PROJ2Cookie cookieCrisp = new CS5300PROJ2Cookie(c.getValue());

					// if IPP local is either Primary or backup
					if (cookieCrisp.equalsEitherLocation(myIPP)) {
						session = sessionDataTable.get(cookieCrisp.getSessionID());
						if (session == null) { 
							/*this can happen if you stop the servlet, clearing the
						  concurrent hashmap, and then run it again -> Eclipse still has
						  the cookie! Make a new cookie now!*/
							break;
						}
					} else {
						// Send a READ request to the primary, then the backup for session object
						CS5300PROJ2RPCClient client = new CS5300PROJ2RPCClient(callID++, cookieCrisp, true, rpcServerObj.getLocalPort());
						session = client.read();
						boolean found_in_first = true;

						// find it in backup
						if (session == null && cookieCrisp.hasBackupIPP()) {
							found_in_first = false;
							client = new CS5300PROJ2RPCClient(callID++, cookieCrisp, false, rpcServerObj.getLocalPort());
							session = client.read();
						}

						// If found
						if (session != null) {
							synchronized (memberSet) {
								memberSet.put(client.getIppDest(), client.getCallID());

								// Didn't receive confirmation from the second but 
								// assumes that second is fine 
								if (found_in_first) {
									memberSet.put(cookieCrisp.getBackupIPP(), -1);
								}
							}
						} else {
							session = new CS5300PROJ1Session();
						}
					}
					break;
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
	 * @throws NumberFormatException 
	 * @throws IOException 
	 */
	private CS5300PROJ1Session createSession() throws NumberFormatException, IOException {
		CS5300PROJ2SessionId sid = new CS5300PROJ2SessionId(numSessions++, myIPP);
		CS5300PROJ1Session session = new CS5300PROJ1Session(sid);

		synchronized (memberSet) {
			if (memberSet.size() == 0) {
				session.setBackupIPP(null);
			} else {
				// Find a backup
				for (Object o : memberSet.keySet().toArray()) {
					CS5300PROJ2IPP ipp = (CS5300PROJ2IPP) o;
					session.setBackupIPP(ipp);
					CS5300PROJ2RPCClient client = new CS5300PROJ2RPCClient(callID++, session.getCookie(), false, rpcServerObj.getLocalPort());
					session.setEnd((new Date()).getTime() + DISCARD_TIME_FROM_CURRENT);
					if (client.write(session, session.getEnd())) {
						sessionDataTable.put(session.getSessionID(), session);
						return session;
					}
					memberSet.remove(ipp);
				}
			}
		}
		
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

		// End is when the session state could not be gotten from primary and/or backup
		if (type == REQUEST.LOGOUT || session.getEnd() == -1) {

			// Creates a cookie to send to the client to erase all past cookies
			cookieToSend = new CS5300PROJ2Cookie();
			cookieToSend.setMaxAge(0);
			response.addCookie(cookieToSend);
			PrintWriter out = response.getWriter();
			if (session != null && session.getEnd() == -1) {
				out.println("Sorry but we cannot find the session that you were referring to.");
			} else {
				out.println("Bye");
			}
		} else {
			cookieToSend = session.getCookie();
			cookieToSend.setMaxAge((int) (EXPIRY_TIME_FROM_CURRENT / 1000));
			response.addCookie(cookieToSend);

			// Set all the JSP attributes
			getServletContext().setAttribute("message", session.getMessage());
			getServletContext().setAttribute("address", request.getRemoteAddr() + "_" + request.getRemotePort());
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			getServletContext().setAttribute("myIPP", myIPP.toString());
			CS5300PROJ2IPP originIPP = session.getCookie().getSessionID().getOriginIPP();
			getServletContext().setAttribute("sessionOrigin", originIPP.toString());
			CS5300PROJ2Location locations = session.getCookie().getLocation();
			getServletContext().setAttribute("locations", locations.toString());
			getServletContext().setAttribute("expires", dateFormat.format(new Date((new Date()).getTime() + (cookieToSend.getMaxAge() * 1000))));

			getServletContext().setAttribute("discardTime", dateFormat.format(new Date(session.getEnd())));

			Object[] members = null;
			synchronized(memberSet) {
				members = memberSet.keySet().toArray();
			}
			String[] membersString = new String[members.length];
			for (int i = 0; i < members.length; i++) {
				membersString[i] = members[i].toString();
			}
			getServletContext().setAttribute("members", membersString);
			
			RequestDispatcher rd = request.getRequestDispatcher("/CS5300PROJ1index.jsp");
			rd.forward(request, response);
		}
	}

}
