package com.rahnemacollege.model;


import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Min;
import java.util.Date;

@Data
@Entity
@Table(name = "Bids")
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne
    @JoinColumn(name = "auction_id")
    private Auction auction;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @Min(value = 0, message = "Price could not be negative value.")
    private long price;
    private Date date;


    public Bid() {

    }

    public Bid(Auction auction, User user, long price, Date date) {
        this.auction = auction;
        this.user = user;
        this.price = price;
        this.date = date;
    }


}
