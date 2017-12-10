package bin;

import config.ConfigManager;
import handler.ConnectionHandler;
import handler.impl.MarketDataHandler;
import handler.impl.OrderHandler;
import quote.Quote;
import quote.QuoteBook;
import quote.QuoteSide;
import sun.security.krb5.Config;
import symbol.ContractType;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by soumukil on 12/8/17.
 */
public class MainExecutor {

    private static Logger logger = Logger.getLogger(MainExecutor.class.getName());

    public static void main(String []args) throws Exception {

        String symbol = "CASH:GBP:IDEALPRO:EUR";

        ConfigManager configManager = ConfigManager.getInstance();
        configManager.load();

        MarketDataHandler mdHandler = MarketDataHandler.getInstance();
        mdHandler.connect(configManager.getAsString("ibhost"), configManager.getAsInt("ibport"));

        OrderHandler orderHandler = OrderHandler.getInstance();
        orderHandler.connect(configManager.getAsString("ibhost"), configManager.getAsInt("ibport"));

        if (!mdHandler.subscribe(ContractType.FX, symbol)) {
            return;
        }

        Thread mdThread = (new Thread() {
            public void run() {
                while (true) {
                    try {
                        mdHandler.processMessages();
                    } catch (Exception e) {
                        logger.severe("Got exception " + e);
                    }
                }
            }
        });

        Thread orderThread = (new Thread() {
            public void run() {
                while (true) {
                    try {
                        orderHandler.processMessages();
                    } catch (Exception e) {
                        logger.severe("Got exception " + e);
                    }
                }
            }
        });

        mdThread.start();
        orderThread.start();

        // Sleeping for 5 seconds so that we get some quotes
        Thread.sleep(5000);

        //Get price from QuoteBook for the given symbol
        Quote quote = QuoteBook.getInstance().getQuote(symbol);

        //sending bracket order here
        orderHandler.placeOrder(ContractType.FX, symbol, QuoteSide.BID, 25000, 0.8937,.9 , .89);

        mdThread.join();
        orderThread.join();
    }
}
