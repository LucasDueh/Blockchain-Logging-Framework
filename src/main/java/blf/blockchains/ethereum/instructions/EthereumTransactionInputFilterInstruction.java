package blf.blockchains.ethereum.instructions;

import blf.blockchains.ethereum.classes.EthereumTransactionInput;
import blf.blockchains.ethereum.reader.EthereumDataReader;
import blf.blockchains.ethereum.state.EthereumProgramState;
import blf.core.exceptions.ExceptionHandler;
import blf.core.instructions.Instruction;
import blf.core.interfaces.FilterPredicate;
import blf.core.state.ProgramState;
import io.reactivex.annotations.NonNull;
import java.util.List;

/**
 * Transaction Input Scope
 */
public class EthereumTransactionInputFilterInstruction extends Instruction {
    private final FilterPredicate<String> contractAddressCriterion;
    private final String functionIdentifier;
    private final EthereumTransactionInput transactionInput;

    public EthereumTransactionInputFilterInstruction(
        @NonNull FilterPredicate<String> contractAddressCriterion,
        @NonNull String functionIdentifier,
        @NonNull EthereumTransactionInput transactionInput,
        List<Instruction> instructions
    ) {
        super(instructions);
        this.contractAddressCriterion = contractAddressCriterion;
        this.functionIdentifier = functionIdentifier;
        this.transactionInput = transactionInput;
    }

    @Override
    public void execute(ProgramState state) {
        final EthereumProgramState ethereumProgramState = (EthereumProgramState) state;
        final EthereumDataReader ethereumReader = ethereumProgramState.getReader();

        String toAddress = ethereumReader.getCurrentTransaction().getTo();
        if (this.isValidAddress(state, toAddress)) {
            String input = ethereumReader.getCurrentTransaction().getInput();
            try {
                String funcIdentifier = input.substring(0, 10);

                if (this.isValidTransactionInput(funcIdentifier)) {
                    transactionInput.decode(input, ethereumProgramState);

                    this.executeNestedInstructions(state);
                }
            } catch (java.lang.StringIndexOutOfBoundsException ex) {
                // ExceptionHandler.getInstance().handleException("Transaction field 'input' is empty.", ex);
            }
        }
    }

    private boolean isValidTransactionInput(String funcIdentifier) {
        return funcIdentifier.equals(this.functionIdentifier);
    }

    private boolean isValidAddress(ProgramState state, String address) {
        return this.contractAddressCriterion.test(state, address);
    }
}
