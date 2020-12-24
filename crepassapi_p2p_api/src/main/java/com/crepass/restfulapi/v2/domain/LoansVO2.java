package com.crepass.restfulapi.v2.domain;



import lombok.Data;

@Data
public class LoansVO2 {
	
	private int loanId;				// 대출상품ID
	
	private String loanSubject;		// 대출제목

	private String loanSubjectA;	// 채권번호(분기) 
	private String loanSubjectB;	// 채권제목(분기)
	
	private long loanMoney;			// 대출금액
	
	private String repayTerm;		// 상환기간
	
	private String repayWay;		// 상환방법
	
	private String businCorpName;	// 비즈니스 이름
	
	private String socialName;		// 소셜이름
	
	private String grade;			// 신용등급 => 201221 렌도등급
	
	private String scoreLenddo;		// 201221 렌도등급
	
	private String age;				// 연령대
	
	private String isLoanExec;		// 대출진행여부
	
	private String gender;			// 성별
	
	private String isBookMark;		// 즐겨찾기
	
	private String investProgress;	// 대출진행률
	
	private String eduFlag;			// 크레파스 교육 수료여부 
	
    private String corpGrade;		// (대출)회사에서 분류하는 회사등급
    
    private String categoryId;		// 회사, 개인 구분하기 위함
	
	
}
