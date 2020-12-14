package com.rahnemacollege.domain;


import lombok.Data;

import java.util.List;


@Data
public class AuctionDomain {

    private String title;
    private long date = -1;
    private int categoryId;
    private int maxNumber = -1;
    private boolean bookmark = false;
    private boolean mine = false;
    private int id;
    private int current = 0;
    private int state = 0;

    private List<String> pictures;


    public AuctionDomain(String title,
                         long date,
                         int categoryId,
                         int maxNumber,
                         int id,
                         int current,
                         int state) {
        this.title = title;
        this.date = date;
        this.categoryId = categoryId;
        this.maxNumber = maxNumber;
        this.id = id;
        this.current = current;
        this.state = state;
    }

}
