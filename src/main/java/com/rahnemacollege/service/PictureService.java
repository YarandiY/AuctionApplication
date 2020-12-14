package com.rahnemacollege.service;

import com.google.common.collect.Lists;
import com.rahnemacollege.domain.SimpleUserDomain;
import com.rahnemacollege.model.Auction;
import com.rahnemacollege.model.Picture;
import com.rahnemacollege.model.User;
import com.rahnemacollege.repository.PictureRepository;
import com.rahnemacollege.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


@Service
public class PictureService {

    @Autowired
    private PictureRepository pictureRepository;
    @Autowired
    private UserRepository userRepository;
    private final Logger logger = LoggerFactory.getLogger(PictureService.class);
    private final String IMAGE_PATH = "./usr/local/share";


    public List<Picture> getAll() {
        return Lists.newArrayList(pictureRepository.findAll());
    }

    public void save(MultipartFile pic, String path) throws IOException {
        logger.info("start to try save picture with path: " + path);
        File upl = new File(path);
        upl.createNewFile();
        FileOutputStream fout = new FileOutputStream(upl);
        BufferedImage bufferedImage = ImageIO.read(pic.getInputStream());
        Iterator<ImageWriter> imageWriters = ImageIO.getImageWritersByFormatName("jpg");
        if (!imageWriters.hasNext()) {
            throw new IllegalStateException("Writers Not Found!!");
        }
        ImageWriter imageWriter = imageWriters.next();
        ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(fout);
        imageWriter.setOutput(imageOutputStream);
        ImageWriteParam imageWriteParam = imageWriter.getDefaultWriteParam();
        imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        if(pic.getSize() > 3000000){
            imageWriteParam.setCompressionQuality(0.5F);
        }else{
            imageWriteParam.setCompressionQuality(1.0F);
        }
        imageWriter.write(null, new IIOImage(bufferedImage, null, null), imageWriteParam);
        fout.close();
        imageOutputStream.close();
        imageWriter.dispose();
        logger.info("picture saved");
    }


    public void setAuctionPictures(Auction auction, MultipartFile[] images) {
        String path = IMAGE_PATH + "/images/auction_images/" + auction.getId() + "/";
        new File(path).mkdirs();
        logger.info("directory created");
        for (MultipartFile image :
                images) {
            String fileName = "/images/auction_images/" + auction.getId() + "/" + new Date().getTime() + ".jpg";
            String pathName = IMAGE_PATH + fileName;
            try {
                saveAuctionPicture(image, pathName, auction, fileName);
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
    }


    public void saveAuctionPicture(MultipartFile pic, String path, Auction auction, String file_name) throws IOException {
        Picture picture = new Picture(file_name, auction);
        pictureRepository.save(picture);
        logger.info("added picture to repository");
        save(pic, path);
    }

    public SimpleUserDomain setProfilePicture(User user, MultipartFile picture) {
        int userId = user.getId();
        String path = IMAGE_PATH + "/images/profile_images/" + userId + "/";
        new File(path).mkdirs();
        String fileName = "/images/profile_images/" + userId + "/" + new Date().getTime() + ".jpg";
        String pathName = IMAGE_PATH + fileName;
        try {
            save(picture, pathName);
            user.setPicture(fileName);
            userRepository.save(user);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return new SimpleUserDomain(user.getName(), user.getEmail());

    }
}
