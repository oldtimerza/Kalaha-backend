package kalaha.game;

import kalaha.game.moves.CaptureOpponentsStones;
import kalaha.game.moves.DropStone;
import kalaha.game.moves.Sow;
import kalaha.game.moves.TakeAnotherTurn;
import kalaha.game.rules.*;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class MovesTest {

    List<Player> players;
    GameState state;
    GameBoard gameBoard;
    Game game;

    @Before
    public void init(){
        players = new ArrayList<>();
        players.add(Mockito.mock(Player.class));
        players.add(Mockito.mock(Player.class));
        state = Mockito.mock(GameState.class);
        when(state.getCurrentPlayer()).thenReturn(players.get(0));
        gameBoard = Mockito.mock(GameBoard.class);
        game = new Game(gameBoard, state, players);
    }

    @Test
    public void dropStoneShouldAddTheStoneToThePit(){
        Pit pit = Mockito.mock(Pit.class);
        Stone stone = Mockito.mock(Stone.class);
        DropStone dropStone = new DropStone(stone, pit);
        dropStone.execute(players.get(0), game);
        List<Stone> expectedStones = new ArrayList<>();
        expectedStones.add(stone);
        verify(pit, atLeastOnce()).addStones(ArgumentMatchers.eq(expectedStones));
    }

    @Test
    public void sowShouldDropStonesIntoSubsequentPits() {
        int pitNumber = 0;
        List<Stone> stones = new ArrayList<>();
        stones.add(new Stone());
        stones.add(new Stone());
        stones.add(new Stone());
        List<Pit> pits = new ArrayList<>();
        int numberOfPits = 3;
        for(int i = 0; i < numberOfPits; i++){
            Pit pit = new Pit(new ArrayList<>(stones), players.get(0), i + 1);
            pits.add(pit);
            if(pits.size() > 1) {
                pits.get(i - 1).setNextPit(pit);
            }
        }
        Pit starterPit = new Pit(stones, players.get(0), pitNumber);
        starterPit.setNextPit(pits.get(0));
        when(gameBoard.getPitAt(pitNumber)).thenReturn(starterPit);
        Check<DropStone> dropStoneCheck = (Check<DropStone>) Mockito.mock(Check.class);
        Check<CaptureOpponentsStones> captureOpponentsStonesCheck = (Check<CaptureOpponentsStones>) Mockito.mock(Check.class);
        Affirmation mockAllowed = Mockito.mock(Allowed.class);
        when(mockAllowed.ok()).thenReturn(true);
        when(dropStoneCheck.given(ArgumentMatchers.any(Game.class))).thenReturn(dropStoneCheck);
        when(dropStoneCheck.thatPlayer(ArgumentMatchers.any(Player.class))).thenReturn(dropStoneCheck);
        when(dropStoneCheck.isAllowed(ArgumentMatchers.any(DropStone.class))).thenReturn(mockAllowed);
        when(captureOpponentsStonesCheck.given(ArgumentMatchers.any(Game.class))).thenReturn(captureOpponentsStonesCheck);
        when(captureOpponentsStonesCheck.thatPlayer(ArgumentMatchers.any(Player.class))).thenReturn(captureOpponentsStonesCheck);
        Affirmation notAllowed = Mockito.mock(NotAllowed.class);
        when(captureOpponentsStonesCheck.isAllowed(ArgumentMatchers.any(CaptureOpponentsStones.class))).thenReturn(notAllowed);
        Check<TakeAnotherTurn>  takeAnotherTurnCheck = (Check<TakeAnotherTurn>)Mockito.mock(Check.class);
        when(takeAnotherTurnCheck.given(ArgumentMatchers.any(Game.class))).thenReturn(takeAnotherTurnCheck);
        when(takeAnotherTurnCheck.thatPlayer(ArgumentMatchers.any(Player.class))).thenReturn(takeAnotherTurnCheck);
        when(takeAnotherTurnCheck.isAllowed(ArgumentMatchers.any(TakeAnotherTurn.class))).thenReturn(notAllowed);
        Sow sow = new Sow(dropStoneCheck, captureOpponentsStonesCheck, takeAnotherTurnCheck, pitNumber);
        sow.execute(players.get(0), game);
        Assert.assertThat(starterPit.getStones().size(), Matchers.equalTo(0));
        verify(mockAllowed, atLeast(numberOfPits)).thenExecute();
    }

    @Test
    public void droppingLastStoneIntoAnOwnedEmptyPitShouldCaptureStones()
    {
        List<Stone> stones = new ArrayList<>();
        stones.add(new Stone());
        stones.add(new Stone());
        Stone lastStone = new Stone();
        stones.add(lastStone);
        int pitNumber = 1;
        Pit pit = Mockito.mock(Pit.class);
        when(pit.getStones()).thenReturn(new ArrayList<>());
        when(gameBoard.getPitAt(pitNumber)).thenReturn(pit);
        Check<CaptureOpponentsStones> captureOpponentsStonesCheck = (Check<CaptureOpponentsStones>) Mockito.mock(Check.class);
        when(captureOpponentsStonesCheck.given(ArgumentMatchers.any(Game.class))).thenReturn(captureOpponentsStonesCheck);
        when(captureOpponentsStonesCheck.thatPlayer(ArgumentMatchers.any(Player.class))).thenReturn(captureOpponentsStonesCheck);
        Affirmation allowed = Mockito.mock(Allowed.class);
        when(captureOpponentsStonesCheck.isAllowed(ArgumentMatchers.any(CaptureOpponentsStones.class))).thenReturn(allowed);
        Check<TakeAnotherTurn>  takeAnotherTurnCheck = (Check<TakeAnotherTurn>)Mockito.mock(Check.class);
        when(takeAnotherTurnCheck.given(ArgumentMatchers.any(Game.class))).thenReturn(takeAnotherTurnCheck);
        when(takeAnotherTurnCheck.thatPlayer(ArgumentMatchers.any(Player.class))).thenReturn(takeAnotherTurnCheck);
        NotAllowed notAllowed = Mockito.mock(NotAllowed.class);
        when(takeAnotherTurnCheck.isAllowed(ArgumentMatchers.any(TakeAnotherTurn.class))).thenReturn(notAllowed);
        Check<DropStone> dropStoneCheck = Mockito.mock(Check.class);
        Sow sow = new Sow(dropStoneCheck, captureOpponentsStonesCheck, takeAnotherTurnCheck, pitNumber);
        sow.execute(players.get(0), game);
        verify(allowed).thenExecute();
    }

    @Test
    public void captureOpponentsStonesShouldTakeAllStonesFromOppositePitAndAddThemToMyKalaha(){
        Pit myPit = Mockito.mock(Pit.class);
        CaptureOpponentsStones captureOpponentsStones = new CaptureOpponentsStones(myPit);

        List<Stone> opponentsStones = new ArrayList<>();
        int numberOfOpponentsStones = 3;
        for(int i = 0; i < numberOfOpponentsStones; i++){
            opponentsStones.add(new Stone());
        }
        List<Stone> myStones = new ArrayList<>();
        myStones.add(new Stone());

        Pit kalahaPit = Mockito.mock(Pit.class);
        Kalaha kalaha = new Kalaha(kalahaPit);

        Pit opponentPit = Mockito.mock(Pit.class);
        List<Pit> mockedPits = (ArrayList<Pit>)Mockito.mock(ArrayList.class);
        when(mockedPits.size()).thenReturn(5);
        when(players.get(0).getPits()).thenReturn(mockedPits);
        when(players.get(1).getPits()).thenReturn(mockedPits);
        when(mockedPits.indexOf(ArgumentMatchers.eq(myPit))).thenReturn(2);
        when(mockedPits.get(ArgumentMatchers.eq(5 - 2))).thenReturn(opponentPit);
        when(players.get(0).getKalaha()).thenReturn(kalaha);

        when(myPit.removeStones()).thenReturn(myStones);
        when(opponentPit.removeStones()).thenReturn(opponentsStones);

        List<Stone> expectedStones = new ArrayList<>();
        expectedStones.addAll(myStones);
        expectedStones.addAll(opponentsStones);

        captureOpponentsStones.execute(players.get(0), game);
        verify(myPit).removeStones();
        verify(opponentPit).removeStones();
        verify(kalahaPit).addStones(ArgumentMatchers.eq(expectedStones));
    }

    @Test
    public void takeNextTurnShouldUpdateGameStateNextPlayer(){
        Pit pit = Mockito.mock(Pit.class);
        TakeAnotherTurn takeAnotherTurn = new TakeAnotherTurn(pit);
        Player player = players.get(0);
        Game mockGame = Mockito.mock(Game.class);
        takeAnotherTurn.execute(player, mockGame);
        verify(mockGame).setNextPlayer(player);
    }
}
