package com.rahnemacollege.domain;


import com.rahnemacollege.model.Auction;
import com.rahnemacollege.model.User;
import lombok.Data;

@Data
public class Subscription {

    private User user;
    private Auction auction;



    public Subscription(Auction auction, User user){
        this.user = user;
        this.auction = auction;
    }

    public String toString(){
        return user.getEmail();
    }
}
