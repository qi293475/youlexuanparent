package com.youlexuan.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.youlexuan.cart.service.CartService;
import com.youlexuan.entity.Result;
import com.youlexuan.pojo.Cart;
import com.youlexyan.common.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {
    @Reference
    private CartService cartService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @RequestMapping("/findCartList")
    public List<Cart> findCartList(){
        //得到登陆人账号,判断当前是否有人登陆
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        //当用户未登陆时，username的值为anonymousUser
        String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");

        if (cartListString == null || cartListString.equals("")) {
            cartListString = "[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
        if(name.equals("anonymousUser")) {//如果未登录
            return cartList_cookie;
        }else {
            //如果已登录
            List<Cart> cartList_redis =cartService.findCartListFromRedis(name);//从redis中提取
            if(cartList_cookie.size()>0){//如果本地存在购物车
                //合并购物车
                cartList_redis=cartService.mergeCartList(cartList_redis, cartList_cookie);
                //清除本地cookie的数据
                CookieUtil.deleteCookie(request, response, "cartList");
                //将合并后的数据存入redis
                cartService.saveCartListToRedis(name, cartList_redis);
            }
            return cartList_redis;
        }
    }
    @RequestMapping("/addGoodsToCartList")
    public Result addGoodsToCartList(Long itemId, Integer num){
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
        response.setHeader("Access-Control-Allow-Credentials", "true");//允许传入cookie
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            System.out.println("当前登录用户："+username);
            List<Cart> cartList=findCartList();//获取购物车列表
            cartList=cartService.addGoodsToCartList(cartList,itemId,num);

            if(username.equals("anonymousUser")){ //如果是未登录，保存到cookie

                CookieUtil.setCookie(request,response,"cartList",JSON.toJSONString(cartList),3600*24,"UTF-8");

            }else {
                cartService.saveCartListToRedis(username, cartList);
            }
            return new Result(true, "添加成功");
        }catch (Exception e){
            return new Result(false, "添加失败");
        }
    }
}
