package blf.blockchains.ethereum.classes;

import blf.blockchains.ethereum.state.EthereumProgramState;
import blf.core.exceptions.ExceptionHandler;
import blf.core.parameters.Parameter;
import io.reactivex.annotations.NonNull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;

import static org.web3j.abi.Utils.convert;

/**
 * TransactionInput
 */
public class EthereumTransactionInput {
    private final List<Parameter> inputArguments;

    public EthereumTransactionInput(@NonNull List<Parameter> inputArguments) {
        this.inputArguments = new ArrayList<>(inputArguments);
    }

    @SuppressWarnings("all")
    public void decode(String input, EthereumProgramState state) {

        final String queryErrorMsg = "Error decoding input arguments.";
        final String txInputEmptyErrorMsg = "Transaction input does not hold arguments.";
        final String stateNullErrorMsg = "State is null.";

        if (state == null) {
            ExceptionHandler.getInstance().handleException(stateNullErrorMsg, new Exception());

            return;
        }

        final List<TypeReference<?>> inputTypeReferences = this.createInputTypes();
        assert inputTypeReferences.size() == this.inputArguments.size();

        try {
            String encodedInputArgs = input.substring(10);
            final List<Object> results = FunctionReturnDecoder.decode(encodedInputArgs, convert(inputTypeReferences))
            .stream()
            .map(Type::getValue)
            .collect(Collectors.toList());

            assert this.inputArguments.size() == results.size();

            for (int i = 0; i < this.inputArguments.size(); i++) {
                String nameOfVariable = this.inputArguments.get(i).getName();
                Object value = results.get(i);
                state.getValueStore().setValue(nameOfVariable, value);
            }
        } catch (java.lang.StringIndexOutOfBoundsException ex) {
            ExceptionHandler.getInstance().handleException(txInputEmptyErrorMsg, ex);
        } catch (Throwable cause) {
            ExceptionHandler.getInstance().handleException(queryErrorMsg, cause);
        }
    }

    private List<TypeReference<?>> createInputTypes() {
        return this.inputArguments.stream().map(Parameter::getType).collect(Collectors.toList());
    }
}
