package com.champymesofinance.sms;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        // === Configuration ===
        String cheminExcel   = "clients.xlsx";
        String clientId      = "VkcpKtBCK9V0NIKSgNzupnCIWgfsN2SW";
        String clientSecret  = "krLxqljUwUm7R4hu9SMPs5h5ylKKXEQlkT8fZAcSqTCF";
        String senderAddress = "+2260000";
        String senderName    = "CHAMPY MESO";

        // Nom du rapport avec horodatage (ex: rapport_sms_2026-10-08_21-45-00.csv)
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String cheminRapport = "rapport_sms_" + timestamp + ".csv";

        try (RapportCsv rapport = new RapportCsv(cheminRapport)) {

            List<Client> clients = ExcelReader.lireClients(cheminExcel);
            System.out.println(clients.size() + " clients trouvés.");

            OrangeAuthManager authManager = new OrangeAuthManager(clientId, clientSecret);
            OrangeSmsService smsService =
                    new OrangeSmsService(authManager, senderAddress, senderName);

            int nbOk = 0;
            int nbEchecs = 0;

            for (Client client : clients) {
                String message = "Bonjour " + client.getNom()
                        + ", CHAMPY MESO FINANCE vous informe que votre agence de Zogona est désormais située à la Zone du Bois vers la clinique Frany. Pour plus d’infos :70610615/50379090.";
                try {
                    ResultatEnvoi resultat = smsService.envoyerSms(client.getTelephone(), message);

                    if (resultat.isSucces()) {
                        rapport.enregistrerSucces(client);
                        nbOk++;
                    } else {
                        rapport.enregistrerEchec(client, resultat.getDescription());
                        nbEchecs++;
                    }
                } catch (Exception e) {
                    String erreur = e.getClass().getSimpleName() + " : " + e.getMessage();
                    System.err.println("Échec pour " + client.getNom() + " : " + erreur);
                    rapport.enregistrerEchec(client, erreur);
                    nbEchecs++;
                }
                Thread.sleep(200);
            }

            System.out.println();
            System.out.println("=== Récapitulatif ===");
            System.out.println("Total   : " + clients.size());
            System.out.println("Réussis : " + nbOk);
            System.out.println("Échecs  : " + nbEchecs);
            System.out.println("Rapport : " + rapport.getCheminFichier());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}