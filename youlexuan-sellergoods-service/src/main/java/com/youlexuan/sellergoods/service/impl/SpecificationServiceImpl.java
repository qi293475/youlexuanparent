package com.youlexuan.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.youlexuan.entity.PageResult;
import com.youlexuan.group.Specification;
import com.youlexuan.mapper.TbSpecificationMapper;
import com.youlexuan.mapper.TbSpecificationOptionMapper;
import com.youlexuan.pojo.TbSpecification;
import com.youlexuan.pojo.TbSpecificationExample;
import com.youlexuan.pojo.TbSpecificationExample.Criteria;
import com.youlexuan.pojo.TbSpecificationOption;
import com.youlexuan.pojo.TbSpecificationOptionExample;
import com.youlexuansellergoos.interfac.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SpecificationServiceImpl implements SpecificationService {

	@Autowired
	private TbSpecificationMapper specificationMapper;

	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbSpecification> findAll() {
		return specificationMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSpecification> page=   (Page<TbSpecification>) specificationMapper.selectByExample(null);
		return new PageResult((int) page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Specification specification) {

		specificationMapper.insert(specification.getSpecification());
		for (TbSpecificationOption specificationOption:specification.getSpecificationOptionList()){
			specificationOption.setSpecId(specification.getSpecification().getId());
            specificationOptionMapper.insert(specificationOption);
            System.out.println("=================="+specificationOption);
        }

	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(Specification specification){
        //先修改规格
        specificationMapper.updateByPrimaryKey(specification.getSpecification());
        for (TbSpecificationOption option:specification.getSpecificationOptionList()){
            if (option.getId()!=null){
                specificationOptionMapper.updateByPrimaryKey(option);
            }else {
                option.setSpecId(specification.getSpecification().getId());
                specificationOptionMapper.insert(option);
            }
        }
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Specification findOne(Long id){
	    //查询规格
        TbSpecification tbSpecification = specificationMapper.selectByPrimaryKey(id);
        Specification specification=new Specification();
        specification.setSpecification(tbSpecification);

        TbSpecificationOptionExample tbSpecificationOptionExample=new TbSpecificationOptionExample();
        TbSpecificationOptionExample.Criteria criteria = tbSpecificationOptionExample.createCriteria();
        criteria.andSpecIdEqualTo(id);
        List<TbSpecificationOption> tbSpecificationOptions = specificationOptionMapper.selectByExample(tbSpecificationOptionExample);

        specification.setSpecificationOptionList(tbSpecificationOptions);
        //查询规格列表
        return specification;

	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			//先删除规格
			specificationMapper.deleteByPrimaryKey(id);
			//在删除规格细节
			TbSpecificationOptionExample example=new TbSpecificationOptionExample();
			TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
			criteria.andSpecIdEqualTo(id);
			specificationOptionMapper.deleteByExample(example);
		}
	}
	
	
		@Override
	public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSpecificationExample example=new TbSpecificationExample();
		Criteria criteria = example.createCriteria();
		
		if(specification!=null){			
						if(specification.getSpecName()!=null && specification.getSpecName().length()>0){
				criteria.andSpecNameLike("%"+specification.getSpecName()+"%");
			}	
		}
		
		Page<TbSpecification> page= (Page<TbSpecification>)specificationMapper.selectByExample(example);		
		return new PageResult((int) page.getTotal(), page.getResult());
	}

	@Override
	public List<Map> selectSpecList() {
		return specificationMapper.selectSpecList();
	}

}
