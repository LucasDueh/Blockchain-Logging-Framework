package blf.parsing;

import blf.grammar.BcqlParser;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import org.antlr.v4.runtime.Token;

/**
 * FilterCombinationAnalyzer
 */
public class FilterNestingAnalyzer extends SemanticAnalyzer {
    private final Stack<String> filterStack;

    public FilterNestingAnalyzer(ErrorCollector errorCollector) {
        super(errorCollector);
        this.filterStack = new Stack<>();

    }

    @Override
    public void clear() {
        this.filterStack.clear();
    }

    @Override
    public void enterDocument(BcqlParser.DocumentContext ctx) {
        this.filterStack.push(PROGRAM);
    }

    @Override
    public void exitDocument(BcqlParser.DocumentContext ctx) {
        this.filterStack.pop();
    }

    @Override
    public void enterScope(BcqlParser.ScopeContext ctx) {
        final String filterType = this.getFilterName(ctx.filter());
        this.checkFilter(ctx.filter().start, filterType);
        this.filterStack.add(filterType);
    }

    @Override
    public void exitScope(BcqlParser.ScopeContext ctx) {
        this.filterStack.pop();
    }

    private void checkFilter(Token token, String filterType) {
        if (this.verifyEnclosingType(filterType)) {
            return;
        }

        this.addError(token, "Invalid nesting of filters.");
    }

    private boolean verifyEnclosingType(String filterType) {
        return VALID_ENCLOSING_FILTERS.get(filterType).test(this.filterStack);
    }

    private String getFilterName(BcqlParser.FilterContext ctx) {
        if (ctx.blockFilter() != null) {
            return BLOCK_FILTER;
        } else if (ctx.transactionFilter() != null) {
            return TRANSACTION_FILTER;
        } else if (ctx.logEntryFilter() != null) {
            return LOG_ENRY_FILTER;
        } else if (ctx.genericFilter() != null) {
            return GENERIC_FILTER;
        } else if (ctx.smartContractFilter() != null) {
            return SMART_CONTRACT_FILTER;
        } else if (ctx.transactionInputFilter() != null) {
            return TRANSACTION_INPUT_FILTER;
        } else if (ctx.transactionReplay() != null) {
            return TRANSACTION_REPLAY;
        } else {
            throw new UnsupportedOperationException("This filter type is not supported");
        }
    }

    private static final String PROGRAM = "program";
    private static final String BLOCK_FILTER = "block";
    private static final String GENERIC_FILTER = "generic";
    private static final String TRANSACTION_FILTER = "transaction";
    private static final String LOG_ENRY_FILTER = "log entry";
    private static final String SMART_CONTRACT_FILTER = "smart contract";
    private static final String TRANSACTION_INPUT_FILTER = "transaction input";
    private static final String TRANSACTION_REPLAY = "transaction replay";
    private static final Map<String, Predicate<Stack<String>>> VALID_ENCLOSING_FILTERS;

    static {
        VALID_ENCLOSING_FILTERS = new HashMap<>();
        VALID_ENCLOSING_FILTERS.put(BLOCK_FILTER, FilterNestingAnalyzer::areBlockFilterParentsValid);
        VALID_ENCLOSING_FILTERS.put(TRANSACTION_FILTER, FilterNestingAnalyzer::areTransactionFilterParentsValid);
        VALID_ENCLOSING_FILTERS.put(LOG_ENRY_FILTER, FilterNestingAnalyzer::areLogEntryFilterParentsValid);
        VALID_ENCLOSING_FILTERS.put(GENERIC_FILTER, FilterNestingAnalyzer::areGenericFilterParentsValid);
        VALID_ENCLOSING_FILTERS.put(SMART_CONTRACT_FILTER, FilterNestingAnalyzer::areSmartContractFilterParentsValid);
        VALID_ENCLOSING_FILTERS.put(TRANSACTION_INPUT_FILTER, FilterNestingAnalyzer::areTransactionInputFilterParentsValid);
        VALID_ENCLOSING_FILTERS.put(TRANSACTION_REPLAY, FilterNestingAnalyzer::areTransactionReplayParentsValid);
    }

    private static boolean areBlockFilterParentsValid(Stack<String> stack) {
        return !stack.isEmpty()
            && stack.get(0).equals(PROGRAM)
            && IntStream.range(1, stack.size()).allMatch(i -> stack.get(i).equals(GENERIC_FILTER));
    }

    private static boolean areTransactionFilterParentsValid(Stack<String> stack) {
        return !stack.isEmpty() && stack.get(0).equals(PROGRAM) && countFilters(stack, BLOCK_FILTER) == 1;
    }

    private static boolean areLogEntryFilterParentsValid(Stack<String> stack) {
        return !stack.isEmpty()
            && stack.get(0).equals(PROGRAM)
            && countFilters(stack, BLOCK_FILTER) == 1
            && countFilters(stack, TRANSACTION_FILTER) <= 1;
    }

    private static long countFilters(Stack<String> stack, String filter) {
        return stack.stream().filter(f -> f.equals(filter)).count();
    }

    private static boolean areGenericFilterParentsValid(Stack<String> stack) {
        return !stack.isEmpty() && stack.get(0).equals(PROGRAM);
    }

    private static boolean areSmartContractFilterParentsValid(Stack<String> stack) {
        boolean isValidParent = stack.peek().equals(BLOCK_FILTER) || stack.peek().equals(TRANSACTION_INPUT_FILTER);
        return !stack.isEmpty() && isValidParent;
    }

    private static boolean areTransactionInputFilterParentsValid(Stack<String> stack) {
        return !stack.isEmpty()
            && stack.peek().equals(TRANSACTION_FILTER)
            && countFilters(stack, BLOCK_FILTER) == 1
            && countFilters(stack, TRANSACTION_FILTER) == 1;
    }

    private static boolean areTransactionReplayParentsValid(Stack<String> stack) {
        return !stack.isEmpty()
            && stack.peek().equals(TRANSACTION_INPUT_FILTER)
            && countFilters(stack, BLOCK_FILTER) == 1
            && countFilters(stack, TRANSACTION_FILTER) == 1
            && countFilters(stack, TRANSACTION_INPUT_FILTER) == 1;
    }
}
