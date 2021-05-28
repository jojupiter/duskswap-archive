package com.dusk.shared.commons.mailing.services;


import com.dusk.shared.commons.mailing.models.Email;

public interface EmailService {
    void sendEmail(Email email);
    void sendSignupConfirmationEmail(Email email);
    void sendSigninConfirmationEmail(Email email);
}
