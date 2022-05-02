package blf.configuration;

import blf.blockchains.ethereum.classes.EthereumTransactionInput;
import blf.core.interfaces.FilterPredicate;
import blf.core.parameters.Parameter;
import blf.core.values.ValueAccessor;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import org.web3j.crypto.Hash;
import blf.core.exceptions.ExceptionHandler;
import io.reactivex.annotations.NonNull;

/**
 * TransactionInputFilterSpecification
 */
public class TransactionInputFilterSpecification {
    private final EthereumTransactionInput transactionInput;
    private final String functionIdentifier;

    private TransactionInputFilterSpecification(String functionName, List<Parameter> inputs, List<String> inputSolTypes) {
        this.transactionInput = new EthereumTransactionInput(inputs);
        this.functionIdentifier = buildFunctionIdentifier(functionName, inputSolTypes);
    }

    private String buildFunctionIdentifier(String functionName, List<String> inputSolTypes) {
        try {
            return hashFunction(functionName, inputSolTypes).substring(0, 10);
        } catch (java.lang.StringIndexOutOfBoundsException ex) {
            ExceptionHandler.getInstance().handleException("Specified function could not be encoded.", ex);
            return "";
        }
    }

    private static String hashFunction(String functionName, List<String> inputSolTypes) {
        final StringBuilder result = new StringBuilder();
        result.append(functionName);
        result.append("(");
        final String params = inputSolTypes.stream().collect(Collectors.joining(","));
        result.append(params);
        result.append(")");
        return Hash.sha3String(result.toString());
    }

    public EthereumTransactionInput getTransactionInput() {
        return this.transactionInput;
    }

    public String getFunctionIdentifier() {
        return this.functionIdentifier;
    }

    public static TransactionInputFilterSpecification of(@NonNull String functionName, @NonNull List<ParameterSpecification> inputs) {
        final List<Parameter> inputParameters = inputs.stream().map(ParameterSpecification::getParameter).collect(Collectors.toList());
        final List<String> inputSolTypes = inputs.stream().map(ParameterSpecification::getType).collect(Collectors.toList());
        return new TransactionInputFilterSpecification(functionName, inputParameters, inputSolTypes);
    }
}
