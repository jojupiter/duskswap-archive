package com.dusk.duskswap.commons.mailing.services;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.*;
import com.dusk.duskswap.commons.mailing.models.Email;
import com.dusk.duskswap.commons.miscellaneous.DefaultProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private AmazonSimpleEmailService client;
    @Autowired
    private TemplateEngine templateEngine;
    private Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Override
    public void sendEmail(Email email, String templateName) {

        SendEmailRequest request = new SendEmailRequest().withSource(email.getFrom());
        Destination destination = new Destination().withToAddresses(email.getTo());
        request.setDestination(destination);

        Content subjContent = new Content().withData(email.getSubject());
        Message msg = new Message().withSubject(subjContent);
        Content textContent = new Content().withData(email.getMessage());
        Body body = new Body().withText(textContent);
        msg.setBody(body);
        // setting html template
        Context context = new Context();
        context.setVariable("email", email);
        String htmlMessage = templateEngine.process(templateName, context);
        Content htmlContent = new Content().withData(htmlMessage);
        body.setHtml(htmlContent);

        request.setMessage(msg);

        client.sendEmail(request);
    }

    @Override
    public void sendSignupConfirmationEmail(Email email) {
        email.setSubject("Confirmation de la création de compte");
        email.setMessageTitle("Confirmez votre nouveau compte");
        email.setMessageSubTitle("Votre code de confirmation est:");
        email.setFrom(DefaultProperties.EMAIL_NO_REPLY_ADDRESS);
        email.setMessageFooter("Pour activer votre compte vous devez confirmer votre email, pour cela veuillez copier et coller ce code  dans le champ requis du formulaire d'inscription.");

        sendEmail(email, "template_mail");
    }

    @Override
    public void sendSigninConfirmationEmail(Email email) {
        email.setSubject("Confirmation de la connexion");
        email.setMessageTitle("Confirmez la connexion");
        email.setMessageSubTitle("Votre code de confirmation est:");
        email.setFrom(DefaultProperties.EMAIL_NO_REPLY_ADDRESS);
        email.setMessageFooter("Pour vous connectez à votre compte , veuillez copier et collez ce code  dans le champ requis du formulaire de connexion.");

        sendEmail(email, "template_mail");
    }

    @Override
    public void sendWithdrawalEmail(Email email) {
        email.setSubject("Confirmation de la vente/retrait");
        email.setMessageTitle("Confirmez la vente/retrait");
        email.setMessageSubTitle("Votre code de vente/retrait est:");
        email.setFrom(DefaultProperties.EMAIL_NO_REPLY_ADDRESS);
        email.setMessageFooter("Pour confirmer la vente/retrait , veuillez copier et collez ce code  dans le champ requis du formulaire de connexion.");

        sendEmail(email, "template_mail");
    }

    @Override
    public void sendTransferEmail(Email email) {
        email.setSubject("Confirmation du transfert");
        email.setMessageTitle("Confirmez la tranfert");
        email.setMessageSubTitle("Votre code de transfert est:");
        email.setFrom(DefaultProperties.EMAIL_NO_REPLY_ADDRESS);
        email.setMessageFooter("Pour confirmer le transfert , veuillez copier et collez ce code  dans le champ requis du formulaire de connexion.");

        sendEmail(email, "template_mail");
    }

    @Override
    public void sendForgotPasswordEmail(Email email) {
        email.setSubject("Réinitialisation du mot de passe");
        email.setMessageTitle("Code de réinitialisation");
        email.setMessageSubTitle("Votre code est:");
        email.setFrom(DefaultProperties.EMAIL_NO_REPLY_ADDRESS);
        email.setMessageFooter("Pour réinitialiser votre mot de passe , veuillez copier et collez ce code dans le champ requis du formulaire de réinitialisation.");

        sendEmail(email, "template_mail");
    }

    @Override
    public void sendDepositEmail(Email email) {
        email.setSubject("Dépôt effectué");
        email.setMessageTitle("");
        email.setMessage("Vous venez d'effectuer un dépôt de : <span style=\"color:#067a33\"> " + email.getMessage() + "</span> " +
                "Veuillez patientez jusqu'à la confirmation du réseau.");
        email.setMessageSubTitle("");
        email.setFrom(DefaultProperties.EMAIL_NO_REPLY_ADDRESS);
        email.setMessageFooter("");

        sendEmail(email, "template_email_deposit");
    }

}
