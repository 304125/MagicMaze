package org.game.model.AI;

import org.game.model.Action;
import org.game.model.board.Board;
import org.game.model.Player;
import org.game.utils.ActionDelegator;

import java.util.List;

public abstract class AIPlayer extends Player  implements StateChangeListener, AIPlayerBehavior{
    private final Board board;
    private ActionDelegator actionDelegator;

    public AIPlayer(List<Action> actions, String name, Board board) {
        super(actions, name);
        this.board = board;
    }

    public Board getBoard() {
        return board;
    }

    public void setActionDelegator(ActionDelegator actionDelegator){
        this.actionDelegator = actionDelegator;
    }

    public ActionDelegator getActionDelegator(){
        return actionDelegator;
    }


}
