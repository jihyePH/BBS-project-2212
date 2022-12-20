package board;

import java.time.LocalDateTime;

public class Board {
	private int bid;
	private String uid;
	private String title;
	private String content;
	private LocalDateTime modTime;
	private int viewCount;
	private int replyCount;
	private int isDeleted;
	private String files;
	private String uname;
	
	Board(){}
	// 게시글 작성시 사용
	Board(int bid, String uid, String title, String content, String files) {
		this.uid = uid;
		this.title = title;
		this.content = content;
		this.files = files;
	}
	 Board(int bid, String uid, String title, String content, LocalDateTime modTime, int viewCount,
			int replyCount, int isDeleted, String files, String uname) {
		this.bid = bid;
		this.uid = uid;
		this.title = title;
		this.content = content;
		this.modTime = modTime;
		this.viewCount = viewCount;
		this.replyCount = replyCount;
		this.isDeleted = isDeleted;
		this.files = files;
		this.uname = uname;
	}
	public int getBid() {
		return bid;
	}
	public void setBid(int bid) {
		this.bid = bid;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public LocalDateTime getModTime() {
		return modTime;
	}
	public void setModTime(LocalDateTime modTime) {
		this.modTime = modTime;
	}
	public int getViewCount() {
		return viewCount;
	}
	public void setViewCount(int viewCount) {
		this.viewCount = viewCount;
	}
	public int getReplyCount() {
		return replyCount;
	}
	public void setReplyCount(int replyCount) {
		this.replyCount = replyCount;
	}
	public int getIsDeleted() {
		return isDeleted;
	}
	public void setIsDeleted(int isDeleted) {
		this.isDeleted = isDeleted;
	}
	public String getFiles() {
		return files;
	}
	public void setFiles(String files) {
		this.files = files;
	}
	public String getUname() {
		return uname;
	}
	public void setUname(String uname) {
		this.uname = uname;
	}
}