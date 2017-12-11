package symbol;

import symbol.impl.FXContract;

/**
 * Created by soumukil on 12/8/17.
 */
public class ContractFactory {

    public static BaseContract getContract(ContractType type, String contractStr) throws Exception {
        switch (type) {
            case FX:
                return FXContract.builder().fromString(contractStr).build();
            default:
                throw new Exception("Unsupported contract type: " + type.name());
        }
    }
}
