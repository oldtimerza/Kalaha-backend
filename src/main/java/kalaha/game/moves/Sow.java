package kalaha.game.moves;

import kalaha.game.*;
import kalaha.game.rules.Affirmation;
import kalaha.game.rules.Check;

import java.util.List;

public class Sow  implements Move{

    private Check<DropStone> dropStoneCheck;

    private Check<CaptureOpponentsStones> captureOpponentsStonesCheck;

    private Check<TakeAnotherTurn> takeAnotherTurnCheck;

    private final int maxAttempts = 100;

    private int pitNumber;

    public Sow(Check<DropStone> dropStoneCheck, Check<CaptureOpponentsStones> captureOpponentsStonesCheck, Check<TakeAnotherTurn> takeAnotherTurnCheck, int pitNumber) {
        this.dropStoneCheck = dropStoneCheck;
        this.captureOpponentsStonesCheck = captureOpponentsStonesCheck;
        this.takeAnotherTurnCheck = takeAnotherTurnCheck;
        this.pitNumber = pitNumber;
    }

    @Override
    public void execute(Player player, Game game) {
        Pit pit = game.getGameBoard().getPitAt(pitNumber);
        List<Stone> stones = pit.removeStones();
        Stone stone = null;
        for(int i=0; i<stones.size(); i++){
            stone = stones.get(i);
            pit = pit.getNextPit();
            DropStone dropStone = new DropStone(stone, pit);
            Affirmation affirmation = dropStoneCheck.thatPlayer(player).given(game).isAllowed(dropStone);
            int attempt = 0;
            while(!affirmation.ok() && pit != null && attempt < maxAttempts){
                pit=pit.getNextPit();
                dropStone = new DropStone(stone, pit);
                affirmation = dropStoneCheck.isAllowed(dropStone);
                attempt++;
            }
            affirmation.thenExecute();
        }
        captureOpponentsStonesCheck.thatPlayer(player).given(game).isAllowed(new CaptureOpponentsStones(pit)).thenExecute();
        takeAnotherTurnCheck.thatPlayer(player).given(game).isAllowed(new TakeAnotherTurn(pit)).thenExecute();
    }
}
