package kalaha.game.rules;

import kalaha.game.Game;
import kalaha.game.Player;
import kalaha.game.moves.Move;

public class NotAllowed extends Affirmation {

    public NotAllowed(Move move, Player player, Game game) {
        super(move, player, game);
    }

    @Override
    public boolean ok() {
        return false;
    }

    @Override
    public void thenExecute() {

    }
}
