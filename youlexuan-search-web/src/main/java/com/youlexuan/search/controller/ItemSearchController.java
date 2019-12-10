package com.youlexuan.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.youlexuan.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/itemSearch")
public class ItemSearchController {
    @Reference
    ItemSearchService itemSearchService;


    @RequestMapping("/search")
    public Map search(@RequestBody Map searchMap ){

        return itemSearchService.search(searchMap);
    }


}
