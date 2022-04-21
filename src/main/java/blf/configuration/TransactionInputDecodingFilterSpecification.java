package blf.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import blf.blockchains.ethereum.classes.EthereumTransactionInputDecoding;
import blf.core.values.ValueAccessor;
import blf.core.parameters.Parameter;
import blf.core.interfaces.FilterPredicate;


/**
 * TransactionInputDecodingFilterSpecification
 */
public class TransactionInputDecodingFilterSpecification {
    private final EthereumTransactionInputDecoding decoding;
    private final FilterPredicate<String> transactionInputCriterion;

    private TransactionInputDecodingFilterSpecification(ValueAccessor functionIdentifier, List<Parameter> inputs) {
      this.decoding = new EthereumTransactionInputDecoding(inputs);
      this.transactionInputCriterion = (state, funcIdentifier) -> {
        final ValueAccessor accessor = functionIdentifier;
        final Object value = accessor.getValue(state);
        if (value instanceof String) {
            return funcIdentifier.equals(value);
        }
        return false;
      };
    }

    public EthereumTransactionInputDecoding getTransactionInputDecoding() {
        return this.decoding;
    }

    public FilterPredicate<String> getTransactionInputCriterion() {
      return this.transactionInputCriterion;
    }

    public static TransactionInputDecodingFilterSpecification of(ValueAccessorSpecification functionIdentifier, List<ParameterSpecification> inputs) {
        final List<Parameter> inputParameters = inputs.stream().map(ParameterSpecification::getParameter).collect(Collectors.toList());
        return new TransactionInputDecodingFilterSpecification(functionIdentifier.getValueAccessor(), inputParameters);
    }
}
