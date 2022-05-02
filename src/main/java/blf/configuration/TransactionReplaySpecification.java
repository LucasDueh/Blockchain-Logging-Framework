package blf.configuration;

import blf.blockchains.ethereum.classes.EthereumTransactionReplay;
import blf.core.parameters.Parameter;
import java.util.List;
import java.util.stream.Collectors;
import io.reactivex.annotations.NonNull;

/**
 * TransactionReplaySpecification
 */
public class TransactionReplaySpecification {
    private final EthereumTransactionReplay transactionReplay;

    private TransactionReplaySpecification(List<Parameter> outputs) {
        this.transactionReplay = new EthereumTransactionReplay(outputs);
    }

    public EthereumTransactionReplay getTransactionReplay() {
        return this.transactionReplay;
    }

    public static TransactionReplaySpecification of(
        @NonNull List<ParameterSpecification> outputParameters
    ) {
        final List<Parameter> outputs = outputParameters.stream().map(ParameterSpecification::getParameter).collect(Collectors.toList());
        return new TransactionReplaySpecification(outputs);
    }
}
