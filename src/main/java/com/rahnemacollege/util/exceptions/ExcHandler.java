package com.rahnemacollege.util.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletResponse;

@ControllerAdvice
public class ExcHandler extends ResponseEntityExceptionHandler {


    private Logger logger = LoggerFactory.getLogger(ExcHandler.class);

    @ExceptionHandler(value = {MessageException.class})
    protected void inputInvalid(MessageException ex,
                                HttpServletResponse response) {
        switch (ex.getMessageStatus()) {
            case TITLE_NULL:
                response.setStatus(430);
                break;
            case TITLE_TOO_LONG:
                response.setStatus(451);
                break;
            case DESCRIPTION_TOO_LONG:
                response.setStatus(452);
                break;
            case BASE_PRICE_NULL:
                response.setStatus(432);
                break;
            case CATEGORY_NULL:
                response.setStatus(433);
                break;
            case MAX_NUMBER_TOO_LOW:
                response.setStatus(434);
                break;
            case MAX_NUMBER_TOO_HIGH:
                response.setStatus(435);
                break;
            case CATEGORY_INVALID:
                response.setStatus(436);
                break;
            case DATE_INVALID:
                response.setStatus(437);
                break;
            case DATE_NULL:
                response.setStatus(438);
                break;
            case MAX_SIZE_EXCEEDED:
                response.setStatus(453);
                break;

            case EMAIL_NULL:
                response.setStatus(439);
                break;
            case NAME_NULL:
                response.setStatus(440);
                break;
            case PASSWORD_TOO_LOW:
                response.setStatus(441);
                break;
            case PASSWORD_TOO_HIGH:
                response.setStatus(442);
                break;
            case EMAIL_INVALID:
                response.setStatus(443);
                break;
            case EMAIL_DUPLICATED:
                response.setStatus(454);
                break;

            case PASSWORD_INCORRECT:
                response.setStatus(445);
                break;

            case EMAIL_NOT_FOUND:
                response.setStatus(446);
                break;
            case INVALID_RESET_LINK:
                response.setStatus(448);
                break;
            case TOKEN_NOT_FOUND:
                response.setStatus(449);
                break;
            case NOT_RECORDED_REQUEST:
                response.setStatus(450);
                break;

            case INVALID_ID:
                response.setStatus(457);
                break;
            case AUCTION_NOT_FOUND:
                response.setStatus(458);
                break;
            case REALLY_BAD_SITUATION:
                response.setStatus(456);
                break;
            case FORBIDDEN_REQUEST:
                response.setStatus(499);
                break;
            case PICTURE_NULL:
                response.setStatus(498);
                break;
            case FINISHED_AUCTION:
                response.setStatus(601);
                break;
            case SCHEDULER_ERROR:
                response.setStatus(602);
                break;
            case ALREADY_BID:
                response.setStatus(603);
                break;
            case USER_IS_USING_ANOTHER_DEVICE:
                response.setStatus(605);
            case USER_NOT_FOUND:
                response.setStatus(606);
        }
        logger.error(ex.getMessageStatus().toString());
    }

    @ExceptionHandler(value = {IllegalStateException.class})

    protected void fileUpload(IllegalStateException ex,
                              HttpServletResponse response) {
        if (ex.getMessage().contains("SizeLimitExceededException"))
            response.setStatus(455);
        logger.error(ex.getMessage());
    }

    @ExceptionHandler(value = {IllegalArgumentException.class})
    private void tryToGetMorePage(IllegalArgumentException ex,
                                  HttpServletResponse response){
        if(ex.getMessage().contains("> toIndex")){
            logger.error(ex.getMessage());
            response.setStatus(604);
        }
    }

    @ExceptionHandler(value = {MessageDeliveryException.class})
    private void userWithAnotherDevice(MessageDeliveryException ex,
                                       HttpServletResponse response){
        System.err.println("dadash boro biron");
        response.setStatus(607);
    }
}
