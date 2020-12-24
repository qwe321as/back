package com.crepass.restfulapi.one.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.crepass.restfulapi.v2.domain.LoansVO;
import com.crepass.restfulapi.v2.domain.LoansVO2;

public interface WishMapper {
    
    public List<?> selectWishById(String mid) throws Exception;

    public int insertWishById(String mid, String loanid) throws Exception;

    public int deleteWishById(Map<String, Object> loanList) throws Exception;

    // api v2 start
    public List<LoansVO> selectWishById2(@Param("pageNum") int pageNum, @Param("pageSize") int pageSize, @Param("mid") String mid) throws Exception;
    
    public List<LoansVO2> selectWishById2_1(@Param("pageNum") int pageNum, @Param("pageSize") int pageSize, @Param("mid") String mid) throws Exception;
    
    public int selectWishByIdSize(String mid) throws Exception;
}
