package blf.configuration;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import blf.blockchains.ethereum.classes.EthereumSmartContractQuery;
import blf.core.values.ValueAccessor;

/**
 * SmartContractFilterSpecification
 */
public class SmartContractFilterSpecification {
    private final List<EthereumSmartContractQuery> queries;
    private final ValueAccessor contractAddress;
    private final ValueAccessor blockOffset;

    private SmartContractFilterSpecification(ValueAccessor contractAddress, List<EthereumSmartContractQuery> queries, ValueAccessor blockOffset) {
        this.contractAddress = contractAddress;
        this.queries = queries;
        this.blockOffset = blockOffset;
    }

    ValueAccessor getContractAddress() {
        return this.contractAddress;
    }

    List<EthereumSmartContractQuery> getQueries() {
        return this.queries;
    }

    ValueAccessor getBlockOffset() {
        return this.blockOffset;
    }

    public static SmartContractFilterSpecification of(ValueAccessorSpecification contractAddress, SmartContractQuerySpecification queries)
        throws BuildException {
        return of(contractAddress, Arrays.asList(queries), null);
    }

    public static SmartContractFilterSpecification of(ValueAccessorSpecification contractAddress, SmartContractQuerySpecification queries, ValueAccessor blockOffset)
        throws BuildException {
        return of(contractAddress, Arrays.asList(queries), blockOffset);
    }

    public static SmartContractFilterSpecification of(
        ValueAccessorSpecification contractAddress,
        List<SmartContractQuerySpecification> querySpecs,
        ValueAccessor blockOffset
    ) {
        final ArrayList<EthereumSmartContractQuery> queries = new ArrayList<>();
        for (SmartContractQuerySpecification querySpec : querySpecs) {
            queries.add(querySpec.getQuery());
        }

        return new SmartContractFilterSpecification(contractAddress.getValueAccessor(), queries, blockOffset);
    }

}
