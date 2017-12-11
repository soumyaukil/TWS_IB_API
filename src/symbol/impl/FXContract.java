package symbol.impl;

import com.ib.client.Contract;
import lombok.Builder;
import lombok.Getter;
import symbol.BaseContract;

@Builder
@Getter
public class FXContract implements BaseContract {

    private Contract contract;

    @Override
    public String toString() {
        return "SecType: " + contract.secType() + "\nCurrency: " + contract.currency() +
                "\nExchange: " + contract.exchange() + "\nSymbol :" + contract.symbol();
    }

    public static class FXContractBuilder {

        public FXContractBuilder fromString(String contractStr) {
            String contractDetails[] = contractStr.split(":");
            contract = new Contract();

            contract.secType(contractDetails[0]);
            contract.currency(contractDetails[1]);
            contract.exchange(contractDetails[2]);
            contract.symbol(contractDetails[3]);
            return this;
        }
    }
}
