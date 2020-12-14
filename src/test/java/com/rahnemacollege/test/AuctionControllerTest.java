package com.rahnemacollege.test;


import com.rahnemacollege.domain.AddAuctionDomain;
import com.rahnemacollege.domain.AuctionDetail;
import com.rahnemacollege.domain.AuctionDomain;
import com.rahnemacollege.model.Auction;
import com.rahnemacollege.model.Category;
import com.rahnemacollege.repository.AuctionRepository;
import com.rahnemacollege.repository.LoginInfoRepository;
import com.rahnemacollege.repository.PictureRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.fail;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AuctionControllerTest extends InitTest {


    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private PictureRepository pictureRepository;

    @Test
    public void categories() throws Exception {
        getCategory();
    }

    public int getCategory() throws Exception{
        String response = mvc.perform(MockMvcRequestBuilders.get(CATEGORY).header("auth", auth)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        List<Category> categories = new ArrayList<>(Arrays.asList(gson.fromJson(response, Category[].class)));
        System.out.println("Categories : ");
        for (Category c :
                categories) {
            System.out.println(c.getCategoryName());
        }
        return categories.get(0).getId();
    }

    @Test
    public void addAuction() throws Exception {
        int categoryId = getCategory();
        System.err.println(categoryId);
        AuctionDomain auctionDomain = addAuction(createAddAuctionRequest("testADDAuction", "", "100", 5, categoryId, 1608883888000L));
        int auctionId = auctionDomain.getId();
        assertThat(auctionDomain.getTitle())
                .isEqualTo("testADDAuction");
        assertThat(auctionDomain.isMine())
                .isEqualTo(true);
        auctionRepository.deleteById(auctionId);

    }

    @Test
    public void invalidAddAuction() throws Exception {
        int categoryId = getCategory();
        String request = createAddAuctionRequest("invalid base price", "", "-1", 5, categoryId, 15660254847150L);
        mvc.perform(MockMvcRequestBuilders.post(ADD)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request).header("auth", auth)
        ).andExpect(status().is(432));
        request = createAddAuctionRequest("invalid max number", "", "100", 1, categoryId, 15660254847150L);
        mvc.perform(MockMvcRequestBuilders.post(ADD)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request).header("auth", auth)
        ).andExpect(status().is(434));

        request = createAddAuctionRequest("", "", "100", 5, categoryId, 15660254847150L);
        mvc.perform(MockMvcRequestBuilders.post(ADD)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request).header("auth", auth)
        ).andExpect(status().is(430));

        request = createAddAuctionRequest("description too long", LONG_STRING, "100", 5, categoryId, 15660254847150L);
        mvc.perform(MockMvcRequestBuilders.post(ADD)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request).header("auth", auth)
        ).andExpect(status().is(452));

        request = createAddAuctionRequest(LONG_STRING, "", "100", 5, categoryId, 15660254847150L);
        mvc.perform(MockMvcRequestBuilders.post(ADD)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request).header("auth", auth)
        ).andExpect(status().is(451));

        request = createAddAuctionRequest("max number too high", "", "100", 16, categoryId, 15660254847150L);
        mvc.perform(MockMvcRequestBuilders.post(ADD)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request).header("auth", auth)
        ).andExpect(status().is(435));

        request = createAddAuctionRequest("invalid category id", "", "100", 5, -1, 15660254847150L);
        mvc.perform(MockMvcRequestBuilders.post(ADD)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request).header("auth", auth)
        ).andExpect(status().is(436));

        /*request = createAddAuctionRequest("invalid date", "", 100, 5, 1, 1566025484715L);
        mvc.perform(MockMvcRequestBuilders.post(ADD)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request).header("auth", auth)
        ).andExpect(status().is(437));*/

        /*request = createAddAuctionRequest("invalid date", "", 100, 5, 1, 1566025484715L);
        mvc.perform(MockMvcRequestBuilders.post(ADD)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request).header("auth", auth)
        ).andExpect(status().is(437));*/

    }

    @Test
    public void toggleBookmark() throws Exception {
        int categoryId = getCategory();
        AuctionDomain auctionDomain = addAuction(createAddAuctionRequest("testADDAuction", "", "100", 5, categoryId, 1608883888000L));
        int auctionId = auctionDomain.getId();
        assertThat(auctionDomain.isBookmark())
                .isEqualTo(false);
        mvc.perform(MockMvcRequestBuilders.post(ADD_BOOKMARK + "?auctionId=" + auctionDomain.getId()).header("auth", auth)).andExpect(status().isOk());
        auctionDomain = getAuction(auctionDomain.getId());
        assertThat(auctionDomain.isBookmark())
                .isEqualTo(true);
        mvc.perform(MockMvcRequestBuilders.post(ADD_BOOKMARK + "?auctionId=" + auctionDomain.getId()).header("auth", auth)).andExpect(status().isOk());
        auctionDomain = getAuction(auctionDomain.getId());
        assertThat(auctionDomain.isBookmark())
                .isEqualTo(false);
        auctionRepository.deleteById(auctionId);
    }

    @Test
    public void addPicture() throws Exception{
        int categoryId = getCategory();
        AuctionDomain auctionDomain = addAuction(createAddAuctionRequest("testADDAuction", "", "100", 5, categoryId , 1608883888000L));
        int auctionId = auctionDomain.getId();
        System.err.println(auctionDomain.getId());
        FileInputStream fis = new FileInputStream(Image_PATH);
        MockMultipartFile multipartFile = new MockMultipartFile("file", fis);
        MockMultipartFile profilePicture = new MockMultipartFile("images", "Beautiful_Fantasy_Worlds_Wallpapers_31.jpg", "multipart/form-data", multipartFile.getBytes());
        mvc.perform(MockMvcRequestBuilders.multipart(ADD_PICTURE + auctionId)
                .file(profilePicture)
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                .header("auth", auth))
                .andExpect(status().isOk());
        pictureRepository.deleteByAuction_id(auctionId);
        auctionRepository.deleteById(auctionId);
    }

    @Test
    public void invalidToggleBookmark() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post(ADD_BOOKMARK + "?auctionId=" + -1)
                .header("auth", auth)).andExpect(status().is(458));
    }

    @Test
    public void findAuction() throws Exception{
        int categoryId = getCategory();
        AuctionDomain auctionDomain = addAuction(createAddAuctionRequest("testADDAuction", "", "100", 5, categoryId, 1608883888000L));
        int auctionId = auctionDomain.getId();
        String response = mvc.perform(MockMvcRequestBuilders.get(FIND + auctionId)
                .header("auth",auth)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        AuctionDetail auctionDetail = gson.fromJson(response, AuctionDetail.class);
        assertThat(auctionDetail.getId())
                .isEqualTo(auctionId);
        auctionRepository.deleteById(auctionId);

    }

    private AuctionDomain getAuction(int id) throws Exception {
        String response = mvc.perform(MockMvcRequestBuilders.get(FIND + id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("auth", auth)
        ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return gson.fromJson(response, AuctionDomain.class);
    }

    private String createAddAuctionRequest(String title, String description, String base_price, int max_number, int category_id, long date) throws Exception {
        AddAuctionDomain addAuctionDomain = new AddAuctionDomain();
        addAuctionDomain.setTitle(title);
        addAuctionDomain.setBasePrice(String.valueOf(base_price));
        addAuctionDomain.setCategoryId(category_id);
        addAuctionDomain.setDate(date);
        addAuctionDomain.setMaxNumber(max_number);
        addAuctionDomain.setDescription(description);
        String request = gson.toJson(addAuctionDomain);
        return request;
    }

    private AuctionDomain addAuction(String request) throws Exception{
        String response = mvc.perform(MockMvcRequestBuilders.post(ADD)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request).header("auth", auth)
        ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        AuctionDomain auctionDomain = gson.fromJson(response, AuctionDomain.class);
        return auctionDomain;
    }

}
