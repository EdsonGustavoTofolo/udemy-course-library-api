package br.com.edsontofolo.libraryapi.service;

import java.util.List;

public interface EmailService {
    void sendMails(String message, List<String> emails);
}
