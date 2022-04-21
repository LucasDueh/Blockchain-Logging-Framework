package blf.blockchains.ethereum.instructions;

import blf.blockchains.ethereum.classes.EthereumSmartContractQuery;
import blf.blockchains.ethereum.state.EthereumProgramState;
import blf.core.instructions.Instruction;
import blf.core.state.ProgramState;
import blf.core.values.ValueAccessor;
import io.reactivex.annotations.NonNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * SmartContractFilter
 */
public class EthereumSmartContractFilterInstruction extends Instruction {
    private final List<EthereumSmartContractQuery> queries;
    private final ValueAccessor contractAddress;
    private final ValueAccessor blockOffset;

    public EthereumSmartContractFilterInstruction(
        @NonNull ValueAccessor contractAddress,
        @NonNull List<EthereumSmartContractQuery> queries,
        @NonNull ValueAccessor blockOffset,
        List<Instruction> instructions
    ) {
        super(instructions);
        this.queries = new ArrayList<>(queries);
        this.contractAddress = contractAddress;
        this.blockOffset = blockOffset;
    }

    @Override
    public void execute(ProgramState state) {
        final EthereumProgramState ethereumProgramState = (EthereumProgramState) state;

        final String address = (String) this.contractAddress.getValue(state);
        final BigInteger blockOffset = (BigInteger) this.blockOffset.getValue(state);

        for (EthereumSmartContractQuery query : this.queries) {
            query.query(address, ethereumProgramState, blockOffset);
        }

        this.executeNestedInstructions(state);
    }

}
