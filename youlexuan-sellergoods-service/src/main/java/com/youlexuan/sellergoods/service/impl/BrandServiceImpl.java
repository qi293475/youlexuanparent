package com.youlexuan.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.youlexuan.entity.PageResult;
import com.youlexuan.mapper.TbBrandMapper;
import com.youlexuan.pojo.TbBrand;
import com.youlexuan.pojo.TbBrandExample;
import com.youlexuansellergoos.interfac.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Service
public class BrandServiceImpl implements BrandService {
    @Autowired
    TbBrandMapper tbBrandMapper;

    @Override
    public List<TbBrand> findAll() {
        return tbBrandMapper.selectByExample(null);

    }

    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        List<TbBrand> list = tbBrandMapper.selectByExample(null);
        Page<TbBrand> page= (Page<TbBrand>) list;

        return new PageResult((int) page.getTotal(),page.getResult());
    }

    @Override
    public void add(TbBrand brand) {
        tbBrandMapper.insert(brand);
    }

    @Override
    public TbBrand findOne(Long id) {
        return tbBrandMapper.selectByPrimaryKey(id);
    }

    @Override
    public void update(TbBrand brand) {
        tbBrandMapper.updateByPrimaryKey(brand);
    }

    @Override
    public void delete(Long[] ids) {
        for (int i = 0; i < ids.length; i++) {
            tbBrandMapper.deleteByPrimaryKey(ids[i]);
        }
    }

    @Override
    public PageResult search(TbBrand brand, int page, int rows) {
        //1、设定查询起始页
        PageHelper.startPage(page,rows);
        //2.模糊查询条件
        TbBrandExample example=new TbBrandExample();
        TbBrandExample.Criteria criteria=example.createCriteria();
        if (brand!=null){
            if (brand.getName()!=null&&brand.getName().length()>0){
                criteria.andNameLike("%"+brand.getName()+"%");
            }if (brand.getFirstChar()!=null&&brand.getFirstChar().length()>0){
                criteria.andFirstCharEqualTo(brand.getFirstChar());
            }
        }
        //3、查询
        Page<TbBrand> p = (Page<TbBrand>) tbBrandMapper.selectByExample(example);

        return new PageResult((int) p.getTotal(),p.getResult());
    }

    @Override
    public List<Map> selectOptionList() {
        return tbBrandMapper.selectOptionList();
    }
}
