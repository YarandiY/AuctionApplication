package com.rahnemacollege.util;

import com.rahnemacollege.util.exceptions.Message;
import com.rahnemacollege.util.exceptions.MessageException;

import java.util.HashMap;
import java.util.Map;

public class NumberHandler {


    private final Map<String, Integer> PERSIAN_NUMBER_MAP = new HashMap<>();
    private final String PERSIAN_NUMBER = "۰۱۲۳۴۵۶۷۸۹";

    public NumberHandler(){
        for (int i = 0; i < 10; i++) {
            PERSIAN_NUMBER_MAP.put(PERSIAN_NUMBER.substring(i,i + 1), i);
        }
    }

    public long createNumberLong(String number){
        if(!number.matches("\\d+")){
                String englishNumber = "";
                for (int i = 0; i < number.length(); i++) {
                    englishNumber += PERSIAN_NUMBER_MAP.get(number.substring(i,i + 1));
                }
                System.err.println(englishNumber);
                number = englishNumber;
                if(number.contains("null")){
                    throw new MessageException(Message.BASE_PRICE_NULL);
                }
        }
        return Long.valueOf(number);
    }


}
