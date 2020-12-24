package com.crepass.restfulapi.v2.domain;

import lombok.Data;

@Data
public class InvestReplyVO {
	
	private int mno;			// 댓글번호
	private int loanId;			// 대출상품번호
	private String name;		// 투자자이름
	private String mid;			// 투자자아이디
	private String comment;		// 댓글내용
	private String regdate;		// 작성일
}
