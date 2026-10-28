package com.champymesofinance.sms;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelReader {

    public static List<Client> lireClients(String cheminFichier) throws IOException {
        List<Client> clients = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(cheminFichier);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Cell cellNom = row.getCell(0);
                Cell cellTel = row.getCell(1);
                if (cellNom == null || cellTel == null) continue;

                String nom = formatter.formatCellValue(cellNom).trim();
                String tel = formatter.formatCellValue(cellTel).trim();

                if (nom.isEmpty() || tel.isEmpty()) continue;
                clients.add(new Client(nom, tel));
            }
        }
        return clients;
    }
}