package com.champymesofinance.sms;

public class Client {
    private final String nom;
    private final String telephone;

    public Client(String nom, String telephone) {
        this.nom = nom;
        this.telephone = telephone;
    }

    public String getNom() { return nom; }
    public String getTelephone() { return telephone; }
}