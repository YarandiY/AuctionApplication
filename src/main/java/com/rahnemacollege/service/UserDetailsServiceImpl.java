package com.rahnemacollege.service;


import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.rahnemacollege.model.User;
import com.rahnemacollege.repository.UserRepository;

import lombok.Getter;
import org.springframework.web.context.WebApplicationContext;

@Service
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Getter
    private User user;

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        User user = userRepository.findById(Integer.valueOf(id)).orElse(null);
        if (user == null)
            throw new UsernameNotFoundException("User not found with id: " + id);
        this.user = user;
        return new org.springframework.security.core.userdetails.User(user.getId().toString(), user.getPassword(), new ArrayList<>());
    }

}
