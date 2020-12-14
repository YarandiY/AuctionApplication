package com.rahnemacollege.controller;

import com.rahnemacollege.domain.Subscription;
import com.rahnemacollege.model.User;
import com.rahnemacollege.service.BidService;
import com.rahnemacollege.service.UserService;
import com.rahnemacollege.util.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;


@Controller
public class SessionUnsubscribeListener implements ApplicationListener<SessionUnsubscribeEvent> {

    @Autowired
    private BidService bidService;

    @Autowired
    private UserService userService;

    private Logger logger = LoggerFactory.getLogger(SessionUnsubscribeListener.class);

    private MessageHandler messageHandler;


    public SessionUnsubscribeListener(SimpMessagingTemplate template) {
        messageHandler = new MessageHandler(template);

    }

    @Override
    public void onApplicationEvent(SessionUnsubscribeEvent event) {
        logger.info("someone try to exit from auction");
        GenericMessage message = (GenericMessage) event.getMessage();
        StompHeaderAccessor headerAccessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        Integer userId = bidService.getUserId(headerAccessor.getUser().getName());
        if(userId == null){
            logger.error("the user is null");
            return;
        }
        User user = userService.findUserId(userId);
        String subscriptionId = String.valueOf(user.getId());
        Subscription subscription = bidService.getSubscription(subscriptionId);
        bidService.removeFromAllAuction(user);
        logger.info("user with id " + user.getId() + " exit from auction with id" + subscription.getAuction().getId());
        int current = bidService.getMembers(subscription.getAuction());
        messageHandler.subscribeMessage(subscription.getAuction().getId(), current);
    }
}
