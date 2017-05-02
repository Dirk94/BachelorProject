package testingstuff.data;

public class Edge {
    
    public DfaState stateFrom, stateTo;

    public int label;

    public Edge(DfaState stateFrom, DfaState stateTo, int label) {
        this.stateFrom = stateFrom;
        this.stateTo = stateTo;
        this.label = label;
    }
}
