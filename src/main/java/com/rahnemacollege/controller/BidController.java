package com.rahnemacollege.controller;


import com.google.gson.JsonObject;
import com.rahnemacollege.domain.BidRequest;
import com.rahnemacollege.model.Auction;
import com.rahnemacollege.model.Bid;
import com.rahnemacollege.model.User;
import com.rahnemacollege.service.AuctionService;
import com.rahnemacollege.service.BidService;
import com.rahnemacollege.service.UserService;
import com.rahnemacollege.util.MessageHandler;
import com.rahnemacollege.util.exceptions.MessageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
public class BidController {


    @Autowired
    private BidService bidService;
    @Autowired
    private UserService userService;
    @Autowired
    private AuctionService auctionService;

    private final Logger logger = LoggerFactory.getLogger(BidController.class);

    private MessageHandler messageHandler;

    public BidController(SimpMessagingTemplate template) {
        messageHandler = new MessageHandler(template);
    }


    @MessageMapping("/bid")
    public void bid(BidRequest request, Message<?> message) {
        logger.info("someone try to bid");
        StompHeaderAccessor headerAccessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        Integer userId = bidService.getUserId(headerAccessor.getUser().getName());
        if (userId == null){
            logger.error("user is null ");
            return;
        }
        User user = userService.findUserId(userId);
        logger.info(user.getEmail() + " with id " + user.getId() + " wants to bid auction " + request.getAuctionId());
        Bid bid = bidService.add(request, user);
        auctionService.scheduleFinalizing(bid);
        logger.info("bid accepted");
        messageHandler.newBidMessage(request.getAuctionId(), bid.getPrice(), false);
        messageHandler.myBidMessage(headerAccessor.getUser().getName(), bid.getPrice());
    }



    @SubscribeMapping("/id/{auctionId}")
    public void enterAuction(@DestinationVariable("auctionId") int auctionId,
                             Message<?> message) throws Exception {
        logger.info("someone try to enter auction " + auctionId);
        StompHeaderAccessor headerAccessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        Auction auction = auctionService.findById(auctionId);
        Integer userId = bidService.getUserId(headerAccessor.getUser().getName());
        if (userId == null){
            logger.error("user is null");
            return;
        }
        User user = userService.findUserId(userId);
        System.err.println(user.getName() + " enter in the auction " + auction.getTitle());
        bidService.addSubscriptionId(String.valueOf(userId), auction, user);
        int current = bidService.enter(auction, user);
        messageHandler.subscribeMessage(auctionId, current);
    }

    @MessageExceptionHandler
    public void enterDenied(MessageException e, Message<?> message) throws Exception {
        logger.error("the exception is : " + e.getMessage());
        StompHeaderAccessor headerAccessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        messageHandler.exceptionMessage(e, headerAccessor.getUser().getName());
    }


}
