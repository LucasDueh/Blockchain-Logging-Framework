package blf.blockchains.ethereum.instructions;

import blf.blockchains.ethereum.classes.EthereumTransactionInputDecoding;
import blf.blockchains.ethereum.reader.EthereumDataReader;
import blf.blockchains.ethereum.state.EthereumProgramState;
import blf.core.state.ProgramState;
import blf.core.instructions.Instruction;
import blf.core.interfaces.FilterPredicate;
import io.reactivex.annotations.NonNull;

import java.util.List;

/**
 * Transaction Input Decoding Scope
 */
public class EthereumTransactionInputDecodingFilterInstruction extends Instruction {
    private final FilterPredicate<String> transactionInputCriterion;
    private final EthereumTransactionInputDecoding decoding;

    public EthereumTransactionInputDecodingFilterInstruction(
            @NonNull FilterPredicate<String> transactionInputCriterion,
            @NonNull EthereumTransactionInputDecoding decoding,
            List<Instruction> instructions) {
        super(instructions);
        this.transactionInputCriterion = transactionInputCriterion;
        this.decoding = decoding;
    }

    @Override
    public void execute(ProgramState state) {
        final EthereumProgramState ethereumProgramState = (EthereumProgramState) state;
        final EthereumDataReader ethereumReader = ethereumProgramState.getReader();

        String input = ethereumReader.getCurrentTransaction().getInput();
        String functionIdentifier = input.substring(0, 10);

        if (this.isValidTransactionInput(state, functionIdentifier)) {
            decoding.decode(input, ethereumProgramState);

            this.executeNestedInstructions(state);
        }
    }

    private boolean isValidTransactionInput(ProgramState state, String functionIdentifier) {
        return this.transactionInputCriterion.test(state, functionIdentifier);
    }
}
