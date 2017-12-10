package handler.impl;

import com.ib.client.Contract;
import com.ib.client.Order;
import handler.ConnectionHandler;
import quote.QuoteSide;
import symbol.ContractFactory;
import symbol.ContractType;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class OrderHandler extends ConnectionHandler {

    private static Logger logger = Logger.getLogger(OrderHandler.class.getName());

    private int _orderId;

    private static OrderHandler instance;

    private OrderHandler() {
        _orderId = 0;
    }

    /// Get singleton instance
    public static OrderHandler getInstance() {
        if(instance == null) {
            instance = new OrderHandler();
        }
        return instance;
    }

    @Override
    public boolean connect(String host, int port)
    {
        if (!super.connect(host, port))
        {
            return false;
        }
        _client.reqIds(0);
        return true;
    }

    @Override
    public void nextValidId(int orderId) {
        _orderId = orderId;
        logger.info("[IB OP] nextValidId: " + _orderId);
    }

    public void placeOrder(ContractType type,
                           String symbol,
                           QuoteSide side,
                           double quantity,
                           double limitPrice,
                           double takeProfitLimitPrice,
                           double stopLossPrice) throws Exception {
        List<Order> orders = createBracketOrder(_orderId, side, quantity, limitPrice, takeProfitLimitPrice, stopLossPrice);
        Contract ibContract = ContractFactory.getContract(type, symbol).getContract();
        for (Order order : orders) {
            _client.placeOrder(order.orderId(), ibContract, order);
        }
        logger.info("[IB OP] Sent Bracket Order\n" + bracketOrderAsString(orders, ibContract));
        _orderId += 3;
    }

    private List<Order> createBracketOrder(int parentOrderId,
                                           QuoteSide action,
                                           double quantity,
                                           double limitPrice,
                                           double takeProfitLimitPrice,
                                           double stopLossPrice) {

        //This will be our main or "parent" order
        Order parent = new Order();
        parent.orderId(parentOrderId);
        parent.action(action.name().equals("BID") ? "BUY" : "SELL");
        parent.orderType("LMT");
        parent.totalQuantity(quantity);
        parent.lmtPrice(limitPrice);
        //The parent and children orders will need this attribute set to false to prevent accidental executions.
        //The LAST CHILD will have it set to true.
        parent.transmit(false);

        Order takeProfit = new Order();
        takeProfit.orderId(parent.orderId() + 1);
        takeProfit.action(action.name().equals("BID") ? "SELL" : "BUY");
        takeProfit.orderType("LMT");
        takeProfit.totalQuantity(quantity);
        takeProfit.lmtPrice(takeProfitLimitPrice);
        takeProfit.parentId(parentOrderId);
        takeProfit.transmit(false);

        Order stopLoss = new Order();
        stopLoss.orderId(parent.orderId() + 2);
        stopLoss.action(action.name().equals("BID") ? "SELL" : "BUY");
        stopLoss.orderType("STP");
        //Stop trigger price
        stopLoss.auxPrice(stopLossPrice);
        stopLoss.totalQuantity(quantity);
        stopLoss.parentId(parentOrderId);
        //In this case, the low side order will be the last child being sent. Therefore,
        // it needs to set this attribute to true to activate all its predecessors
        stopLoss.transmit(true);

        List<Order> bracketOrder = new ArrayList<>();
        bracketOrder.add(parent);
        bracketOrder.add(takeProfit);
        bracketOrder.add(stopLoss);

        return bracketOrder;
    }

    private String bracketOrderAsString(List<Order> orderList, Contract symbol)
    {
        String str = "";
        str += "Orders are on symbol: ";
        str += symbol.symbol();
        str += " @ ";
        str += symbol.exchange();
        str += "\n";
        str += primaryEntryOrderAsString(orderList.get(0));
        str += secondaryLimitOrderAsString(orderList.get(1));
        str += secondaryStopOrderOrderAsString(orderList.get(2));
        return str;
    }

    private String primaryEntryOrderAsString(Order primaryEntry)
    {
        String str = "";
        str += "Primary Entry: orderId: ";
        str += primaryEntry.orderId();
        str += " action: ";
        str += primaryEntry.action();
        str += " orderType: ";
        str += primaryEntry.orderType();
        str += " tif: ";
        str += primaryEntry.tif();
        str += " totalQuantity: ";
        str += primaryEntry.totalQuantity();
        str += " lmtPrice: ";
        str += primaryEntry.lmtPrice();
        str += "\n";
        return str;
    }

    private String secondaryLimitOrderAsString(Order secondaryLimit)
    {
        String str = "";
        str += "Secondary Limit: orderId: ";
        str += secondaryLimit.orderId();
        str += " action: ";
        str += secondaryLimit.action();
        str += " orderType: ";
        str += secondaryLimit.orderType();
        str += " tif: ";
        str += secondaryLimit.tif();
        str += " totalQuantity: ";
        str += secondaryLimit.totalQuantity();
        str += " lmtPrice: ";
        str += secondaryLimit.lmtPrice();
        str += " parentId: ";
        str += secondaryLimit.parentId();
        str += "\n";
        return str;
    }

    private String secondaryStopOrderOrderAsString(Order secondaryStop)
    {
        String str = "";
        str += "Secondary Stop: orderId: ";
        str += secondaryStop.orderId();
        str += " action: ";
        str += secondaryStop.action();
        str += " orderType: ";
        str += secondaryStop.orderType();
        str += " tif: ";
        str += secondaryStop.tif();
        str += " totalQuantity: ";
        str += secondaryStop.totalQuantity();
        str += " auxPrice: ";
        str += secondaryStop.auxPrice();
        str += " lmtPrice: ";
        str += secondaryStop.lmtPrice();
        str += " parentId: ";
        str += secondaryStop.parentId();
        str += "\n";
        return str;

    }
}
