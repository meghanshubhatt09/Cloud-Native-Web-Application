package com.webservices.cloudwebapp.webapp.service;

import com.webservices.cloudwebapp.webapp.model.User;
import com.webservices.cloudwebapp.webapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.apache.commons.validator.routines.EmailValidator;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;


    public User registerUser(User userDetail) {

        userDetail.setPassword(passwordEncoder.encode(userDetail.getPassword()));
        userDetail.setAccount_created(LocalDateTime.now());
        userDetail.setAccount_updated(LocalDateTime.now());
        userDetail.setVerified(false);
        userRepository.save(userDetail);
        return userDetail;

    }

    public List<User> getAllUser() {
        return (List<User>) userRepository.findAll();
    }

    public ResponseEntity updateUser(Optional<User> userDetail, User newDetail) {

        boolean updateHappend = false;

        if (newDetail.getFirst_name()!=null) {
            if (!newDetail.getFirst_name().isEmpty()) {
                updateHappend = true;
                userDetail.get().setFirst_name(newDetail.getFirst_name());
            }
        }


        if (newDetail.getLast_name()!=null) {
            if (!newDetail.getLast_name().isEmpty()) {
                updateHappend = true;
                userDetail.get().setLast_name(newDetail.getLast_name());
            }
        }

        if (newDetail.getPassword()!=null) {
            if (!newDetail.getPassword().isEmpty()) {
                updateHappend = true;
                //user.setPassword(passwordEncoder.encode(user.getPassword()));
                userDetail.get().setPassword(passwordEncoder.encode(newDetail.getPassword()));
            }
        }

        if (updateHappend == true)
        {
            userDetail.get().setAccount_updated(LocalDateTime.now());
            userRepository.save(userDetail.get());
            return new ResponseEntity(userDetail,HttpStatus.OK);
        }
        return new ResponseEntity(userDetail,HttpStatus.BAD_REQUEST);


    }

    public boolean isEmailPresent(String username) {
        return userRepository.findByEmail(username);
    }

    public boolean isEmailVaild(String email){
        boolean allowLocal = true;
        boolean valid = EmailValidator.getInstance(allowLocal).isValid(email);
        return valid;
    }
}
