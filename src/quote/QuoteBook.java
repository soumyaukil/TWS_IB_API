package quote;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class QuoteBook {

    private Lock _lock;

    private Map<String, Quote> _quoteMap;

    private static QuoteBook _instance;

    private QuoteBook() {
        _lock = new ReentrantLock();
        _quoteMap = new HashMap<>();
    }

    /// Get singleton instance
    public static QuoteBook getInstance() {
        if(_instance == null) {
            _instance = new QuoteBook();
        }
        return _instance;
    }

    public Quote getQuote(String securityId) {
        if (_quoteMap.containsKey(securityId)) {
            return _quoteMap.get(securityId);
        }
        return null;
    }

    public void addQuote(Quote quote) {
        _lock.lock();
        _quoteMap.put(quote.getSecurityId(), quote);
        _lock.unlock();
    }

    public void updateQuote(Quote quote, QuoteSide side) {
        _lock.lock();
        if (!_quoteMap.containsKey(quote.getSecurityId())) {
            _quoteMap.put(quote.getSecurityId(), quote);
        }
        else if(side == QuoteSide.BID) {
            _quoteMap.get(quote.getSecurityId()).setBidPrice(quote.getBidPrice());
        }
        else {
            _quoteMap.get(quote.getSecurityId()).setBidPrice(quote.getAskPrice());
        }
        _lock.unlock();
    }

    public void deleteQuote(String securityId) {
        _lock.lock();
        _quoteMap.remove(securityId);
        _lock.unlock();
    }
}
