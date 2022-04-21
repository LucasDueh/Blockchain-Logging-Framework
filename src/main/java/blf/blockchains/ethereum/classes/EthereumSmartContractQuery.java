package blf.blockchains.ethereum.classes;

import java.math.BigInteger;

import blf.blockchains.ethereum.state.EthereumProgramState;

/**
 * SmartContractQuery
 */
public interface EthereumSmartContractQuery {
    void query(String contract, EthereumProgramState state, BigInteger blockOffset);
}
