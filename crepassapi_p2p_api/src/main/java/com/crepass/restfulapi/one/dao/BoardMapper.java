package com.crepass.restfulapi.one.dao;

import java.util.List;

import com.crepass.restfulapi.one.domain.Board;
import com.crepass.restfulapi.one.domain.BoardQna;
import com.crepass.restfulapi.one.domain.OneEventItem;
import com.crepass.restfulapi.one.domain.OneNoticeMain;

public interface BoardMapper {
    
    public Board selectBoardById(String table, String id) throws Exception;
    
    public List<Board> selectBoardByAll(String table) throws Exception;

    public List<BoardQna> selectQnaByAll() throws Exception;
    
    public OneNoticeMain selectNoticeMain() throws Exception;

	public OneEventItem selectOneEventItem(String mType) throws Exception;

	public OneNoticeMain selectOneEventItemV2(String mType) throws Exception;

	public String selectGetBannerId() throws Exception;
    
    public OneNoticeMain selectNoticeMainV2() throws Exception;
    

}
