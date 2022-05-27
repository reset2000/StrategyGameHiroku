package pl.eg.enginegame;

public class TurnAction {
        int action;
        TurnOperation operation = new TurnOperation();

        public TurnAction() {
        }

        public TurnAction(int action, TurnOperation operation) {
            this.action = action;
            this.operation = operation;
        }

        public int getAction() {
            return action;
        }
        public TurnOperation getOperation() {
            return operation;
        }

}
