package blf.blockchains.ethereum.classes;

import blf.blockchains.ethereum.reader.EthereumClient;
import blf.blockchains.ethereum.reader.EthereumTransaction;
import blf.blockchains.ethereum.state.EthereumProgramState;
import blf.core.exceptions.ExceptionHandler;
import blf.core.parameters.Parameter;
import io.reactivex.annotations.NonNull;
import org.web3j.abi.TypeDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.web3j.abi.Utils.convert;

/**
 * PublicMemberQuery
 */
public class EthereumTransactionReplay {
    private final List<Parameter> outputParameters;

    public EthereumTransactionReplay(@NonNull List<Parameter> outputParameters) {
        this.outputParameters = new ArrayList<>(outputParameters);
    }

    @SuppressWarnings("all")
    public void replay(String hash, EthereumProgramState state) {

        final String replayErrorMsg = String.format("Error replaying transaction.");
        final String stateNullErrorMsg = "State is null.";

        if (state == null) {
            ExceptionHandler.getInstance().handleException(stateNullErrorMsg, new Exception());

            return;
        }

        try {
            final EthereumClient client = state.getReader().getClient();

            final List<TypeReference<?>> outputs = this.createReturnTypes();
            final List<Type> values = client.replayTransaction(hash, convert(outputs));
            this.setValues(values, state);
        } catch (Throwable cause) {
            ExceptionHandler.getInstance().handleException(replayErrorMsg, cause);
        }
    }

    private List<TypeReference<?>> createReturnTypes() {
        return this.outputParameters.stream().map(Parameter::getType).collect(Collectors.toList());
    }

    @SuppressWarnings("all")
    private void setValues(List<Type> values, EthereumProgramState state) {
        if (!this.matchOutputParameters(values)) {
            throw new IllegalArgumentException("Output parameters not compatible with return values.");
        }

        IntStream.range(0, values.size()).forEach(i -> {
            final Object value = values.get(i).getValue();
            final String name = this.outputParameters.get(i).getName();
            state.getValueStore().setValue(name, value);
        });
    }

    @SuppressWarnings("all")
    private boolean matchOutputParameters(List<Type> values) {
        if (values.size() != this.outputParameters.size()) {
            return false;
        }

        return IntStream.range(0, values.size()).allMatch(i -> typesMatch(values.get(i), this.outputParameters.get(i)));
    }

    @SuppressWarnings("all")
    private boolean typesMatch(Type type, Parameter parameter) {
        if (type == null) {
            return false;
        }
        try {
            return parameter.getType().getClassType().equals(type.getClass());
        } catch (Throwable cause) {
            return false;
        }
    }

}
