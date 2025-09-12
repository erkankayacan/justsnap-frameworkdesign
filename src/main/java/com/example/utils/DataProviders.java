package com.example.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.annotations.DataProvider;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DataProviders {

    @DataProvider(name = "loginCsv")
    public static Object[][] loginCsv() throws IOException {
        List<Object[]> data = new ArrayList<>();
        try (FileReader reader = new FileReader("src/test/resources/data/login.csv")) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);
            for (CSVRecord r: records) {
                data.add(new Object[]{ r.get("username"), r.get("password"), r.get("valid").equalsIgnoreCase("true") });
            }
        }
        return data.toArray(new Object[0][0]);
    }

    @DataProvider(name = "loginExcel")
    public static Object[][] loginExcel() throws IOException {
        List<Object[]> list = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream("src/test/resources/data/login.xlsx");
             XSSFWorkbook wb = new XSSFWorkbook(fis)) {
            Sheet s = wb.getSheetAt(0);
            Iterator<Row> it = s.iterator();
            it.next(); // header
            while (it.hasNext()) {
                Row r = it.next();
                list.add(new Object[]{
                        r.getCell(0).getStringCellValue(),
                        r.getCell(1).getStringCellValue(),
                        r.getCell(2).getBooleanCellValue()
                });
            }
        }
        return list.toArray(new Object[0][0]);
    }
}
