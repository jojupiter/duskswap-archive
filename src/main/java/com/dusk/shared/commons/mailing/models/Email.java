package com.dusk.shared.commons.mailing.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Email {

    public static String FROM_NAME = "Duskpay";
    @NotBlank
    private String from;
    @NotBlank
    private List<String> to;
    private String subject;
    private String messageTitle;
    private String messageSubTitle;
    private String message;
    private String messageFooter;
    private String pathToAttachement;

}
