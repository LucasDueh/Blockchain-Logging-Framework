package blf.configuration;

import blf.blockchains.ethereum.classes.EthereumTransactionInput;
import blf.core.interfaces.FilterPredicate;
import blf.core.parameters.Parameter;
import blf.core.values.ValueAccessor;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TransactionInputFilterSpecification
 */
public class TransactionInputFilterSpecification {
    private final EthereumTransactionInput transactionInput;
    private final FilterPredicate<String> transactionInputCriterion;

    private TransactionInputFilterSpecification(ValueAccessor functionIdentifier, List<Parameter> inputs) {
        this.transactionInput = new EthereumTransactionInput(inputs);
        this.transactionInputCriterion = (state, funcIdentifier) -> {
            final ValueAccessor accessor = functionIdentifier;
            final Object value = accessor.getValue(state);
            if (value instanceof String) {
                return funcIdentifier.equals(value);
            }
            return false;
        };
    }

    public EthereumTransactionInput getTransactionInput() {
        return this.transactionInput;
    }

    public FilterPredicate<String> getTransactionInputCriterion() {
        return this.transactionInputCriterion;
    }

    public static TransactionInputFilterSpecification of(
        ValueAccessorSpecification functionIdentifier,
        List<ParameterSpecification> inputs
    ) {
        final List<Parameter> inputParameters = inputs.stream().map(ParameterSpecification::getParameter).collect(Collectors.toList());
        return new TransactionInputFilterSpecification(functionIdentifier.getValueAccessor(), inputParameters);
    }
}
