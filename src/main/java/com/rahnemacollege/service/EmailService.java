package com.rahnemacollege.service;

import com.rahnemacollege.model.Auction;
import com.rahnemacollege.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
@EnableAsync
public class EmailService {
    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendPassRecoveryMail(User user, String appUrl, String token) throws MessagingException {
        String userEmail = user.getEmail();
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setFrom("fivegears.rahnema@gmail.com");
        helper.setTo(userEmail);
        String link = appUrl + "/users/reset?token=" + token;
        String text = "<html>\n" +
                "\n" +
                "<body dir=\"rtl\">\n" +
                "    <div>\n" +
                "        <h1>\n" +
                user.getName() +
                "            عزیز! سلام.\n" +
                "\n" +
                "        </h1>\n" +
                "        <h3>\n" +
                "            جهت بازنشانی رمز عبور خود بر روی لینک زیر کلیک کنید.\n" +
                "        </h3>\n" +
                "        <h2 dir=\"ltr\">\n" +
                link +
                "        </h2>\n" +
                "        <h3>\n" +
                "            در صورتی که شما درخواست بازنشانی نداده اید این ایمیل را نادیده بگیرید.\n" +
                "            <br>\n" +
                "            یاعلی. در پناه حق!\n" +
                "        </h3>\n" +
                "    </div>\n" +
                "\n" +
                "\n" +
                "</body>\n" +
                "\n" +
                "</html>";
        helper.setText(text, true);
        helper.setSubject("بازنشانی رمزعبور AucApp");
        mailSender.send(message);
    }

    @Async
    public void notifyAuctionWinner(Auction wonAuction, Long auctionFinalPrice) throws MessagingException {
        User winnerUser = wonAuction.getWinner();
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setFrom("fivegears.rahnema@gmail.com");
        helper.setTo(winnerUser.getEmail());
        String text = "<html>\n" +
                "\n" +
                "<body dir=\"rtl\">\n" +
                "    <div>\n" +
                "            <h1>\n" +
                winnerUser.getName() +
                "               عزیز، سلام!\n" +
                "\n" +
                "            </h1>\n" +
                "            <h3>\n" +
                "                تبریک! شما برندۀ مزایدۀ\n" +
                "                «\n" +
                wonAuction.getTitle() +
                "                »\n" +
                "                با قیمت نهایی\n" +
                auctionFinalPrice.toString() +
                "                تومان شده\u200Cاید. منتظر دریافت تماس از مالک مزایده باشید.\n" +
                "                <br>\n" +
                "                یاعلی. در پناه حق!\n" +
                "            </h3>\n" +
                "    </div>\n" +
                "\n" +
                "\n" +
                "</body>\n" +
                "\n" +
                "</html>";

        helper.setText(text, true);
        helper.setSubject("شما برندۀ یک مزایده شدید!");
        mailSender.send(message);
    }

    @Async
    public void notifyAuctionOwner(Auction wonAuction, Long auctionFinalPrice) throws MessagingException {
        User winnerUser = wonAuction.getWinner(), ownerUser = wonAuction.getOwner();
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setFrom("fivegears.rahnema@gmail.com");
        helper.setTo(ownerUser.getEmail());
        String text = "<html>\n" +
                "\n" +
                "<body dir=\"rtl\">\n" +
                "    <div>\n" +
                "        <h1>\n" +
                ownerUser.getName() +
                "            عزیز، سلام!\n" +
                "\n" +
                "        </h1>\n" +
                "        <h3>\n" +
                "            کاربر به نشانی\n" +
                winnerUser.getEmail() +
                "            برندۀ مزایدۀ شما با عنوان\n" +
                "            «\n" +
                wonAuction.getTitle() +
                "            »\n" +
                "            و\n" +
                "            با قیمت\n" +
                auctionFinalPrice +
                "            تومان شده است. لطفاً در اسرع وقت با ایشان تماس حاصل فرمایید.\n" +
                "            <br>\n" +
                "            یاعلی. در پناه حق!\n" +
                "        </h3>\n" +
                "    </div>\n" +
                "\n" +
                "\n" +
                "</body>\n" +
                "\n" +
                "</html>";

        helper.setText(text, true);
        helper.setSubject("مزایدۀ آگهی شما به پایان رسید!");
        mailSender.send(message);
    }

    @Async
    public void notifyExpiredAuction(Auction expiredAuction) throws MessagingException {
        User owner = expiredAuction.getOwner();
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setFrom("fivegears.rahnema@gmail.com");
        helper.setTo(owner.getEmail());
        String text = "<html>\n" +
                "\n" +
                "<body dir=\"rtl\">\n" +
                "    <div>\n" +
                "            <h1>\n" +
                owner.getName() +
                "               عزیز، سلام!\n" +
                "\n" +
                "            </h1>\n" +
                "            <h3>\n" +
                "متأسفانه مزایدۀ شما با عنوان \n" +
                "                «\n" +
                expiredAuction.getTitle() +
                "                »\n" +
                " منقضی شده است.\n" +
                "                <br>\n" +
                "                یاعلی. در پناه حق!\n" +
                "            </h3>\n" +
                "    </div>\n" +
                "\n" +
                "\n" +
                "</body>\n" +
                "\n" +
                "</html>";

        helper.setText(text, true);
        helper.setSubject("یک آگهی شما منقضی شده است.");
        mailSender.send(message);
    }
}
