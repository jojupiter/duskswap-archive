package com.dusk.duskswap.commons.mailing.services;


import com.dusk.duskswap.commons.mailing.models.Email;

public interface EmailService {
    void sendEmail(Email email);
    void sendSignupConfirmationEmail(Email email);
    void sendSigninConfirmationEmail(Email email);
    void sendWithdrawalEmail(Email email);
    void sendTransferEmail(Email email);
    void sendForgotPasswordEmail(Email email);
}
