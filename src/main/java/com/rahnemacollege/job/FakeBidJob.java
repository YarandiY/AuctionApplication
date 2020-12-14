package com.rahnemacollege.job;

import com.rahnemacollege.model.Auction;
import com.rahnemacollege.model.Bid;
import com.rahnemacollege.repository.BidRepository;
import com.rahnemacollege.service.AuctionService;
import com.rahnemacollege.util.MessageHandler;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class FakeBidJob extends QuartzJobBean {
    private static final Logger logger = LoggerFactory.getLogger(FinalizeAuctionJob.class);

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private AuctionService auctionService;

    private MessageHandler messageHandler;

    public FakeBidJob(SimpMessagingTemplate template){
        messageHandler = new MessageHandler(template);
    }


    @Override
    protected void executeInternal(JobExecutionContext context) {
        logger.info("Executing Job with key {}", context.getJobDetail().getKey());
        JobDataMap jobDataMap = context.getMergedJobDataMap();
        Auction auction = (Auction) jobDataMap.get("auction");
        Bid bid = new Bid();
        bid.setAuction(auction);
        bid.setPrice(auction.getBasePrice());
        bid.setUser(auction.getOwner());
        bid.setDate(auction.getDate());
        bidRepository.save(bid);
        auctionService.scheduleFinalizing(bid);
        messageHandler.newBidMessage(auction.getId(),auction.getBasePrice(), true);
        logger.info("Fake biding on auction Id#"+auction.getId()+" @"+new Date());
    }
}