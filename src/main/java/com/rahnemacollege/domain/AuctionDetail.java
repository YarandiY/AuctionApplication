package com.rahnemacollege.domain;

import lombok.Data;

import java.util.List;

@Data
public class AuctionDetail {

    private String title;
    private String description;
    private long basePrice = -1;
    private long date = -1;
    private int categoryId;
    private int maxNumber = -1;
    private boolean bookmark = false;
    private boolean mine = false;
    private int id;
    private List<String> pictures;
    private int current;
    private Long latestBidTime = null;
    private int state = 0;
    private UserDomain winner;

    public AuctionDetail(AuctionDomain auctionDomain,
                         String description,
                         long lastPrice,
                         UserDomain winner,
                         Long latestBidTime) {
        this.title = auctionDomain.getTitle();
        this.description = description;
        this.basePrice = lastPrice;
        this.date = auctionDomain.getDate();
        this.categoryId = auctionDomain.getCategoryId();
        this.maxNumber = auctionDomain.getMaxNumber();
        this.pictures = auctionDomain.getPictures();
        this.mine = auctionDomain.isMine();
        this.bookmark = auctionDomain.isBookmark();
        this.id = auctionDomain.getId();
        this.current = auctionDomain.getCurrent();
        this.latestBidTime = latestBidTime;
        this.state = auctionDomain.getState();
        this.winner = winner;
    }


}
