package com.youlexuan.mapper;


import java.util.List;

import com.youlexuan.pojo.TbPayLog;
import com.youlexuan.pojo.TbPayLogExample;
import org.apache.ibatis.annotations.Param;

public interface TbPayLogMapper {
    int countByExample(TbPayLogExample example);

    int deleteByExample(TbPayLogExample example);

    int deleteByPrimaryKey(String outTradeNo);

    int insert(TbPayLog record);

    int insertSelective(TbPayLog record);

    List<TbPayLog> selectByExample(TbPayLogExample example);

    TbPayLog selectByPrimaryKey(String outTradeNo);

    int updateByExampleSelective(@Param("record") TbPayLog record, @Param("example") TbPayLogExample example);

    int updateByExample(@Param("record") TbPayLog record, @Param("example") TbPayLogExample example);

    int updateByPrimaryKeySelective(TbPayLog record);

    int updateByPrimaryKey(TbPayLog record);
}