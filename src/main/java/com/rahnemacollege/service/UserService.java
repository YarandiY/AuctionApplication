package com.rahnemacollege.service;

import com.google.common.collect.Lists;
import com.rahnemacollege.domain.SimpleUserDomain;
import com.rahnemacollege.domain.UserDomain;
import com.rahnemacollege.model.Auction;
import com.rahnemacollege.model.LoginInfo;
import com.rahnemacollege.model.User;
import com.rahnemacollege.repository.LoginInfoRepository;
import com.rahnemacollege.repository.UserRepository;
import com.rahnemacollege.util.exceptions.MessageException;
import com.rahnemacollege.util.exceptions.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class UserService {

    @Autowired
    private UserRepository repository;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private LoginInfoRepository loginInfoRepository;


    private final Logger logger;
    private final String VALID_EMAIL_REGEX = "^\\w+([\\.-]?\\w+)*@\\w+([\\.-]?\\w+)*(\\.\\w{2,3})+$";

    @Value("${server_ip}")
    private String ip;


    public UserService() {
        logger = LoggerFactory.getLogger(UserService.class);
    }

    public boolean isExist(String user_email) {
        return repository.findByEmail(user_email).isPresent();
    }

    public SimpleUserDomain addUser(String name, String email, String password) {
        if (!email.matches(VALID_EMAIL_REGEX))
            throw new MessageException(Message.EMAIL_INVALID);
        if (isExist(email))
            throw new MessageException(Message.EMAIL_DUPLICATED);
        if (password.length() < 6)
            throw new MessageException(Message.PASSWORD_TOO_LOW);
        if (password.length() > 100)
            throw new MessageException(Message.PASSWORD_TOO_HIGH);
        User user = new User(name, email, encoder.encode(password));
        repository.save(user);
        return new SimpleUserDomain(name, email);
    }

    public List<UserDomain> getAll() {
        ArrayList<UserDomain> users = new ArrayList<>();
        Lists.newArrayList(repository.findAll()).stream().map(this::toUserDomain).forEach(users::add);
        return users;
    }

    public void authenticate(int id, String password) throws MessageException {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(id, password));
        } catch (DisabledException e) {
            logger.error(e.getMessage());
        } catch (BadCredentialsException e) {
            throw new MessageException(Message.PASSWORD_INCORRECT);
        }
    }

    public Optional<User> findUserByEmail(String email) {
        Optional<User> user = repository.findByEmail(email);
        return user;
    }

    public User findUserId(int id) {
        return repository.findById(id).orElseThrow(() -> new MessageException(Message.INVALID_ID));
    }

    public SimpleUserDomain changePassword(User user, String newPassword) {
        user.setPassword(encoder.encode(newPassword));
        repository.save(user);
        return new SimpleUserDomain(user.getName(), user.getEmail());
    }

    public User edit(User user, String name, String email) throws MessageException {
        if (email != null && email.length() > 1 && !user.getEmail().equals(email)) {
            if (!email.matches(VALID_EMAIL_REGEX))
                throw new MessageException(Message.EMAIL_INVALID);
            user.setEmail(email);
        }
        if (name != null && name.length() > 0)
            user.setName(name);
        repository.save(user);
        return user;
    }

    public void addLoginInfo(User user) {
        LoginInfo loginInfo = new LoginInfo();
        loginInfo.setUser(user);
        loginInfo.setDate(new Date());
        loginInfoRepository.save(loginInfo);
    }

    public UserDomain toUserDomain(User user) {
        UserDomain userDomain;
        if (user.getPicture() != null && user.getPicture().length() > 1)
            userDomain = new UserDomain(user.getName(), user.getEmail(), "http://" + ip + user.getPicture());
        else
            userDomain = new UserDomain(user.getName(), user.getEmail(), user.getPicture());
        return userDomain;
    }

    @Transactional
    public List<Auction> getUserBookmarks(User user) {
        user = repository.findByEmail(user.getEmail()).orElseThrow(() -> new MessageException(Message.EMAIL_INVALID));
        return new ArrayList<>(user.getBookmarks()).stream().filter(a -> a.getState() == 0 || a.getState() == 2)
                .sorted((a, b) -> b.getId() - a.getId()).collect(Collectors.toList());
    }

}
