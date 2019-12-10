package com.youlexuan.search.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.youlexuan.pojo.TbBrand;
import com.youlexuan.pojo.TbItem;
import com.youlexuan.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {
    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public Map<String, Object> search(Map searchMap) {
        //空格处理
        String keywords = (String) searchMap.get("keywords");
        searchMap.put("keywords", keywords.replace(" ", ""));
        Map<String,Object> map=new HashMap<>();
        //1.按关键字查询（高亮显示）
        map.putAll(searchList(searchMap));


        //2.根据关键字查询商品分类

        List<String> list = searchCategoryList(searchMap);

        map.put("categoryList",list);
        //3.查询品牌和规格列表
        if (list.size()>0){
            map.putAll(searchBrandAndSpecList(list.get(0)));
        }

        return map;
    }

    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    @Override
    public void deleteByGoodsIds(List goodsIdList) {
        System.out.println("删除商品ID"+goodsIdList);
        Query query=new SimpleQuery();
        Criteria criteria=new Criteria("item_goodsid").in(goodsIdList);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    public Map searchList(Map searchMap){
        Map map=new HashMap();
        HighlightQuery highlightQuery=new  SimpleHighlightQuery();
        HighlightOptions highlightOptions=new HighlightOptions().addField("item_title");
        highlightOptions.setSimplePrefix("<em style='color:red'>");//高亮前缀
        highlightOptions.setSimplePostfix("</em>");//高亮后缀
        //设置高亮的选项
        highlightQuery.setHighlightOptions(highlightOptions);

        //按照关键字查询
        Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
        highlightQuery.addCriteria(criteria);
        //二、分类过滤查询
        if(!"".equals(searchMap.get("category"))){
            Criteria criteria1 = new Criteria("item_category").is(searchMap.get("category"));
            FilterQuery filterQuery = new SimpleFacetQuery(criteria1);
            highlightQuery.addFilterQuery(filterQuery);
        }
        //三、品牌过滤查询
        if(!"".equals(searchMap.get("brand"))){
            Criteria criteria1 = new Criteria("item_brand").is(searchMap.get("brand"));
            FilterQuery filterQuery = new SimpleFacetQuery(criteria1);
            highlightQuery.addFilterQuery(filterQuery);
        }
        //四、规格过滤查询
        if(searchMap.get("spec") != null){
            Map<String,String> map1 = (Map)searchMap.get("spec");
            for(Map.Entry<String,String> entry : map1.entrySet()){
                Criteria criteria1 = new Criteria("item_spec_"+entry.getKey()).is(entry.getValue());
                FilterQuery filterQuery = new SimpleFacetQuery(criteria1);
                highlightQuery.addFilterQuery(filterQuery);
            }
        }
        //5按价格筛选.....
        if (!"".equals(searchMap.get("price"))){
            String []price= ((String) searchMap.get("price")).split("-");
            if (!price[0].equals("0")){
                Criteria criteria1=new Criteria("item_price").greaterThanEqual(price[0]);
                FilterQuery filterQuery=new SimpleFilterQuery(criteria1);
                highlightQuery.addFilterQuery(filterQuery);
            }
            if (!price[1].equals("*")){
                Criteria criteria1=new Criteria("item_price").lessThanEqual(price[1]);
                FilterQuery filterQuery=new SimpleFilterQuery(criteria1);
                highlightQuery.addFilterQuery(filterQuery);
            }
        }
        //1.6 分页查询
        Integer pageNo= (Integer) searchMap.get("pageNo");
        if (pageNo==null){
            pageNo=1;
        }
        Integer pageSize= (Integer) searchMap.get("pageSize");
        if (pageSize==null){
            pageSize=20;
        }
        highlightQuery.setRows(pageSize);
        highlightQuery.setOffset((pageNo-1)*pageSize);//从第几条数据开始查

        //1.7排序
        String sortValue= (String) searchMap.get("sort");//ASC DESC
        String sortField= (String) searchMap.get("sortField");//排序字段
        if (sortValue!=null&&sortField.length()>0){
            if (sortValue.equals("ASC")){
                Sort sort=new Sort(Sort.Direction.ASC,"item_"+sortField);
                highlightQuery.addSort(sort);
            }
            if(sortValue.equals("DESC")){
                Sort sort=new Sort(Sort.Direction.DESC, "item_"+sortField);
                highlightQuery.addSort(sort);
            }
        }


        //查询
        HighlightPage<TbItem> page=solrTemplate.queryForHighlightPage(highlightQuery,TbItem.class);
        //循环高亮入口集合
        for (HighlightEntry<TbItem> h:page.getHighlighted()){
            TbItem item=h.getEntity();//还原实体类
            if (h.getHighlights().size()>0&&h.getHighlights().get(0).getSnipplets().size()>0){
                //设置高亮的结果
                item.setTitle(h.getHighlights().get(0).getSnipplets().get(0));
            }
        }
        map.put("rows",page.getContent());
        map.put("totalPages", page.getTotalPages());//返回总页数
        map.put("total", page.getTotalElements());//返回总记录数
        return map;

    }

    /**
     * //2.根据关键字查询商品分类
     */
    public List<String> searchCategoryList(Map searchMap){
        List <String> list= new ArrayList<>();
        Query query=new SimpleQuery();

        //按照关键字查询
        Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //设置分组选项
        GroupOptions groupOptions=new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        //得到分组页
        GroupPage<TbItem> page=solrTemplate.queryForGroupPage(query,TbItem.class);

        //根据列得到分组结果集
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");

        //根据分组结果入口页
        Page<GroupEntry<TbItem>> groupEntries =groupResult.getGroupEntries();

        //得到分组入口集合
        List<GroupEntry<TbItem>> content=groupEntries.getContent();
        for (GroupEntry<TbItem> groupEntry:content){
            //将分组结果的名称封装到返回值中
            list.add(groupEntry.getGroupValue());
        }

        return list;
    }
    /**
     * 查询品牌规格
     */
    private Map searchBrandAndSpecList(String category){
        Map map=new HashMap();
        Long typeId= (Long) redisTemplate.boundHashOps("itemCat").get(category);
        if (typeId!=null){
            //根据ID查询品牌列表
            List<TbBrand> brandList= (List<TbBrand>) redisTemplate.boundHashOps("brandList").get(typeId);
            map.put("brandList",brandList);
            //根据模板ID查询规格
            List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
            map.put("specList", specList);
        }
        return map;
    }
}
