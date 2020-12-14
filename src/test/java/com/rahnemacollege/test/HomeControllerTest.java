package com.rahnemacollege.test;


import com.rahnemacollege.domain.AuctionDomain;
import com.rahnemacollege.domain.UserDomain;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class HomeControllerTest extends InitTest {

    @Test
    public void search() throws Exception {
        UserDomain userDomain = getFirstUserInfo();
        String response = mvc.perform(MockMvcRequestBuilders.post(SEARCH + "0" + "?page=" + 0 + "&size=" + 40)
                .header("auth", auth).contentType(MediaType.APPLICATION_JSON)
                .param("title", "")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        PagedResources<Resource<AuctionDomain>> auctionDomainResource = gson.fromJson(response, PagedResources.class);
        System.out.println(response);
        //TODO
    }
}

