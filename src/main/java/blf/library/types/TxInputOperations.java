package blf.library.types;

import java.math.BigInteger;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import blf.core.exceptions.ExceptionHandler;
import blf.core.state.ProgramState;
import blf.library.util.TriFunction;

import org.web3j.abi.TypeDecoder;

/**
 * TxInputOperations
 */
public class TxInputOperations {
  public static <T> T decodeTxInput(Object[] parameters, ProgramState state) {
    return (T) operate(state, parameters,
        (string, stringList, index) -> decode(string, stringList, index, getParamClass(stringList, index)));
  }

  private static <T> T decode(String string, List<String> paramTypes, BigInteger index, Class<T> cl) {
    return cl.cast(string);
  }

  private static Class<?> getParamClass(List<String> paramTypes, BigInteger index) {
    String paramType = paramTypes.get(index.intValue());

    // TODO: Cover all possible types
    switch (paramType) {
      case "address":
        return String.class;
      case "string":
        return String.class;
      case "uint256":
        return BigInteger.class;
      case "bool":
        return Boolean.class;
      case "uint256[]":
        // TODO: Return correct type List<Integer>.class
        return List.class;
      default:
        // TODO: Throw error
        return String.class;
    }
  }

  @SuppressWarnings("unchecked")
  private static <T> T operate(ProgramState state, Object[] parameters,
      TriFunction<String, List<String>, BigInteger, T> operation) {
    if (!areValidParameters(parameters)) {
      ExceptionHandler.getInstance().handleException("Invalid parameters for method call.", new Exception());

      return null;
    }

    final String operand1 = (String) parameters[0];
    final List<String> operand2 = (List<String>) parameters[1];
    final BigInteger operand3 = (BigInteger) parameters[2];

    try {
      return operation.apply(operand1, operand2, operand3);
    } catch (Exception e) {
      ExceptionHandler.getInstance().handleException("Error executing method call.", e);
    }

    return null;
  }

  private static boolean areValidParameters(Object[] parameters) {
    return parameters != null
        && parameters.length == 3;
  }
}
