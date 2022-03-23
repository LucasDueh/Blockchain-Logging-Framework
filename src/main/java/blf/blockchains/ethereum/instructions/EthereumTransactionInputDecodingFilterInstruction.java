package blf.blockchains.ethereum.instructions;

import blf.blockchains.ethereum.classes.EthereumTransactionInputDecoding;
import blf.blockchains.ethereum.state.EthereumProgramState;
import blf.core.state.ProgramState;
import blf.core.instructions.Instruction;
import io.reactivex.annotations.NonNull;

import java.util.List;

/**
 * Transaction Input Decoding Scope
 */
public class EthereumTransactionInputDecodingFilterInstruction extends Instruction {
  private final EthereumTransactionInputDecoding decoding;

  public EthereumTransactionInputDecodingFilterInstruction(
        @NonNull EthereumTransactionInputDecoding decoding,
        List<Instruction> instructions
    ) {
        super(instructions);
        this.decoding = decoding;
    }

    @Override
    public void execute(ProgramState state) {
        final EthereumProgramState ethereumProgramState = (EthereumProgramState) state;

        decoding.decode(ethereumProgramState);

        this.executeNestedInstructions(state);
    }
}