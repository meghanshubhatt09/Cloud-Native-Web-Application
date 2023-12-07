package com.webservices.cloudwebapp.webapp.security;

import com.webservices.cloudwebapp.webapp.model.User;
import com.webservices.cloudwebapp.webapp.repository.UserRepository;
import com.webservices.cloudwebapp.webapp.service.UserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetailService loadUserByUsername(String username) throws UsernameNotFoundException {
        User user= this.userRepository.findUserByUsername(username);
        if(user==null){
            throw new UsernameNotFoundException("No user with the provided username");
        }
        return new UserDetailService(user);
    }




}
