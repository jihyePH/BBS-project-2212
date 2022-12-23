package board;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.apache.catalina.tribes.util.Arrays;

import db.BoardDao;
import db.ReplyDao;
import misc.JSONUtil;

/**
 * Servlet implementation class BoardController
 */
@WebServlet({ "/board/list", "/board/write", "/board/update",
			  "/board/detail", "/board/delete", "/board/deleteConfirm", 
			  "/board/reply"})

public class BoardController extends HttpServlet {

	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String[] uri = request.getRequestURI().split("/");
		String action = uri[uri.length - 1];
		BoardDao dao = new BoardDao();
		ReplyDao replyDao = new ReplyDao();
		HttpSession session = request.getSession();
		String sessionUid = (String) session.getAttribute("uid");
		session.setAttribute("menu", "board");
		
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html; charset=utf-8");
		String title = null, content = null, files = null , uid = null, today = null;
		String jsonFiles = null;
		int bid = 0, totalBoardNo=0, totalPages=0, page =0;
		Board board = null;
		List<Board> list = null;
		List<String> pageList = null;
		RequestDispatcher rd = null;
		
		switch(action) {
		case "list":
			String page_ = request.getParameter("p");
			String field = request.getParameter("f");
			String query = request.getParameter("q");
 
			page = (page_ == null || page_.equals("")) ? 1 : Integer.parseInt(page_);
			field = (field == null || field.equals("")) ? "title" : field;
			query = (query == null || query.equals("")) ? "" : query;
			list = dao.listBoard(field, query, page);

			session.setAttribute("currentBoardPage", page);
			request.setAttribute("field", field);
			request.setAttribute("query", query);
			
			totalBoardNo = dao.getBoardCount("bid", "");
			totalPages = (int) Math.ceil(totalBoardNo / 10.);
			 
			int startPage = (int)(Math.ceil((page-0.5)/10) -1) * 10 + 1;
			int endPage = Math.min(totalPages, startPage + 9);
			pageList = new ArrayList<>();
			for (int i = startPage; i <= endPage; i++)
				pageList.add(String.valueOf(i));
			request.setAttribute("pageList", pageList);
			request.setAttribute("startPage", startPage);
			request.setAttribute("endPage", endPage);
			request.setAttribute("totalPages", totalPages);
			
			today = LocalDate.now().toString();		// 2022-12-20
			request.setAttribute("today", today);
			request.setAttribute("boardList", list);
			rd = request.getRequestDispatcher("/WEB-INF/view/board/list.jsp");
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
			jsonFiles = board.getFiles();
			if (!(jsonFiles == null || jsonFiles.equals(""))) {
				JSONUtil json = new JSONUtil();
				List<String> fileList = json.parse(jsonFiles);
				request.setAttribute("fileList", fileList);
			}
			request.setAttribute("board", board);
			List<Reply> replyList = replyDao.getReplies(bid);
			request.setAttribute("replyList", replyList);
			
			rd = request.getRequestDispatcher("/WEB-INF/view/board/detail.jsp");
			rd.forward(request, response);
			break;
			
		case "write":	
			if (request.getMethod().equals("GET")) {
				rd = request.getRequestDispatcher("/WEB-INF/view/board/write2.jsp");
				rd.forward(request, response);
				//	response.sendRedirect("/bbs2/board/write.jsp");
			} else {
				/** board/fileupload로부터 전달된 데이터 읽음 				 */
				title = (String) request.getAttribute("title");
				content = (String) request.getAttribute("content");
				files = (String) request.getAttribute("files");
				//System.out.println("title="+title+", files="+files);

				board = new Board(sessionUid, title, content, files);
				dao.insertBoard(board);
				response.sendRedirect("/bbs2/board/list?p=1&f=&q=");
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
			rd =  request.getRequestDispatcher("/WEB-INF/view/board/delete.jsp?bid="+ bid);
			rd.forward(request, response);
			break;
		
		case "deleteConfirm":
			bid = Integer.parseInt(request.getParameter("bid"));
			dao.deleteBoard(bid);
			response.sendRedirect("/bbs2/board/list?p=" + session.getAttribute("currentBoardPage") + "&f=&q=");
			break;
			
		case "update":
			if (request.getMethod().equals("GET")) {
					bid = Integer.parseInt(request.getParameter("bid"));
					board = dao.getBoardDetail(bid);
				
				jsonFiles = board.getFiles();
				if (!(jsonFiles == null || jsonFiles.equals(""))) {
					JSONUtil json = new JSONUtil();
					List<String> fileList = json.parse(jsonFiles);
					session.setAttribute("fileList", fileList);
				}
				
				request.setAttribute("board", board);
				rd = request.getRequestDispatcher("/WEB-INF/view/board/update2.jsp");	// Editor version
				// rd = request.getRequestDispatcher("/board/update.jsp");
				rd.forward(request, response);
			} else {
				String bid_ = (String) request.getAttribute("bid");
				bid = Integer.parseInt(bid_);
				uid = (String) request.getAttribute("uid");
				title = (String) request.getAttribute("title");
				System.out.println(title);
				content = (String) request.getAttribute("content");
				
				List<String> listAdditionalFiles = (List<String>) session.getAttribute("fileList");
				
				String delName = (String) request.getAttribute("delFile");
				
				if (!(delName == null || delName.equals(""))) {
					File delFile = new File("c:/Temp/upload/" + delName);
					delFile.delete();
					listAdditionalFiles.remove(delName);
				}
				JSONUtil json = new JSONUtil();
				files = (String) request.getAttribute("files");		// FileUpload에서 넘어온 것
				List<String> tmpList = json.parse(files);
				for (String tmp: tmpList)
					listAdditionalFiles.add(tmp);
				files = json.stringify(listAdditionalFiles);
				
				board = new Board(bid, title, content, files);
				dao.updateBoard(board);
				response.sendRedirect("/bbs2/board/detail?bid=" + bid + "&uid=" + uid + "&option=DNI");
			}
			break;

		default:
			System.out.println(request.getMethod() + " 잘못된 경로");
		}
	}

}