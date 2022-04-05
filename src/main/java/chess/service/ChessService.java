package chess.service;

import chess.dao.BoardDao;
import chess.dao.PieceDao;
import chess.domain.board.Board;
import chess.domain.board.Score;
import chess.domain.board.generator.BasicBoardGenerator;
import chess.domain.board.generator.DatabaseLoadBoardGenerator;
import chess.domain.game.ChessGame;
import chess.domain.piece.Piece;
import chess.domain.piece.Team;
import chess.domain.position.Position;
import chess.dto.MoveRequestDto;
import chess.dto.PieceDto;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

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
        model.put("turn", chessGame.getTurn().name());
        return model;
    }

    public int createNewBoard(BoardDao boardDao, PieceDao pieceDao, ChessGame chessGame) {
        chessGame.start(new BasicBoardGenerator());
        int boardId = boardDao.createBoard(chessGame.getTurn());
        pieceDao.create(convertPieceDtos(chessGame.getBoard()), boardId);
        return boardId;
    }

    private List<PieceDto> convertPieceDtos(Board board) {
        return board.getValue()
                .entrySet()
                .stream()
                .map(entry -> new PieceDto(entry.getValue(), entry.getKey()))
                .collect(Collectors.toUnmodifiableList());
    }

    public Map<String, Object> move(ChessGame chessGame, PieceDao pieceDao, BoardDao boardDao, int boardId,
                                    MoveRequestDto moveRequestDto) {
        Map<String, Object> model = new HashMap<>();

        try {
            chessGame.move(moveRequestDto);
            pieceDao.updatePosition(moveRequestDto.getFrom(), moveRequestDto.getTo());
            pieceDao.delete(moveRequestDto.getFrom());
            boardDao.updateTurn(chessGame.getTurn(), boardId);
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
