package board;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import db.BoardDao;
import db.ReplyDao;

/**
 * Servlet implementation class BoardController
 */
@WebServlet({ "/board/list", "/board/search", "/board/write", "/board/update",
			  "/board/detail", "/board/delete", "/board/deleteConfirm", 
			  "/board/reply" })
public class BoardController extends HttpServlet {

	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		String[] uri = request.getRequestURI().split("/");
		String action = uri[uri.length - 1];
		BoardDao dao = new BoardDao();
		ReplyDao replyDao = new ReplyDao();
		HttpSession session = request.getSession();
		String sessionUid = (String) session.getAttribute("uid");
		session.setAttribute("menu", "board");
		
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html; charset=utf-8");
		String title = null, content = null, files = null , uid = null;
		int bid = 0;
		Board board = null;
		RequestDispatcher rd = null;
		
		switch(action) {
		case "list":
			int page = Integer.parseInt(request.getParameter("page"));
			List<Board> list = dao.listUsers("title", "", page);
			
			session.setAttribute("currentBoardPage", page);
			int totalBoardNo = dao.getBoardCount();
			int totalPages = (int) Math.ceil(totalBoardNo / 10.);
			List<String> pageList = new ArrayList<>();
			for (int i = 1; i <= totalPages; i++)
				pageList.add(String.valueOf(i));
			request.setAttribute("pageList", pageList);
			
			String today = LocalDate.now().toString();		// 2022-12-20
			request.setAttribute("today", today);
			request.setAttribute("boardList", list);
			rd = request.getRequestDispatcher("/board/list.jsp");
			rd.forward(request, response);
			break;
			
		case "detail":
			bid = Integer.parseInt(request.getParameter("bid"));
			uid = request.getParameter("uid");
			String option = request.getParameter("option");
			// 조회수 증가, 단 본인이 읽거나 댓글 작성후에는 제외
			if (option == null  && (!uid.equals(sessionUid))) {
				dao.increaseViewCount(bid);
			}
			board = dao.getBoardDetail(bid);
			request.setAttribute("board", board);
			List<Reply> replyList = replyDao.getReplies(bid);
			request.setAttribute("replyList", replyList);
			
			rd = request.getRequestDispatcher("/board/detail.jsp");
			rd.forward(request, response);
			break;
			
		case "write":	
			if (request.getMethod().equals("GET")) {
				response.sendRedirect("/bbs2/board/write.jsp");
			} else {
				title = request.getParameter("title");
				content = request.getParameter("content");
				files = request.getParameter("files");
				
				board = new Board(sessionUid, title, content, files);
				dao.insert(board);
				response.sendRedirect("/bbs2/board/list?page=1");
			}
			break;
			
		case "reply":
			content = request.getParameter("content");
			bid = Integer.parseInt(request.getParameter("bid"));
			uid = request.getParameter("uid"); 				// 게시글 작성자 uid
			// 게시글 uid와 댓글쓰려는 사람의 uid 같으면 isMIne=1
			int isMine = (uid.equals(sessionUid)) ? 1 : 0; 	
			Reply reply = new Reply(content, isMine, sessionUid, bid);
			replyDao.insert(reply);
			dao.increasReplyCount(bid);
			response.sendRedirect("/bbs2/board/detail?bid="+ bid + "&uid=" + uid + "&option=DNI");
			break;
			
		case "delete":
			bid = Integer.parseInt(request.getParameter("bid"));
			response.sendRedirect("/bbs2/board/delete.jsp?bid="+ bid);
			break;
		
		case "deleteConfirm":
			bid = Integer.parseInt(request.getParameter("bid"));
			dao.deleteBoard(bid);
			response.sendRedirect("/bbs2/board/list?page=" + session.getAttribute("currentBoardPage"));
			break;
			
		case "update":
			if (request.getMethod().equals("GET")) {
				bid = Integer.parseInt(request.getParameter("bid"));
				board = dao.getBoardDetail(bid);
				request.setAttribute("board", board);
				rd = request.getRequestDispatcher("/board/update.jsp");
				rd.forward(request, response);
			} else {
				bid = Integer.parseInt(request.getParameter("bid"));
				uid = request.getParameter("uid");
				title = request.getParameter("title");
				content = request.getParameter("content");
				files = request.getParameter("files");
				
				board = new Board(bid, title, content, files);
				dao.updateBoard(board);
				response.sendRedirect("/bbs2/board/update?bid="+ bid + "&uid=" + uid + "&option=DNI");
			}
			break;
			
		default:
			System.out.println(request.getMethod() + " 잘못된 경로");
		}
	}
	
}