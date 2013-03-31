package sessions;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map.Entry;
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
	private ConcurrentHashMap<String, CS5300PROJ1Session> sessionDataTable = 
			new ConcurrentHashMap<String, CS5300PROJ1Session>();
	private ConcurrentHashMap<String, Integer> memberSet = 
			new ConcurrentHashMap<String, Integer>();
	//The value is the latest callID received by this member

	/**Time Variables for timeouts**/
	public static final long DELTA = 1000 * 3; //3 secs
	public static final long SESSION_TIMEOUT_SECS = 1000 * 60 * 30; //120 secs = 2 mins
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
			response.setContentType("text/html");
			response.getWriter().print("Voldemort has risen from the dead and has crucio'd our " +
					"server. Please excuse us while we try to exorcise him.");
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
			response.setContentType("text/html");
			response.getWriter().print("100 Emus have landed from outer space and incapacitated our server. " +
					"Please excuse us while we try to shoot all of them.");
			return;
		}
		else if (request.getParameter("Crash") != null) {
			CRASH = true;
			response.setContentType("text/html");
			response.getWriter().print(" I'm afraid. I'm afraid, Dave. Dave, my mind is going. " +
					"I can feel it. I can feel it. My mind is going. " +
					"There is no question about it. I can feel it. " +
					"I can feel it. I can feel it. I'm a... fraid. " +
					"Good afternoon, gentlemen. I am a HAL 9000 computer. " +
					"I became operational at the H.A.L. plant in Urbana, " +
					"Illinois on the 12th of January 1992. My instructor was " +
					"Mr. Langley, and he taught me to sing a song. If you'd " +
					"like to hear it I can sing it for you. ");
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
			
			//Remove from the primary and backup if applicable
			CS5300PROJ2RPCClient client;
			
			if (!session.getCookie().getPrimaryIPP().equals(myIPP)) {
				client = new CS5300PROJ2RPCClient(callID++, session.getCookie(), true, rpcServerObj.getLocalPort());
				if (!client.delete()) {
					synchronized(memberSet) {
						memberSet.remove(client.getIppDest().toString());
					}
				}
			}
			
			if (session.getCookie().getBackupIPP() != null &&
					!session.getCookie().getBackupIPP().equals(myIPP)) {
				client = new CS5300PROJ2RPCClient(callID++, session.getCookie(), false, rpcServerObj.getLocalPort());
				if (!client.delete()) {
					synchronized (memberSet) {
						memberSet.remove(client.getIppDest().toString());
					}
				}
			}

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
					for (Entry<String, Integer> entry: memberSet.entrySet()) {
						CS5300PROJ2IPP ipp = new CS5300PROJ2IPP(entry.getKey());
						session.setBackupIPP(ipp);
						CS5300PROJ2RPCClient client = new CS5300PROJ2RPCClient(callID++, session.getCookie(), false, rpcServerObj.getLocalPort());
						session.setEnd((new Date()).getTime() + DISCARD_TIME_FROM_CURRENT);
						if (client.write(session, session.getEnd())) {
							sessionDataTable.put(session.getSessionID().toString(), session);
							return session;
						}
						memberSet.remove(ipp.toString());
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
						session = sessionDataTable.get(cookieCrisp.getSessionID().toString());
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
						boolean firstResponded = client.getResponded();
						boolean secondResponded = false;
						if (client.getResponded()) { // If there's a response from the first
							synchronized (memberSet) {
								memberSet.put(client.getIppDest().toString(), client.getCallID());
								if (cookieCrisp.hasBackupIPP()) {
									memberSet.put(cookieCrisp.getBackupIPP().toString(), -1);
								}
							}
						} 
						
						// If the first one didn't return anything but there is a backupIPP
						if (session == null && cookieCrisp.hasBackupIPP()) { // there's a backup to send to
							client = new CS5300PROJ2RPCClient(callID++, cookieCrisp, false, rpcServerObj.getLocalPort());
							session = client.read();
							secondResponded = client.getResponded();
							if (client.getResponded()) {
								synchronized (memberSet) {
									memberSet.put(client.getIppDest().toString(), client.getCallID());
								}
							}
						} 
						
						if (session == null) { //Create a new session
							session = new CS5300PROJ1Session();
							if (!firstResponded && !secondResponded) {
								session.setMessage("No server knows of you, so hey new user!");
							} else if (!firstResponded && !secondResponded) {
								session.setMessage("Both server communications failed, so hey new user!");
							} else if (firstResponded && !secondResponded) {
								session.setMessage("Primary forget and second didn't respond, so hey new user!");
							} else {
								session.setMessage("Primary didn't respond and second failed, so hey new user!");
							}
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
				for (Entry<String, Integer> entry: memberSet.entrySet()) {
					CS5300PROJ2IPP ipp = new CS5300PROJ2IPP(entry.getKey());
					session.setBackupIPP(ipp);
					CS5300PROJ2RPCClient client = new CS5300PROJ2RPCClient(callID++, session.getCookie(), false, rpcServerObj.getLocalPort());
					session.setEnd((new Date()).getTime() + DISCARD_TIME_FROM_CURRENT);
					if (client.write(session, session.getEnd())) {
						sessionDataTable.put(session.getSessionID().toString(), session);
						return session;
					}
					memberSet.remove(ipp.toString());
				}
			}
		}
		
		if (DEBUG) {
			System.out.println("Created a New Session: " + session.toString());
		}
		sessionDataTable.put(session.getSessionID().toString(), session); 
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

			String members = "";
			synchronized(memberSet) {
				
				members = memberSet.keySet().toString();
			}
			getServletContext().setAttribute("members", members);
			
			RequestDispatcher rd = request.getRequestDispatcher("/CS5300PROJ1index.jsp");
			rd.forward(request, response);
		}
	}

}
