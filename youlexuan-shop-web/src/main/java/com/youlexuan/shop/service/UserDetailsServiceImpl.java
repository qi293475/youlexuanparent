package com.youlexuan.shop.service;

import com.youlexuan.pojo.TbSeller;
import com.youlexuansellergoos.interfac.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class UserDetailsServiceImpl implements UserDetailsService {

    private SellerService sellerService;

    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("========="+username);
        List<GrantedAuthority> grantedAuthorities=new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_SELLER"));
        TbSeller tbSeller = sellerService.findOne(username);
        System.out.println("tbseller"+tbSeller.getPassword());
        if (tbSeller!=null&&"1".equals(tbSeller.getStatus())){
            System.out.println("========================");
                return new User(username,tbSeller.getPassword(),grantedAuthorities);
        }else{
            return null;
        }

    }
}
