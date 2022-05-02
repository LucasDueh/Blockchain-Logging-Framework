package blf.blockchains.ethereum.instructions;

import blf.blockchains.ethereum.classes.EthereumTransactionReplay;
import blf.blockchains.ethereum.reader.EthereumDataReader;
import blf.blockchains.ethereum.state.EthereumProgramState;
import blf.core.instructions.Instruction;
import blf.core.state.ProgramState;
import io.reactivex.annotations.NonNull;
import java.util.List;

/**
 * Transaction Input Scope
 */
public class EthereumTransactionReplayInstruction extends Instruction {
    private final EthereumTransactionReplay transactionReplay;

    public EthereumTransactionReplayInstruction(
        @NonNull EthereumTransactionReplay transactionReplay,
        List<Instruction> instructions
    ) {
        super(instructions);
        this.transactionReplay = transactionReplay;
    }

    @Override
    public void execute(ProgramState state) {
        final EthereumProgramState ethereumProgramState = (EthereumProgramState) state;
        final EthereumDataReader ethereumReader = ethereumProgramState.getReader();

        String hash = ethereumReader.getCurrentTransaction().getHash();
        
        transactionReplay.replay(hash, ethereumProgramState);

        this.executeNestedInstructions(state);
    }
}
