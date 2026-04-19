package br.ufrn.imd.pd.proposer.paxos;

public class PaxosRoundResult {

    private final boolean success;
    private final String value;
    private final String errorMessage;

    private PaxosRoundResult(boolean success, String value, String errorMessage) {
        this.success = success;
        this.value = value;
        this.errorMessage = errorMessage;
    }

    public static PaxosRoundResult success(String committedValue) {
        return new PaxosRoundResult(true, committedValue, null);
    }

    public static PaxosRoundResult failure(String reason) {
        return new PaxosRoundResult(false, null, reason);
    }

    public boolean isSuccess() { return success; }
    public String getValue() { return value; }
    public String getErrorMessage() { return errorMessage; }
}
