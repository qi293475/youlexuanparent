package com.youlexuansellergoos.interfac.service;

import com.youlexuan.entity.PageResult;
import com.youlexuan.group.Goods;
import com.youlexuan.pojo.TbGoods;
import com.youlexuan.pojo.TbItem;

import java.util.List;
/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface GoodsService {
	/**
	 * 根据商品ID和状态查询Item表信息
	 */

	public List<TbItem> findItemListByGoodsIdandStatus(Long[] goodsIds, String status );

	/**
	 * 批量修改状态
	 * @param ids
	 * @param status
	 */
	public void updateStatus(Long []ids,String status);


	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbGoods> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);
	
	
	/**
	 * 增加
	*/
	public void add(Goods  goods);
	
	
	/**
	 * 修改
	 */
	public void update(Goods goods);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public Goods findOne(Long id);
	
	
	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long[] ids);
	public void deleteShop(Long[] ids);
	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize);
	
}
