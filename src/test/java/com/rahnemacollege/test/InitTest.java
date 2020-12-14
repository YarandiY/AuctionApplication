package com.rahnemacollege.test;


import com.google.gson.Gson;
import com.rahnemacollege.domain.AuthenticationResponse;
import com.rahnemacollege.domain.UserDomain;
import com.rahnemacollege.model.Auction;
import com.rahnemacollege.model.Category;
import com.rahnemacollege.model.User;
import com.rahnemacollege.repository.AuctionRepository;
import com.rahnemacollege.repository.CategoryRepository;
import com.rahnemacollege.repository.LoginInfoRepository;
import com.rahnemacollege.repository.UserRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Ignore
public class InitTest {
    @Autowired
    protected MockMvc mvc;
    protected String auth, auth2;
    protected User user, user2;
    protected Gson gson;
    protected final String EDIT = "/users/edit";
    protected final String ME = "/users/me";
    protected final String LOGIN = "/users/login";
    protected final String EDIT_PICTURE = "/users/edit/picture";
    protected final String EDIT_PASSWORD = "/users/edit/password";
    protected final String FORGOT = "/users/forgot";
    protected final String GET_BOOKMARKS = "/users/bookmarks";
    protected final String RESET = "/users/reset";
    protected final String AUCTIONS = "/users/auctions";
    protected final String CATEGORY = "/auctions/category";
    protected final String ADD = "/auctions/add";
    protected final String ADD_PICTURE = "/auctions/add/picture/";
    protected final String FIND = "/auctions/find/";
    protected final String ADD_BOOKMARK = "/auctions/bookmark";
    protected final String ALL = "/auctions/all";
    protected final String SEARCH = "/home/search/";


    protected final String Image_PATH = "data/Beautiful_Fantasy_Worlds_Wallpapers_31.jpg";

    //length : 1676
    protected final String LONG_STRING = "MySQL is an open source relational database management system (RDBMS) with a wide-range of applications in business infrastructure. The huge amounts transactions processed by any MySQL server on a day to day basis and the importance of maintaining smooth continuity of these transactions for uninterrupted business service delivery makes it essential for business organizations to have a proper MySQL Management system in place. Also, while most MySQL monitor tools generate notifications in case of performance issues, an ideal MySQL monitoring tool will not only alert you but also provides comprehensive insight into the root cause of the issues and helps you troubleshoot them quickly. \n" +
            "\n" +
            "Applications Manager's MySQL Management software helps database administrators in managing and monitoring the performance and availability of their SQL databases. With the help of MySQL Performance Monitor, DB Admins can monitor critical performance parameters of their database and maintain maximum uptime and health. It is one among the best MySQL management tool which provides an intuitive web client that helps you ease your MySQL Management efforts and allows you to visualize, manage and monitor database farms effectively. \n" +
            "\n" +
            "Unlike most database monitoring tools for MySQL which offers only health and availability stats for your database, Applications Manager's MySQL Monitor provides in-depth MySQL performance monitoring with numerous performance metrics and triggers notifications in case of downtimes. Also, the MySQL performance monitor keeps track of usage patterns, offers insights to plan capacity and helps you get notified about impending problems in your database.";

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuctionRepository auctionRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private LoginInfoRepository loginInfoRepository;

    @Before
    public void login() throws Exception {
        initialPackage();
        gson = new Gson();

        user = new User();
        user.setEmail("tmohati@gmail.com");
        user.setPassword("t.mohati");
        String response = mvc.perform(MockMvcRequestBuilders.post(LOGIN)
                .contentType(MediaType.APPLICATION_JSON).content(gson.toJson(user)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        auth = gson.fromJson(response, AuthenticationResponse.class).getToken();
        auth = "Bearer " + auth;
        user.setName(getFirstUserInfo().getName());

        user2 = new User();
        user2.setEmail("yalda.yarandi@gmail.com");
        user2.setPassword("y.yarandi");
        response = mvc.perform(MockMvcRequestBuilders.post(LOGIN)
                .contentType(MediaType.APPLICATION_JSON).content(gson.toJson(user2)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        auth2 = gson.fromJson(response, AuthenticationResponse.class).getToken();
        auth2 = "Bearer " + auth2;
        user2.setName(getFirstUserInfo().getName());
    }

    private void initialPackage() throws ParseException {
        List<Category> categories = new ArrayList<>();
        categories.add(new Category("ورزشی"));
        categories.add(new Category("خانه"));
        categories.add(new Category("دیجیتال"));
        categoryRepository.saveAll(categories);

        User alireza = new User("علیرضا دیزجی", "Alirezadizaji@yahoo.com", "$2a$10$OraZDACEgxzTyF5jK8XTo.ID/bcMEknBWMi3lXwYcCwIpDMfJndaG");
        userRepository.save(alireza);
        User yalda = new User("یلدا یارندی", "yalda.yarandi@gmail.com", "$2a$10$NpanvpOxS9tL3hHSy2JKquiA8iFqwhHCmhRPM9K3a.tzxzjRCXCTK");
        userRepository.save(yalda);
        User omid = new User("امید سیفان", "seyfanomid@ymail.com", "$2a$10$CDcfMF5fHv5LuczO50wxuOvnMvEiCFLmpJN.WPLHs006aWwpGFWbi");
        userRepository.save(omid);
        User tahmine = new User("تهمینه محاطی", "tmohati@gmail.com", "$2a$10$DarBf7KvL.QK4/FResFZ.OvYw5NTV0ieuaqq975DSrOmeh/wScckG");
        userRepository.save(tahmine);
        User sobhan = new User("سبحان ابراهیمی", "sobhanebrahimi82@gmail.com", "$2a$10$JGM95m2hq9ut0QwcltxvOu29BdWJbbGM2Pe8nRJL1QbHs8Q2jZNSq");
        userRepository.save(sobhan);

        List<Auction> auctions = new ArrayList<>();
        auctions.add(new Auction("توپ بسکتبال نابی", "دست یه بسکتبالیست حرفه‌ای بوده!"
                , 400000L,
                categoryRepository.findByCategoryName("ورزشی"),
                new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse("27-05-2020 00:00:00"),
                alireza, 4));
        auctions.add(new Auction("macbook pro نو", "دست یه برنامه نویس حرفه‌ای بوده!", 20000000L,
                categoryRepository.findByCategoryName("دیجیتال"),
                new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse("05-09-2019 12:00:00"),
                omid, 6));
        auctions.add(new Auction("Apple Watch", "تمیز و بدون خط و خش", 1000000, categoryRepository.findByCategoryName("دیجیتال"),
                new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse("04-09-2019 22:00:00")
                , yalda, 5));

        auctions.add(new Auction("یخچال سایدبای ساید SNOWA", "با ۱.۵ سال گارانتی اصلی", 12000000,
                categoryRepository.findByCategoryName("خانه"),
                new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse("04-09-2019 21:30:00"),
                tahmine, 3));

        auctions.add(new Auction("لپتاپ ایسوز FX502VM", "سالم و اصل", 16000000,
                categoryRepository.findByCategoryName("دیجیتال"),
                new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse("04-09-2019 21:40:03"),
                sobhan, 4));

        auctions.add(new Auction("کولر گازی LG", "تورو خدا بخرید یخ کردیم تو رهنماکالج", 7000000,
                categoryRepository.findByCategoryName("خانه"),
                new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse("04-09-2019 18:40:00"),
                sobhan, 5));

        auctions.add(new Auction("صندلی های بدون چرخ رهنماکالج", "پدر کمرامون دراومده", 100000,
                categoryRepository.findByCategoryName("خانه"),
                new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse("05-09-2019 05:00:00"),
                omid, 4));

        auctions.add(new Auction("اجاق گاز رهنماکالج", "اصلا استفاده نشده و سالم و تمیزه", 2000000,
                categoryRepository.findByCategoryName("خانه"),
                new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse("05-09-2019 16:00:00"),
                yalda, 8));

        auctions.add(new Auction("ست لباس ورزشی", "هدیه گرفتم تا حالا نپوشیدم", 200000,
                categoryRepository.findByCategoryName("ورزشی"),
                new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse("06-09-2019 20:30:00"),
                tahmine, 11));

        auctionRepository.saveAll(auctions);

    }

    protected UserDomain getFirstUserInfo() throws Exception {
        String response = mvc.perform(MockMvcRequestBuilders.get(ME).header("auth", auth))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return gson.fromJson(response, UserDomain.class);
    }

    protected UserDomain getSecondUserInfo() throws Exception {
        String response = mvc.perform(MockMvcRequestBuilders.get(ME).header("auth", auth2))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return gson.fromJson(response, UserDomain.class);
    }

    @After
    public void clear(){
        auctionRepository.deleteAll();
        loginInfoRepository.deleteAll();
        userRepository.deleteAll();
        categoryRepository.deleteAll();
        System.err.println("deleted successfully");
    }
}
