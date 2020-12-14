package com.rahnemacollege.service;


import com.rahnemacollege.domain.BidRequest;
import com.rahnemacollege.domain.Subscription;
import com.rahnemacollege.model.Auction;
import com.rahnemacollege.model.Bid;
import com.rahnemacollege.model.User;
import com.rahnemacollege.repository.AuctionRepository;
import com.rahnemacollege.repository.BidRepository;
import com.rahnemacollege.repository.OnlinePeopleRepository;
import com.rahnemacollege.util.NumberHandler;
import com.rahnemacollege.util.exceptions.MessageException;
import com.rahnemacollege.util.exceptions.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class BidService {


    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private AuctionRepository auctionRepository;

    private OnlinePeopleRepository peopleRepository;
    private final NumberHandler numberHandler = new NumberHandler();

    private Logger logger;


    public BidService() {
        logger = LoggerFactory.getLogger(BidService.class);
        peopleRepository = OnlinePeopleRepository.getInstance();
    }


    public Bid add(@NotNull BidRequest request, @NotNull User user) {
        Auction auction = auctionRepository
                .findById(request.getAuctionId()).orElseThrow(() ->
                        new MessageException(Message.AUCTION_NOT_FOUND));
        if (auction.getState() == 1)
            throw new MessageException(Message.FINISHED_AUCTION);
        if (user.equals(auction.getOwner()))
            throw new MessageException(Message.THE_USER_IS_THE_OWNER_OF_THE_AUCTION);
        if (auction.getDate().getTime() > new Date().getTime())
            throw new MessageException(Message.THE_AUCTION_DIDNT_START_YET);
        if (!peopleRepository.isInAuction(auction.getId(), user))
            throw new MessageException(Message.THE_USER_IS_NOT_IN_AUCTION);
        if (bidRepository.findTopByAuction_idOrderByIdDesc(auction.getId()).isPresent()
                && bidRepository.findTopByAuction_idOrderByIdDesc(auction.getId()).get().getUser().getId().equals(user.getId())) {
            throw new MessageException(Message.ALREADY_BID);
        }
        long lastPrice = findLastPrice(auction);
        long bidPrice = numberHandler.createNumberLong(request.getPrice());
        if (bidPrice <= lastPrice)
            throw new MessageException(Message.PRICE_TOO_LOW);
        Bid bid = new Bid(auction, user, bidPrice, new Date());
        bid = bidRepository.save(bid);
        if (auction.getState() == 0) {
            auction.setState(2);
            auctionRepository.save(auction);
        }
        return bid;
    }

    public long findLastPrice(Auction auction) {
        return bidRepository.findTopByAuction_idOrderByIdDesc(auction.getId()).map(Bid::getPrice).orElseGet(auction::getBasePrice);
    }

    public Long findLatestBidTime(Auction auction) {
        Optional<Date> date = bidRepository.findTopByAuction_idOrderByIdDesc(auction.getId()).map(Bid::getDate);
        if (date.isPresent()) {
            return date.get().getTime();
        } else {
            return null;
        }
    }

    public int enter(Auction auction, User user) {
        if (auction.getDate().getTime() > new Date().getTime())
            throw new MessageException(Message.THE_AUCTION_DIDNT_START_YET);
        if (peopleRepository.isInAuction(auction.getId(), user)) {
            logger.warn("the user with id " + user.getId() + "was in auction with id " + auction.getId());
            throw new MessageException(Message.THE_USER_WAS_IN_AUCTION);
        }
        if(peopleRepository.isInAuction(user)){
            throw new MessageException(Message.THE_USER_IS_IN_ANOTHER_AUCTION);
        }
        if (auction.getState() == 1)
            throw new MessageException(Message.FINISHED_AUCTION);
        peopleRepository.add(auction, user);
        return peopleRepository.getMembers(auction.getId()).size();
    }

    public int getMembers(Auction auction) {
        List<User> users = peopleRepository.getMembers(auction.getId());
        if (users == null)
            return 0;
        return users.size();
    }

    public void removeFromAllAuction(User user) {
        peopleRepository.ExitUser(user);
    }

    public void addSubscriptionId(String subscriptionId, Auction auction, User user) {
        peopleRepository.addSubscriptionId(subscriptionId, new Subscription(auction, user));
    }

    public void removeAuction(int auctionId) {
        peopleRepository.removeAuction(auctionId);
    }

    public Subscription getSubscription(String subscriptionId) {
        return peopleRepository.getAuctionId(subscriptionId);
    }

    public void addDeviceId(String deviceId, User user){
        peopleRepository.addDeviceId(deviceId, user);
    }

    public Integer getUserId(String deviceId){
        Integer userId = peopleRepository.getUserId(deviceId);
        return userId;
    }

    public String getDeviceId(User user){
        String deviceId = peopleRepository.getDeviceId(user);
        if(deviceId == null)
            throw new MessageException(Message.USER_NOT_FOUND);
        return deviceId;
    }

    public boolean userIsOnline(User user){
        return peopleRepository.userExist(user);
    }
    public void removeDeviceId(String deviceId){
        peopleRepository.removeDeviceId(deviceId);
    }

}
