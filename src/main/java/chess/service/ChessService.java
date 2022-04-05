package chess.service;

import chess.dao.PieceDao;
import chess.domain.board.Board;
import chess.domain.board.Score;
import chess.domain.board.generator.BoardGenerator;
import chess.domain.board.generator.DatabaseLoadBoardGenerator;
import chess.domain.game.ChessGame;
import chess.domain.piece.Piece;
import chess.domain.piece.Team;
import chess.domain.position.Position;
import chess.dto.PieceDto;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ChessService {

    public Map<String, Object> findBoardModel(ChessGame chessGame, PieceDao pieceDao, int boardId) {
        List<PieceDto> pieces = pieceDao.findByBoardId(boardId);
        chessGame.start(new DatabaseLoadBoardGenerator(pieces));

        return generateBoardModel(chessGame);
    }

    private Map<String, Object> generateBoardModel(ChessGame chessGame) {
        Board board = chessGame.getBoard();

        Map<String, Object> model = new HashMap<>();
        for (Entry<Position, Piece> entry : board.getValue().entrySet()) {
            model.put(entry.getKey().getName()
                    , new PieceDto(entry.getValue(), entry.getKey()));
        }
        model.put("turn", chessGame.getTurn());
        return model;
    }

    public int createNewBoard(BoardGenerator boardGenerator, ChessGame chessGame) {
        chessGame.start(boardGenerator);
        return 1; // TODO
    }

    public Map<String, Object> move(ChessGame chessGame, PieceDao pieceDao, List<String> inputs) {
        Map<String, Object> model = new HashMap<>();

        try {
            chessGame.move(inputs);
            pieceDao.updatePosition(inputs.get(1), inputs.get(2));
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.put("message", e.getMessage());
        }
        model.putAll(generateBoardModel(chessGame));

        return model;
    }

    public Map<String, Object> calculateScore(ChessGame chessGame) {
        Map<String, Object> model = new HashMap<>();

        try {
            Score score = chessGame.status();
            model = generateBoardModel(chessGame);
            model.put("message", drawScoreSentence(score));

        } catch (IllegalArgumentException | IllegalStateException e) {
            model.put("message", e.getMessage());
        }

        return model;
    }

    private String drawScoreSentence(Score score) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Entry<Team, Double> entry : score.getValue().entrySet()) {
            stringBuilder.append(entry.getKey().name())
                    .append(": ")
                    .append(entry.getValue())
                    .append(" / ");
        }
        stringBuilder.append(drawWinner(score.findWinTeam()));

        return stringBuilder.toString();
    }

    private String drawWinner(Team team) {
        if (team == null) {
            return "무승부";
        }
        return String.format("승리 팀: %s", team);
    }

    public void end(ChessGame chessGame) {
        chessGame.end();
    }
}
