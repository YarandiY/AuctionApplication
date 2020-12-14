package com.rahnemacollege.service;

import com.rahnemacollege.model.ResetRequest;
import com.rahnemacollege.model.User;
import com.rahnemacollege.repository.ResetRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class ResetRequestService {
    private final ResetRequestRepository repository;
    private final long tokenExpireTime = 10800000; //in ms

    @Autowired
    public ResetRequestService(ResetRequestRepository repository) {
        this.repository = repository;
    }

    public Optional<ResetRequest> findByUser(User user) {
        return repository.findByUser(user);
    }

    public void addRequest(ResetRequest request) {
        this.repository.save(request);
    }

    public String registerResetRequest(User user) {
        String token;
        ResetRequest resetRequest;
        if (findByUser(user).isPresent()) {
            resetRequest = findByUser(user).get();
            if (new Date().getTime() - resetRequest.getDate().getTime() < tokenExpireTime) {
                token = resetRequest.getToken();
            } else {
                token = generateToken();
                resetRequest.setToken(token);
                addRequest(resetRequest);
            }
        } else {
            token = generateToken();
            resetRequest = new ResetRequest(user, new Date(), token);
            addRequest(resetRequest);
        }
        return token;
    }

    public String generateToken() {
        return UUID.randomUUID().toString();
    }

    public Optional<ResetRequest> findByToken(String token) {
        return repository.findByToken(token);
    }

    public void removeRequest(ResetRequest request) {
        repository.delete(request);
    }

}
