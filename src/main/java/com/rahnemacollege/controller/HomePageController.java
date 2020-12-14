package com.rahnemacollege.controller;

import com.rahnemacollege.domain.AuctionDomain;
import com.rahnemacollege.model.Auction;
import com.rahnemacollege.model.User;
import com.rahnemacollege.service.AuctionService;
import com.rahnemacollege.service.BidService;
import com.rahnemacollege.service.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/home")
public class HomePageController {

    private final AuctionService auctionService;
    private final Logger log;
    private final UserDetailsServiceImpl userDetailsService;
    private final BidService bidService;

    public HomePageController(AuctionService auctionService, UserDetailsServiceImpl userDetailsService, BidService bidService) {
        this.auctionService = auctionService;
        this.userDetailsService = userDetailsService;
        this.bidService = bidService;
        log = LoggerFactory.getLogger(AuctionController.class);
    }

    @PostMapping("/search/{category}")
    public PagedResources<Resource<AuctionDomain>> search(@RequestParam("title") String title,
                                                          @PathVariable int category,
                                                          @RequestParam(value = "hottest", defaultValue = "false") boolean hottest,
                                                          @RequestParam("page") int page,
                                                          @RequestParam("size") int size,
                                                          PagedResourcesAssembler<AuctionDomain> assembler) {
        log.info("search");
        Page<Auction> auctions = auctionService.findByTitle(title, category, hottest, page, size);
        User user = userDetailsService.getUser();
        Page<AuctionDomain> auctionDomains = auctions.map(a ->
                auctionService.toAuctionDomain(a, user, bidService.getMembers(a)));
        log.info("user with email " + user.getEmail() + " search for title " + title);
        return assembler.toResource(auctionDomains);
    }

}
