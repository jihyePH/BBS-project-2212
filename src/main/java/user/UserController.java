package user;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Servlet implementation class UserServiceController
 */
@WebServlet({ "/user/list", "/user/login", "/user/logout",
			  "/user/register", "/user/update", "/user/delete","/user/deleteConfirm" })
public class UserController extends HttpServlet {
	
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String[] uri = request.getRequestURI().split("/");
		String action = uri[uri.length - 1];
		UserDao dao = new UserDao();
		HttpSession session = request.getSession();
		session.setAttribute("menu", "user");
		
		response.setContentType("text/html; charset=utf-8");
		String uid = null, pwd = null, pwd2 = null, uname = null, email = null;
		User u = null;
		RequestDispatcher rd = null;
		switch(action) {
		case "list":
			int page = Integer.parseInt(request.getParameter("page"));
			List<User> list = dao.listUsers(page);
			
			session.setAttribute("currentUserPage", page);
			int totalUsers = dao.getUserCount();
			int totalPages = (int) Math.ceil(totalUsers / 10.);
			List<String> pageList = new ArrayList<>();
			for (int i =1; i <= totalPages; i++)
				pageList.add(String.valueOf(i));
			request.setAttribute("pageList", pageList);
			
			request.setAttribute("userList", list);
			rd = request.getRequestDispatcher("/WEB-INF/view/user/list.jsp");
			rd.forward(request, response);
			break;
		case "login":
			if (request.getMethod().equals("GET")) {
				rd = request.getRequestDispatcher("/WEB-INF/view/user/login.jsp");
				rd.forward(request, response);
			} else {
				uid = request.getParameter("uid");
				pwd = request.getParameter("pwd");
				u = dao.getUserInfo(uid);
				if (u.getUid() != null) {		// uid ??? ??????
					if (BCrypt.checkpw(pwd, u.getPwd())) {
						// System.out.println(u.getUid() + ", " + u.getUname());
						session.setAttribute("uid", u.getUid());
						session.setAttribute("uname", u.getUname());
						
						// Welcome message
						request.setAttribute("msg", u.getUname() + "??? ???????????????.");
						request.setAttribute("url", "/bbs2/board/list?p=1&f=&q=");
						rd = request.getRequestDispatcher("/WEB-INF/view/user/alertMsg.jsp");
						rd.forward(request, response);
					} else {
						// ??? ????????? ?????????
						request.setAttribute("msg", "????????? ???????????? ?????????. ?????? ???????????????.");
						request.setAttribute("url", "/bbs2/user/login");
						rd = request.getRequestDispatcher("/WEB-INF/view/user/alertMsg.jsp");
						rd.forward(request, response);
					}
					} else {				// uid ??? ??????
						// ?????? ?????? ???????????? ??????
						request.setAttribute("msg", "?????? ?????? ???????????? ???????????????.");
						request.setAttribute("url", "/bbs2/user/register");
						rd = request.getRequestDispatcher("/WEB-INF/view/user/alertMsg.jsp");
						rd.forward(request, response);
					}
				}
			break;
		case "logout":
			session.invalidate();
			response.sendRedirect("/bbs2/user/login");
			break;
		case "register":
			if (request.getMethod().equals("GET")) {
				rd = request.getRequestDispatcher("/WEB-INF/view/user/register.jsp");
				rd.forward(request, response);
			} else {
				uid = request.getParameter("uid").strip();
				pwd = request.getParameter("pwd").strip();
				pwd2 = request.getParameter("pwd2").strip();
				uname = request.getParameter("uname").strip();
				email = request.getParameter("email").strip();
				if (pwd.equals(pwd2)) {
					u = new User(uid, pwd, uname, email);
					dao.registerUser(u);
					response.sendRedirect("/bbs2/user/login");
				} else {
					request.setAttribute("msg", "???????????? ????????? ?????????????????????.");
					request.setAttribute("url", "/bbs2/user/register");
					rd = request.getRequestDispatcher("/WEB-INF/view/user/alertMsg.jsp");
					rd.forward(request, response);
				}
			}
			break;
			
		case "update":
			if (request.getMethod().equals("GET")) {
				uid = request.getParameter("uid");
				u = dao.getUserInfo(uid);
				request.setAttribute("user", u);
				rd = request.getRequestDispatcher("/WEB-INF/view/user/update.jsp");
				rd.forward(request, response);
			} else {								// POST
				uid = request.getParameter("uid");
				pwd = request.getParameter("pwd").strip();
				pwd2 = request.getParameter("pwd2").strip();
				uname = request.getParameter("uname").strip();
				email = request.getParameter("email").strip();
				
				if(pwd == null || pwd.equals("")) { 	//pwd??? ???????????? ?????? ??????
					u = new User(uid, uname, email);
					dao.updateUser(u);
					session.setAttribute("uname", uname);
					response.sendRedirect("/bbs2/user/list?page="+ session.getAttribute("currentUserPage"));
				} else if (pwd.equals(pwd2)) { 			// pwd??? ????????? ??????
					u = new User(uid,pwd,uname,email);
					dao.updateUserWithPassword(u);
					session.setAttribute("uname", uname);
					response.sendRedirect("/bbs2/user/list?page="+ session.getAttribute("currentUserPage"));
				} else {									// pwd ??????????????? ??????
					request.setAttribute("msg", "???????????? ????????? ?????????????????????.");
					request.setAttribute("url", "/bbs2/user/update?uid=" + uid);
					rd = request.getRequestDispatcher("/WEB-INF/view/user/alertMsg.jsp");
					rd.forward(request, response);
				}
			}
			break;
			
		case "delete":
			uid = request.getParameter("uid");
			rd = request.getRequestDispatcher("/WEB-INF/view/user/delete.jsp?uid=" + uid);
			rd.forward(request, response);
			break;
		case "deleteConfirm":
			uid = request.getParameter("uid");
			dao.deleteUser(uid);
			response.sendRedirect("/bbs2/user/list?page="+ session.getAttribute("currentUserPage"));
			break;
		default:
			System.out.println(request.getMethod() + " ????????? ??????");
		}
	}

}