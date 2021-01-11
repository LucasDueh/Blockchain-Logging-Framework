package blf.blockchains.hyperledger.instructions;

import blf.blockchains.hyperledger.state.HyperledgerProgramState;
import blf.core.exceptions.ExceptionHandler;
import blf.core.exceptions.ProgramException;
import blf.core.interfaces.Instruction;
import blf.core.state.ProgramState;
import org.antlr.v4.runtime.misc.Pair;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.BlockInfo;
import org.hyperledger.fabric.sdk.ChaincodeEvent;
import org.json.*;

import java.math.BigInteger;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 */
public class HyperledgerLogEntryFilterInstruction implements Instruction {

    private final Logger logger;
    private final ExceptionHandler exceptionHandler;

    private final List<String> addressNames;
    private final String eventName;
    private final List<Pair<String, String>> entryParameters;

    public HyperledgerLogEntryFilterInstruction(
        final List<String> addressNames,
        String eventName,
        List<Pair<String, String>> entryParameters
    ) {
        this.addressNames = addressNames;
        this.eventName = eventName;
        this.entryParameters = entryParameters;

        this.logger = Logger.getLogger(HyperledgerLogEntryFilterInstruction.class.getName());
        this.exceptionHandler = new ExceptionHandler();
    }

    @Override
    public void execute(ProgramState state) throws ProgramException {
        HyperledgerProgramState hyperledgerProgramState = (HyperledgerProgramState) state;

        // get current block
        BlockEvent be = hyperledgerProgramState.getCurrentBlock();
        if (be == null) {
            this.exceptionHandler.handleExceptionAndDecideOnAbort("Expected block, received null", new NullPointerException());

            return;
        }

        for (BlockEvent.TransactionEvent te : be.getTransactionEvents()) {
            for (BlockInfo.TransactionEnvelopeInfo.TransactionActionInfo ti : te.getTransactionActionInfos()) {
                ChaincodeEvent ce = ti.getEvent();
                if (ce != null) {
                    // first try parse json
                    String payloadString = new String(ce.getPayload());
                    try {
                        JSONObject obj = new JSONObject(payloadString);

                        // if json is nested, search for eventName as index in the nested object
                        try {
                            JSONObject eventObj = obj.getJSONObject(ce.getEventName());
                            for (Pair<String, String> parameter : this.entryParameters) {
                                String parameterType = parameter.a;
                                String parameterName = parameter.b;

                                setStateValue(hyperledgerProgramState, parameterName, parameterType, eventObj);
                            }
                        } catch (JSONException e) {
                            // payload does not contain a nested json object with the given eventName
                            // if json is flat, check if the eventName is the event name of the Event
                            if (this.eventName.equals(ce.getEventName())) {
                                for (Pair<String, String> parameter : this.entryParameters) {
                                    String parameterType = parameter.a;
                                    String parameterName = parameter.b;

                                    setStateValue(hyperledgerProgramState, parameterName, parameterType, obj);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        // there is no valid json in the payload
                        // if json parsing fails completely, also check the eventName and try to parse the byte array as single value
                        if (this.eventName.equals(ce.getEventName())) {
                            if (this.entryParameters.size() == 1) {
                                // parse the one unstructured parameter
                                String parameterType = this.entryParameters.get(0).a;
                                String parameterName = this.entryParameters.get(0).b;
                                setStateValue(hyperledgerProgramState, parameterName, parameterType, payloadString, ce.getPayload());
                            } else {
                                this.exceptionHandler.handleExceptionAndDecideOnAbort(
                                    "We expect exactly one parameter when extracting unstructured data",
                                    e
                                );
                            }
                        }
                    }
                }
            }
        }

        final String infoMsg = String.format(
            "Executing HyperledgerLogEntryFilterInstruction(addressNames -> %s | eventName -> %s | entryParameters -> %s) for the block %s",
            this.addressNames.toString(),
            this.eventName,
            this.entryParameters.toString(),
            hyperledgerProgramState.getCurrentBlockNumber().toString()
        );

        logger.info(infoMsg);
    }

    private void setStateValue(
        HyperledgerProgramState hyperledgerProgramState,
        String parameterName,
        String parameterType,
        String payloadString,
        byte[] payload
    ) {
        if (parameterType.contains("int")) {
            try {
                BigInteger data = new BigInteger(payloadString);
                hyperledgerProgramState.getValueStore().setValue(parameterName, data);
            } catch (NumberFormatException e) {
                this.exceptionHandler.handleExceptionAndDecideOnAbort("Could not parse payload to BigInteger", e);
            }
        } else if (parameterType.contains("string")) {
            hyperledgerProgramState.getValueStore().setValue(parameterName, payloadString);
        } else if (parameterType.contains("bool")) {
            try {
                boolean data = payload[0] != 0;
                hyperledgerProgramState.getValueStore().setValue(parameterName, data);
            } catch (ArrayIndexOutOfBoundsException e) {
                this.exceptionHandler.handleExceptionAndDecideOnAbort("Could not convert empty payload to bool", e);
            }
        } else if (parameterType.equals("byte")) {
            try {
                byte data = payload[0];
                hyperledgerProgramState.getValueStore().setValue(parameterName, data);
            } catch (ArrayIndexOutOfBoundsException e) {
                this.exceptionHandler.handleExceptionAndDecideOnAbort("Could not access byte in empty payload", e);
            }
        } else if (parameterType.contains("bytes")) {
            hyperledgerProgramState.getValueStore().setValue(parameterName, payload);
        }
    }

    private void setStateValue(
        HyperledgerProgramState hyperledgerProgramState,
        String parameterName,
        String parameterType,
        JSONObject obj
    ) {
        if (obj.has(parameterName)) {
            try {
                if (parameterType.contains("int")) {
                    BigInteger data = obj.getBigInteger(parameterName);
                    hyperledgerProgramState.getValueStore().setValue(parameterName, data);
                } else if (parameterType.contains("bool")) {
                    boolean data = obj.getBoolean(parameterName);
                    hyperledgerProgramState.getValueStore().setValue(parameterName, data);
                } else if (parameterType.contains("string") || parameterType.equals("byte") || parameterType.contains("bytes")) {
                    // TODO maybe something else is better suited here for byte and bytes?
                    String data = obj.getString(parameterName);
                    hyperledgerProgramState.getValueStore().setValue(parameterName, data);
                }
            } catch (JSONException e) {
                this.exceptionHandler.handleExceptionAndDecideOnAbort("Wrong type: " + parameterType, e);
            }
        } else {
            String message = "JSON object does not contain key: " + parameterName;
            this.exceptionHandler.handleExceptionAndDecideOnAbort(message, new JSONException(message));
        }
    }
}
