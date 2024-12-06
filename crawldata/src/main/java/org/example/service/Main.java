package org.example.service;

import org.example.Connector.DBLoader;
import org.example.assets.CrawlProcessStatus;
import org.example.assets.LinkConstant;
import org.example.model.CrawlLog;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<FutaTable> futaTables = new ArrayList<>();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("MM-dd-yyyy");

        System.setProperty("webdriver.gecko.driver", "geckodriver-v0.35.0-win32/geckodriver.exe");
        FirefoxOptions options = new FirefoxOptions();
        options.setBinary(LinkConstant.LELONG_FIRE_FOX);
        WebDriver driver = new FirefoxDriver(options);

        var dateCrawlData = DBLoader.getInstance().getDateFailedCrawl();
        try {
            if (dateCrawlData != null) {
                String formattedDate = dateCrawlData.format(myFormatObj);
                var configDatas = DBLoader.getInstance().getConfigData();

                for (var configData : configDatas) {
                    String url = configData.url().replace("DATEFROM", formattedDate);
                    var dataTable = getData(url, driver, formattedDate, dateCrawlData);
                    futaTables.addAll(dataTable);
                }

            } else {
                dateCrawlData = LocalDate.now().plusDays(1);
                String formattedDate = dateCrawlData.format(myFormatObj);
                var configDatas = DBLoader.getInstance().getConfigData();

                for (var configData : configDatas) {
                    String url = configData.url().replace("DATEFROM", formattedDate);
                    var dataTable = getData(url, driver, formattedDate, dateCrawlData);
                    futaTables.addAll(dataTable);
                }
            }
        } catch (InterruptedException e) {
            DBLoader.getInstance().insertLog(new CrawlLog(CrawlProcessStatus.CRAWL_FAILED, e.getMessage(), CrawlProcessStatus.GENERATE_AUTHOR,LocalDate.now(), dateCrawlData));
        } finally {
            driver.quit();
        }

        try{
            CSVExporter.exportToCSV2(futaTables, DBLoader.getInstance().getFilePath());
        } catch (IOException e) {
            DBLoader.getInstance().insertLog(new CrawlLog(CrawlProcessStatus.CRAWL_FAILED, e.getMessage(), CrawlProcessStatus.GENERATE_AUTHOR, LocalDate.now(), dateCrawlData));
        }

        DBLoader.getInstance().insertLog(new CrawlLog(futaTables.size(), CrawlProcessStatus.CRAWL_SUCCESS, CrawlProcessStatus.SUCCESS_MESSAGE, CrawlProcessStatus.GENERATE_AUTHOR, LocalDate.now(), dateCrawlData));
    }

    public static List<FutaTable> getData(String url, WebDriver driver, String formattedDate, LocalDate date) throws InterruptedException {
        List<FutaTable> futaTables = new ArrayList<>();
        driver.get(url);

        Thread.sleep(10000); // You might want to replace this with WebDriverWait

        List<WebElement> busElements = driver.findElements(By.cssSelector(".no-scrollbar > .card-box-shadown")); // Update the selector

        List<WebElement> input = driver.findElements(By.cssSelector(".input-search"));
        String startCity = input.get(0).getText();
        String endCity = input.get(1).getText();

        int i = 0;
        for (WebElement bus : busElements) {
            FutaTable futaTable = new FutaTable();

            futaTable.setStartCity(startCity);
            futaTable.setEndCity(endCity);

            String firstData = bus.findElements(By.cssSelector(".flex .w-full .flex-col")).get(0).getText();
            String[] firstDatas = firstData.split("\n");
            futaTable.setDepartureTime(firstDatas[0]);
            futaTable.setDepartureDate(formattedDate);
            futaTable.setTransitTime(firstDatas[1]);
            futaTable.setArrivalTime(firstDatas[3]);
            futaTable.setArrivalDate(formattedDate);
            futaTable.setStartPoint(firstDatas[4]);
            futaTable.setEndPoint(firstDatas[5]);

            String[] ticketInfors = bus.findElements(By.cssSelector(".text-gray.hidden.flex-wrap.items-center")).get(0).getText().split("\n");
            futaTable.setBusType(ticketInfors[0]);
            futaTable.setTotalAvailableSeat(ticketInfors[1]);
            futaTable.setTicketPrice(ticketInfors[2]);

            futaTables.add(futaTable);
            i++;
            if (i == 10) break;
        }

        return futaTables;
    }
}