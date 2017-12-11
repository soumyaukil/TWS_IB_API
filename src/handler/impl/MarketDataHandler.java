package handler.impl;

import com.ib.client.Contract;
import com.ib.client.TickAttr;
import com.ib.client.TickType;
import handler.ConnectionHandler;
import quote.Quote;
import quote.QuoteBook;
import quote.QuoteSide;
import symbol.ContractFactory;
import symbol.ContractType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by soumukil on 12/8/17.
 */
public class MarketDataHandler extends ConnectionHandler {

    private static Logger logger = Logger.getLogger(MarketDataHandler.class.getName());

    Map<Integer, String> _tickerIdToContract; /*!< To each data stream subscription assigned
                                  ticker id. This used to keep connection
                                  betweem contract and assigned ticker id */

    private static int sTickerId;

    private static MarketDataHandler instance;

    static {
        sTickerId = 1;
    }

    private MarketDataHandler() {
        _tickerIdToContract = new HashMap<>();
    }

    /// Get singleton instance
    public static MarketDataHandler getInstance() {
        if(instance == null) {
            instance = new MarketDataHandler();
        }
        return instance;
    }

    public synchronized boolean subscribe(ContractType type, String symbol) throws Exception {
        if (this.isConnected()) {
            // Switch to live (1) frozen (2) delayed (3) or delayed frozen (4)
            _client.reqMarketDataType(1);
            Contract ibContract = ContractFactory.getContract(type, symbol).getContract();
            logger.info("Contract Details:\n" + ibContract);

            _client.reqMktData(sTickerId, ibContract, "", false,
                    false, Collections.emptyList());
            _tickerIdToContract.put(sTickerId, symbol);

            logger.info("[IB MD] Subscribed to " + symbol + " tickerId " + sTickerId);
            ++sTickerId;
            return true;
        } else {
            logger.severe("[IB MD] Not Subscribed to " + symbol + " since Feed connection is not yet ready.");
        }
        return false;
    }

    public synchronized boolean unsubscribe(final String symbol) {
        if (_client.isConnected()) {
            if (!_tickerIdToContract.containsValue(symbol)) {
                logger.warning("[IB MD] Trying to unsubscribe from " + symbol
                        + " while it is not subscribed to it.");
                return false;
            }

            int tickerId = _tickerIdToContract.keySet()
                    .stream()
                    .filter(key -> _tickerIdToContract.get(key).equals(symbol))
                    .findFirst()
                    .get();
            _client.cancelMktData(tickerId);
            _tickerIdToContract.remove(tickerId);
            logger.info("[IB MD] Unsubscribed from " + symbol + " tickerId " + tickerId);
            return true;
        } else {
            logger.severe("[IB MD] Not Unsubscribed to " + symbol + " since Feed connection is not yet ready.");
        }
        return false;
    }

    @Override
    public void tickPrice(int tickerId, int field,
                          double price, TickAttr tickAttr) {
        if (_client.isConnected()) {
            String symbol = _tickerIdToContract.get(tickerId);
            if (price < 0) {
                logger.warning("[IB MD] Ticker " + symbol +
                        " receiving negative tick price " + price + " ignoring.");
                return;
            }
            if (TickType.BID.index() == field) {
                logger.info("[IB MD] Ticker " + symbol + " BID price: " + price);
                Quote quote = Quote.builder().securityId(symbol).bidPrice(price).build();
                QuoteBook.getInstance().updateQuote(quote, QuoteSide.BID);
            }
            else if (TickType.ASK.index() == field) {
                logger.info("[IB MD] Ticker " + symbol + " ASK price: " + price);
                Quote quote = Quote.builder().securityId(symbol).askPrice(price).build();
                QuoteBook.getInstance().updateQuote(quote, QuoteSide.ASK);
            }
            else {
                logger.info("[IB MD] Ticker " + symbol
                        + " skipping recieved ticker type " + field);
            }
        } else {
            logger.severe("[IB MD] Ignoring prices since connection down.");
        }
    }
}
