package com.dusk.duskswap.commons.miscellaneous;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FieldValidation {

    public HashMap<String, String> isPasswordValid(String password) {

        HashMap<String, String> validity = new HashMap<>();
        boolean isPassValid = true;
        String message = "";
        Pattern pattern = null;
        Matcher matcher = null;

        // Does password contain at least an uppercase character?
        pattern = Pattern.compile("[A-Z]+");
        matcher = pattern.matcher(password);
        if(!matcher.find()) {
            isPassValid = false;
            message += "The password should contain at least an uppercase character.\n";
        }

        // Does password contain at least a digit?
        pattern = Pattern.compile("\\d+");
        matcher = pattern.matcher(password);
        if(!matcher.find()) {
            isPassValid = false;
            message += "The password should contain at least a digit.\n";
        }

        // Does password contain a special character?
        pattern = Pattern.compile("[@#$%^&+=!%;,:$£\\-\\*µ]");
        matcher = pattern.matcher(password);
        if(!matcher.find()) {
            isPassValid = false;
            message += "The password should contain at least a special character(@#$%^&+=!%;,:$£-**µ).\n";
        }

        // Does password is minimum 8 characters of length?
        if(password.length() < 8){
            isPassValid = false;
            message += "The password should contain at least 8 characters.\n";
        }

        validity.put("isPasswordValid", String.valueOf(isPassValid));
        validity.put("message", message);

        return validity;
    }


    public HashMap<String, String> isEmailValid(String email) {

        HashMap<String, String> validity = new HashMap<>();
        boolean isEmailValid = true;
        String message = "";
        Pattern pattern = null;
        Matcher matcher = null;

        // Does email is empty?
        if(email.isEmpty() || email == null) {
            isEmailValid = false;
            message += "Email cannot be empty. \n";
        }

        // Does email has a regular pattern?
        pattern = Pattern.compile("^(.+)@(.+)$");
        matcher = pattern.matcher(email);
        if(!matcher.find()) {
            isEmailValid = false;
            message += "The email not to valid. \n";
        }

        validity.put("isEmailValid", String.valueOf(isEmailValid));
        validity.put("message", message);

        return validity;

    }

}
