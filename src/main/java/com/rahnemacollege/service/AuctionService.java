package com.rahnemacollege.service;


import com.google.common.collect.Lists;
import com.rahnemacollege.domain.AddAuctionDomain;
import com.rahnemacollege.domain.AuctionDomain;
import com.rahnemacollege.job.FakeBidJob;
import com.rahnemacollege.job.FinalizeAuctionJob;
import com.rahnemacollege.job.NotifyBookmarkedAuctionJob;
import com.rahnemacollege.model.Auction;
import com.rahnemacollege.model.Bid;
import com.rahnemacollege.model.Category;
import com.rahnemacollege.model.User;
import com.rahnemacollege.repository.*;
import com.rahnemacollege.util.MessageHandler;
import com.rahnemacollege.util.NumberHandler;
import com.rahnemacollege.util.exceptions.Message;
import com.rahnemacollege.util.exceptions.MessageException;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuctionService {

    private final UserRepository userRepository;
    private final AuctionRepository auctionRepository;
    private final CategoryRepository categoryRepository;
    private final PictureRepository pictureRepository;
    private final Logger logger;
    private final NumberHandler numberHandler = new NumberHandler();

    @Value("${server_ip}")
    private String ip;

    @Autowired
    private Scheduler scheduler;

    private final long AUCTION_ACTIVE_SESSION_TIME = 30000L;
    private final String finalizeAuctionTriggerName = "FTrigger-";
    private final String finalizeAuctionTriggerGroup = "FinalizeAuction-triggers";
    private final String finalizeAuctionJobGroup = "FinalizeAuction-jobs";

    private final long REMAINING_TIME_TO_NOTIFY = 600000L;
    private final String notifyBookmarkedAuctionTriggerGroup = "NotifyAuction-triggers";
    private final String notifyBookmarkedAuctionJobGroup = "NotifyAuction-jobs";

    private final String fakeBidTriggerName = "FakeBidTrigger-";
    private final String fakeBidTriggerGroup = "FakeBid-triggers";
    private final String fakeBidJobGroup = "FakeBid-jobs";

    private MessageHandler messageHandler;


    @Autowired
    public AuctionService(SimpMessagingTemplate template, UserRepository userRepository, AuctionRepository auctionRepository, CategoryRepository categoryRepository,
                          PictureRepository pictureRepository) {
        this.userRepository = userRepository;
        this.auctionRepository = auctionRepository;
        this.categoryRepository = categoryRepository;
        this.pictureRepository = pictureRepository;
        this.logger = LoggerFactory.getLogger(AuctionService.class);
        messageHandler = new MessageHandler(template);
    }

    public Auction addAuction(AddAuctionDomain auctionDomain, User user) {
        validation(auctionDomain);
        Auction auction = toAuction(auctionDomain, user);
        auction = auctionRepository.save(auction);
        scheduleFakeBidOn(auction);
        return auction;
    }


    private Trigger buildFakeBidJobTrigger(JobDetail jobDetail, Date finishDate, int auctionId) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(TriggerKey.triggerKey(fakeBidTriggerName + auctionId, fakeBidTriggerGroup))
                .withDescription("Fake Bid Trigger")
                .startAt(finishDate)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();
    }


    private JobDetail buildFakeBidJobDetail(Auction auction) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("auction", auction);
        return JobBuilder.newJob(FakeBidJob.class)
                .withIdentity(JobKey.jobKey(String.valueOf(auction.getId()), fakeBidJobGroup))
                .withDescription("Fake bid Job")
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();
    }

    private void validation(AddAuctionDomain auctionDomain) {
        if (auctionDomain.getTitle() == null || auctionDomain.getTitle().length() < 1)
            throw new MessageException(Message.TITLE_NULL);
        if (auctionDomain.getTitle().length() > 50)
            throw new MessageException(Message.TITLE_TOO_LONG);
        if (auctionDomain.getDescription() != null && auctionDomain.getDescription().length() > 1000)
            throw new MessageException(Message.DESCRIPTION_TOO_LONG);
        if (auctionDomain.getDate() < 1)
            throw new MessageException(Message.DATE_NULL);
        if (auctionDomain.getDate() - new Date().getTime() < 120000L)
            throw new MessageException(Message.DATE_INVALID);
        if (numberHandler.createNumberLong(auctionDomain.getBasePrice()) < 0)
            throw new MessageException(Message.BASE_PRICE_NULL);
        if (auctionDomain.getMaxNumber() < 2)
            throw new MessageException(Message.MAX_NUMBER_TOO_LOW);
        if (auctionDomain.getMaxNumber() > 15)
            throw new MessageException(Message.MAX_NUMBER_TOO_HIGH);
    }

    private Auction toAuction(AddAuctionDomain auctionDomain, User user) {
        Date date = new Date(auctionDomain.getDate());
        Category category = categoryRepository.findById(auctionDomain.getCategoryId()).orElseThrow(() -> new MessageException(Message.CATEGORY_INVALID));
        long basePrice = numberHandler.createNumberLong(auctionDomain.getBasePrice());
        return new Auction(auctionDomain.getTitle(), auctionDomain.getDescription(), basePrice, category, date, user, auctionDomain.getMaxNumber());
    }

    public Auction findAuctionById(int id) {
        return auctionRepository.findById(id).orElseThrow(() -> new MessageException(Message.AUCTION_NOT_FOUND));
    }

    public AuctionDomain toAuctionDomain(Auction auction, User user, int current) {
        AuctionDomain auctionDomain = new AuctionDomain(auction.getTitle(),
                auction.getDate().getTime(),
                auction.getCategory().getId(),
                auction.getMaxNumber(),
                auction.getId(),
                current,
                auction.getState());
        if (auction.getOwner().getId().equals(user.getId()))
            auctionDomain.setMine(true);
        String userEmail = user.getEmail();
        user = userRepository.findByEmail(userEmail).get();
        if (user.getBookmarks().contains(auction)) {
            auctionDomain.setBookmark(true);
        }
        List<String> auctionPictures = Lists.newArrayList(pictureRepository.findAll()).stream().filter(picture ->
                picture.getFileName().contains("/" + auction.getId() + "/")).map(
                picture -> "http://" + ip + picture.getFileName()
        ).collect(Collectors.toList());
        auctionDomain.setPictures(auctionPictures);
        return auctionDomain;
    }

    public List<Category> getCategory() {
        return Lists.newArrayList(categoryRepository.findAll());
    }

    public List<Auction> getAll() {
        return new ArrayList<>(Lists.newArrayList(auctionRepository.findAll()));
    }

    public Auction findById(int id) {
        return auctionRepository.findById(id).orElseThrow(() -> new MessageException(Message.AUCTION_NOT_FOUND));
    }


    public Page<Auction> findByTitle(String title, int categoryId, boolean hottest, int page, int size) {
        Page<Auction> auctions;
        PageRequest pageRequest = new PageRequest(page, size);

        if (hottest) {
            if (categoryId == 0)
                auctions = auctionRepository.findHottest(title, pageRequest);
            else {
                auctions = auctionRepository.findHottestByCategoryId(categoryId, title, pageRequest);
            }
        } else {
            if (categoryId == 0) {
                auctions = auctionRepository.findByStateNotAndTitleContainingOrderByIdDesc(1, title, pageRequest);
            } else {
                auctions = auctionRepository.findByStateNotAndCategory_idAndTitleContainingOrderByIdDesc(1, categoryId, title, pageRequest);
            }
        }
        return auctions;
    }

    public Page<Auction> findByOwner(User user, int page, int size) {
        return auctionRepository.findByOwner_idOrderByIdDesc(user.getId(), new PageRequest(page, size));
    }

    public Page<AuctionDomain> toPage(List<AuctionDomain> list, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        int start = (int) pageable.getOffset();
        int end = (start + pageable.getPageSize()) > list.size() ? list.size() : (start + pageable.getPageSize());
        Page<AuctionDomain> pages = new PageImpl(list.subList(start, end), pageable, list.size());
        return pages;
    }


    @Transactional
    public void addBookmark(User user, Auction auction) {
        Set<Auction> bookmarks = user.getBookmarks();
        if (bookmarks.contains(auction)) {
            bookmarks.remove(auction);
            unscheduleNotifying(user, auction);
        } else {
            bookmarks.add(auction);
            scheduleNotifying(user, auction);
        }
        userRepository.save(user);
    }

    private void unscheduleNotifying(User user, Auction bookmarkedAuction) {
        logger.warn(user.getEmail() + " is here to remove auction");
        try {
            if (scheduler.checkExists(JobKey.jobKey((bookmarkedAuction.getId() + "/" + user.getId()), notifyBookmarkedAuctionJobGroup))) {
                if (scheduler.checkExists(TriggerKey.triggerKey(bookmarkedAuction.getId() + "/" + user.getId(), notifyBookmarkedAuctionTriggerGroup)))
                    scheduler.unscheduleJob(TriggerKey.triggerKey(bookmarkedAuction.getId() + "/" + user.getId(), notifyBookmarkedAuctionTriggerGroup));
                scheduler.deleteJob(JobKey.jobKey(bookmarkedAuction.getId() + "/" + user.getId(), notifyBookmarkedAuctionJobGroup));
                logger.info("auction Id#" + bookmarkedAuction.getId() + " won't be notified to user Id#" + user.getId() + " anymore. ");
            }
        } catch (SchedulerException e) {
            logger.error("Error unscheduling notification : " + e.getMessage());
            throw new MessageException(Message.SCHEDULER_ERROR);
        }
    }


    private void scheduleNotifying(User user, Auction bookmarkedAuction) {
        int auctionId = bookmarkedAuction.getId();
        int userId = user.getId();
        try {
            Date finishDate = new Date(bookmarkedAuction.getDate().getTime() - REMAINING_TIME_TO_NOTIFY);
            if (finishDate.after(new Date())) {
                JobDetail jobDetail = buildNotifyJobDetail(user, bookmarkedAuction);
                Trigger trigger = buildNotifyJobTrigger(jobDetail, finishDate, userId, auctionId);
                scheduler.scheduleJob(jobDetail, trigger);
                logger.info("auction Id#" + auctionId + " will be notified to user Id#" + userId + " @ " + finishDate);
            }
        } catch (SchedulerException e) {
            logger.error("Error scheduling notification : " + e.getMessage());
            throw new MessageException(Message.SCHEDULER_ERROR);
        }

    }

    private void scheduleFakeBidOn(Auction addedAuction) {
        int auctionId = addedAuction.getId();
        try {
            Date finishDate = addedAuction.getDate();
            if (addedAuction.getState() != 1) {
                JobDetail jobDetail = buildFakeBidJobDetail(addedAuction);
                Trigger trigger = buildFakeBidJobTrigger(jobDetail, finishDate, auctionId);
                scheduler.scheduleJob(jobDetail, trigger);
                logger.info("It will bid on auction Id#" + auctionId + " @ " + finishDate);
            }
        } catch (SchedulerException e) {
            logger.error("Error scheduling fake biding : " + e.getMessage());
            throw new MessageException(Message.SCHEDULER_ERROR);
        }
    }

    @PostConstruct
    public void initialReschedule() {
        Iterable<User> users = userRepository.findAll();
        Iterable<Auction> auctions = auctionRepository.findAll();
        for (User user : users) {
            for (Auction auction : user.getBookmarks()) {
                scheduleNotifying(user, auction);
            }
        }
        for (Auction auction : auctions) {
            scheduleFakeBidOn(auction);
        }
        logger.info("Rescheduled jobs successfully.");

    }


    private Trigger buildNotifyJobTrigger(JobDetail jobDetail, Date finishDate, int userId, int auctionId) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(TriggerKey.triggerKey(auctionId + "/" + userId, notifyBookmarkedAuctionTriggerGroup))
                .withDescription("Notify Auction Trigger")
                .startAt(finishDate)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();
    }

    private JobDetail buildNotifyJobDetail(User user, Auction bookmarkedAuction) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("auction", bookmarkedAuction);
        jobDataMap.put("user", user);
        return JobBuilder.newJob(NotifyBookmarkedAuctionJob.class)
                .withIdentity(JobKey.jobKey((bookmarkedAuction.getId() + "/" + user.getId()), notifyBookmarkedAuctionJobGroup))
                .withDescription("Notify Auction Job")
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();
    }


    public void scheduleFinalizing(Bid bidRequest) {
        int auctionId = bidRequest.getAuction().getId();
        if (findAuctionById(auctionId).getState() == 1) {
            logger.error("cannot scheduleFinalizing, auction Id#" + auctionId + " is already finished.");
            throw new MessageException(Message.FINISHED_AUCTION);
        }
        try {
            if (scheduler.checkExists(TriggerKey.triggerKey(finalizeAuctionTriggerName + auctionId, finalizeAuctionTriggerGroup))
                    || scheduler.checkExists(JobKey.jobKey(String.valueOf(bidRequest.getId()), finalizeAuctionJobGroup))) {
                scheduler.unscheduleJob(TriggerKey.triggerKey(finalizeAuctionTriggerName + auctionId, finalizeAuctionTriggerGroup));
                scheduler.deleteJob(JobKey.jobKey(String.valueOf(bidRequest.getId()), finalizeAuctionJobGroup));
            }
        } catch (SchedulerException e) {
            logger.error("Error scheduling bid : " + e.getMessage());
            throw new MessageException(Message.SCHEDULER_ERROR);
        }
        try {
            Date finishDate = new Date(System.currentTimeMillis() + AUCTION_ACTIVE_SESSION_TIME);
            JobDetail jobDetail = buildFinalizeJobDetail(bidRequest);
            Trigger trigger = buildFinalizeJobTrigger(jobDetail, finishDate, auctionId);
            scheduler.scheduleJob(jobDetail, trigger);
            logger.info("auction Id#" + auctionId + " will be finished @ " + finishDate);
        } catch (SchedulerException e) {
            logger.error("Error scheduling bid : " + e.getMessage());
            throw new MessageException(Message.SCHEDULER_ERROR);
        }
    }

    private JobDetail buildFinalizeJobDetail(Bid bid) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("auction", bid.getAuction());
        jobDataMap.put("bidder", bid.getUser());
        return JobBuilder.newJob(FinalizeAuctionJob.class)
                .withIdentity(JobKey.jobKey(String.valueOf(bid.getId()), finalizeAuctionJobGroup))
                .withDescription("Finalize Auction Job")
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();
    }

    private Trigger buildFinalizeJobTrigger(JobDetail jobDetail, Date startAt, Integer auctionId) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(TriggerKey.triggerKey(finalizeAuctionTriggerName + auctionId, finalizeAuctionTriggerGroup))
                .withDescription("Finalize Auction Trigger")
                .startAt(startAt)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();
    }

}