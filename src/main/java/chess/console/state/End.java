package chess.console.state;

import java.util.List;

public class End implements State {

    @Override
    public boolean isEnd() {
        return true;
    }

    @Override
    public State run(List<String> inputs) {
        throw new IllegalStateException("게임이 종료되어 실행이 불가능합니다.");
    }
}
