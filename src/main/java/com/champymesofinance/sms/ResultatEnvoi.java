package com.champymesofinance.sms;

/**
 * Résultat d'un envoi de SMS.
 */
public class ResultatEnvoi {
    private final boolean succes;
    private final int codeHttp;
    private final String messageErreur;

    private ResultatEnvoi(boolean succes, int codeHttp, String messageErreur) {
        this.succes = succes;
        this.codeHttp = codeHttp;
        this.messageErreur = messageErreur;
    }

    public static ResultatEnvoi ok() {
        return new ResultatEnvoi(true, 200, null);
    }

    public static ResultatEnvoi echec(int codeHttp, String messageErreur) {
        return new ResultatEnvoi(false, codeHttp, messageErreur);
    }

    public boolean isSucces() { return succes; }
    public int getCodeHttp() { return codeHttp; }
    public String getMessageErreur() { return messageErreur; }

    /** Message lisible pour le CSV ou les logs. */
    public String getDescription() {
        if (succes) return "SMS envoyé (HTTP " + codeHttp + ")";
        return "HTTP " + codeHttp + " - " + messageErreur;
    }
}