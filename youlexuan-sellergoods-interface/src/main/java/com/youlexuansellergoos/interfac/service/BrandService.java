package com.youlexuansellergoos.interfac.service;

import com.youlexuan.entity.PageResult;
import com.youlexuan.pojo.TbBrand;

import java.util.List;
import java.util.Map;

public interface BrandService {
     List<TbBrand> findAll();
    PageResult findPage(int pageNum,int pageSize);
     void add(TbBrand brand);
     TbBrand findOne(Long id);

    void update(TbBrand brand);

    void delete(Long[] ids);

    PageResult search(TbBrand brand, int page, int rows);

    List<Map> selectOptionList();
}
