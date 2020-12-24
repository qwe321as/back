package com.crepass.restfulapi.one.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crepass.restfulapi.one.dao.BoardMapper;
import com.crepass.restfulapi.one.domain.Board;
import com.crepass.restfulapi.one.domain.BoardQna;
import com.crepass.restfulapi.one.domain.OneEventItem;
import com.crepass.restfulapi.one.domain.OneNoticeMain;

@Service
public class BoardService {
    
    @Autowired
    BoardMapper boardmapper;
    
    public Board selectBoardById(String table, String id) throws Exception {
        return boardmapper.selectBoardById(table, id);
    }
    
    public List<Board> selectBoardByAll(String table) throws Exception {
        return boardmapper.selectBoardByAll(table);
    }

    public List<BoardQna> selectQnaByAll() throws Exception {
        return boardmapper.selectQnaByAll();
    }
    
    public OneNoticeMain selectNoticeMain() throws Exception {
    	return boardmapper.selectNoticeMain();
    }

	public OneEventItem selectOneEventItem(String mType) throws Exception {
		return boardmapper.selectOneEventItem(mType);
	}

	public OneNoticeMain selectOneEventItemV2(String mType) throws Exception {
		return boardmapper.selectOneEventItemV2(mType);
	}

	public String selectGetBannerId() throws Exception {
		return boardmapper.selectGetBannerId();
	}

    public OneNoticeMain selectNoticeMainV2() throws Exception {
    	return boardmapper.selectNoticeMainV2();
    }


}
