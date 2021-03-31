package chess.domain.state;

import chess.domain.piece.Color;

public class Init implements State {

    @Override
    public Color color() {
        throw new IllegalArgumentException();
    }

    @Override
    public State opposite() {
        throw new IllegalArgumentException();
    }

    @Override
    public State end() {
        return new Finished();
    }

    @Override
    public State status() {
        throw new IllegalArgumentException();
    }

    @Override
    public boolean isEnd() {
        return false;
    }

    @Override
    public State start() {
        return new BlackTurn();
    }
}
