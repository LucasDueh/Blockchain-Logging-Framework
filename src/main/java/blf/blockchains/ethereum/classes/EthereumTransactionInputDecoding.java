package blf.blockchains.ethereum.classes;

import blf.blockchains.ethereum.reader.EthereumDataReader;
import blf.blockchains.ethereum.reader.EthereumTransaction;
import blf.blockchains.ethereum.reader.EthereumClient;
import blf.blockchains.ethereum.state.EthereumProgramState;
import blf.core.exceptions.ExceptionHandler;
import blf.core.parameters.Parameter;
import io.reactivex.annotations.NonNull;
import org.web3j.abi.TypeDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.FunctionReturnDecoder;

import static org.web3j.abi.Utils.convert;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * TransactionInputDecoding
 */
public class EthereumTransactionInputDecoding {
    private final List<Parameter> inputArguments;

    public EthereumTransactionInputDecoding(@NonNull List<Parameter> inputArguments) {
        this.inputArguments = new ArrayList<>(inputArguments);
    }

    @SuppressWarnings("all")
    public void decode(String input, EthereumProgramState state) {

        final String queryErrorMsg = "Error decoding input arguments.";
        final String stateNullErrorMsg = "State is null.";

        if (state == null) {
            ExceptionHandler.getInstance().handleException(stateNullErrorMsg, new Exception());

            return;
        }

        try {
            final List<TypeReference<?>> inputTypeReferences = this.createInputTypes();
            assert inputTypeReferences.size() == this.inputArguments.size();

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
        } catch (Throwable cause) {
            ExceptionHandler.getInstance().handleException(queryErrorMsg, cause);
        }
    }

    private List<TypeReference<?>> createInputTypes() {
        return this.inputArguments.stream().map(Parameter::getType).collect(Collectors.toList());
    }

    // @SuppressWarnings("all")
    // private void setValues(List<Type> values, EthereumProgramState state) {
    // if (!this.matchOutputParameters(values)) {
    // throw new IllegalArgumentException("Output parameters not compatible with
    // return values.");
    // }

    // IntStream.range(0, values.size()).forEach(i -> {
    // final Object value = values.get(i).getValue();
    // final String name = this.inputArguments.get(i).getName();
    // state.getValueStore().setValue(name, value);
    // });
    // }

    // @SuppressWarnings("all")
    // private boolean matchOutputParameters(List<Type> values) {
    // if (values.size() != this.inputArguments.size()) {
    // return false;
    // }

    // return IntStream.range(0, values.size()).allMatch(i ->
    // typesMatch(values.get(i), this.inputArguments.get(i)));
    // }

    // @SuppressWarnings("all")
    // private boolean typesMatch(Type type, Parameter parameter) {
    // if (type == null) {
    // return false;
    // }
    // try {
    // return parameter.getType().getClassType().equals(type.getClass());
    // } catch (Throwable cause) {
    // return false;
    // }
    // }
}
