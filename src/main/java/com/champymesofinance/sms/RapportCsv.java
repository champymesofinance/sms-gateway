package com.champymesofinance.sms;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Écrit un rapport CSV des envois de SMS.
 * Le fichier est créé s'il n'existe pas, sinon les lignes sont ajoutées à la suite.
 * Format : Nom;Téléphone;Statut;CodeHTTP;MessageErreur;DateHeure
 */
public class RapportCsv implements AutoCloseable {

    private static final DateTimeFormatter FORMAT_DATE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final PrintWriter writer;
    private final String cheminFichier;

    public RapportCsv(String cheminFichier) throws IOException {
        this.cheminFichier = cheminFichier;

        boolean fichierExiste = new java.io.File(cheminFichier).exists();

        // FileWriter avec append=true → on ajoute à la suite si le fichier existe
        this.writer = new PrintWriter(new FileWriter(cheminFichier, true));

        // Écrire l'en-tête uniquement si le fichier vient d'être créé
        if (!fichierExiste) {
            writer.println("Nom;Telephone;Statut;DateHeure;Message");
            writer.flush();
        }
    }

    /**
     * Enregistre une ligne de succès.
     */
    public synchronized void enregistrerSucces(Client client) {
        ecrireLigne(client.getNom(), client.getTelephone(),
                "OK", "SMS envoyé avec succès");
    }

    /**
     * Enregistre une ligne d'échec avec le détail de l'erreur.
     */
    public synchronized void enregistrerEchec(Client client, String messageErreur) {
        ecrireLigne(client.getNom(), client.getTelephone(),
                "ECHEC", nettoyer(messageErreur));
    }

    private void ecrireLigne(String nom, String telephone, String statut, String message) {
        String ligne = String.join(";",
                echapper(nom),
                echapper(telephone),
                statut,
                LocalDateTime.now().format(FORMAT_DATE),
                echapper(message));
        writer.println(ligne);
        writer.flush(); // flush immédiat → si le programme plante, on garde la trace
    }

    /**
     * Échappe les ';' et les guillemets pour un CSV lisible par Excel.
     * Si la valeur contient ';' ou '"', on l'entoure de guillemets et on double les guillemets internes.
     */
    private String echapper(String valeur) {
        if (valeur == null) return "";
        if (valeur.contains(";") || valeur.contains("\"") || valeur.contains("\n")) {
            return "\"" + valeur.replace("\"", "\"\"") + "\"";
        }
        return valeur;
    }

    /**
     * Nettoie le message d'erreur pour éviter les retours à la ligne dans une cellule CSV.
     */
    private String nettoyer(String message) {
        if (message == null) return "";
        return message.replace("\r", " ").replace("\n", " ").trim();
    }

    public String getCheminFichier() {
        return cheminFichier;
    }

    @Override
    public void close() {
        if (writer != null) {
            writer.flush();
            writer.close();
        }
    }
}