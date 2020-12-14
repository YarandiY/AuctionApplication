package com.rahnemacollege.controller;


import com.google.gson.Gson;
import com.rahnemacollege.domain.AddAuctionDomain;
import com.rahnemacollege.domain.AuctionDetail;
import com.rahnemacollege.domain.AuctionDomain;
import com.rahnemacollege.domain.UserDomain;
import com.rahnemacollege.model.Auction;
import com.rahnemacollege.model.Category;
import com.rahnemacollege.model.User;
import com.rahnemacollege.service.*;
import com.rahnemacollege.util.exceptions.Message;
import com.rahnemacollege.util.exceptions.MessageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/auctions")
public class AuctionController {

    private final AuctionService auctionService;
    private final UserService userService;
    private final PictureService pictureService;
    private final BidService bidService;
    private final Logger log;
    private UserDetailsServiceImpl userDetailsService;


    public AuctionController(AuctionService auctionService, UserDetailsServiceImpl userDetailsService, UserService userService, PictureService pictureService, BidService bidService) {
        this.auctionService = auctionService;
        this.userDetailsService = userDetailsService;
        this.userService = userService;
        this.pictureService = pictureService;
        this.bidService = bidService;
        log = LoggerFactory.getLogger(AuctionController.class);
    }


    @GetMapping("/category")
    public ResponseEntity<List<Category>> getCategory() {
        log(" call get category");
        return new ResponseEntity<>(auctionService.getCategory(), HttpStatus.OK);
    }

    @PostMapping(value = "/add")
    public ResponseEntity<AuctionDomain> add(@RequestBody AddAuctionDomain addAuctionDomain) {
        log(" call add an auction");
        User user = userDetailsService.getUser();
        Auction auction = auctionService.addAuction(addAuctionDomain, user);
        log(" added auction");
        int current = bidService.getMembers(auction);
        AuctionDomain auctionDomain = auctionService.toAuctionDomain(auction, user, current);
        return new ResponseEntity<>(auctionDomain, HttpStatus.OK);
    }

    @PostMapping(value = "/add/picture/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AuctionDomain> addPicture(@PathVariable int id, @RequestBody MultipartFile[] images) {
        log(" try to add pictures for her/his auction.");
        Auction auction = auctionService.findById(id);
        if (images != null && images.length > 0) {
            pictureService.setAuctionPictures(auction, images);
            log(" added picture for auction " + auction.getId());
        }
//            throw new MessageException(Message.PICTURE_NULL);
        User user = userDetailsService.getUser();
        int current = bidService.getMembers(auction);
        AuctionDomain auctionDomain = auctionService.toAuctionDomain(auction, user, current);
        return new ResponseEntity<>(auctionDomain, HttpStatus.OK);
    }

    @GetMapping("/find/{id}")
    public ResponseEntity<AuctionDetail> one(@PathVariable int id) {
        log(" try to find an auction");
        User user = userDetailsService.getUser();
        Auction auction = auctionService.findById(id);
        log(" find the auction with title " + auction.getTitle());
        long lastPrice = bidService.findLastPrice(auction);
        Long latestBidTime = bidService.findLatestBidTime(auction);
        int members = bidService.getMembers(auction);
        AuctionDomain auctionDomain = auctionService.toAuctionDomain(auction, user, members);
        User winner = auction.getWinner();
        AuctionDetail auctionDetail;
        if (winner != null)
            auctionDetail = new AuctionDetail(auctionDomain, auction.getDescription(), lastPrice, new UserDomain(winner), latestBidTime);
        else
            auctionDetail = new AuctionDetail(auctionDomain, auction.getDescription(), lastPrice, null, latestBidTime);
        System.out.println(new Gson().toJson(auctionDetail));
        return new ResponseEntity<>(auctionDetail, HttpStatus.OK);
    }


    @RequestMapping(value = "/bookmark", method = RequestMethod.POST)
    public ResponseEntity<AddAuctionDomain> addBookmark(@RequestParam("auctionId") Integer id) {
        User user = userService.findUserId(userDetailsService.getUser().getId());
        log.info(user.getEmail() + " tried to add bookmark");
        if (id != null) {
            Auction auction = auctionService.findById(id);
            if (auction != null) {
                auctionService.addBookmark(user, auction);
                AddAuctionDomain addAuctionDomain = new AddAuctionDomain(auction);
                log.info("Auction Id#" + auction.getId() + " just added/removed to " + user.getEmail() + "'s bookmarks");
                return new ResponseEntity<>(addAuctionDomain, HttpStatus.OK);
            }
            throw new MessageException(Message.REALLY_BAD_SITUATION);
        }
        throw new MessageException(Message.INVALID_ID);
    }


    private void log(String action) {
        log.info(userDetailsService.getUser().getName() + " with id " + userDetailsService.getUser().getId() + action);
    }

}



