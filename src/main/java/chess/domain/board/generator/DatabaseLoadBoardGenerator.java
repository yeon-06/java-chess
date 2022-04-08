package chess.domain.board.generator;

import chess.domain.board.Board;
import chess.domain.piece.Piece;
import chess.domain.piece.PieceFactory;
import chess.domain.piece.Team;
import chess.domain.position.Position;
import chess.dto.PieceDto;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseLoadBoardGenerator implements BoardGenerator {

    private final List<PieceDto> pieceDtos;
    private final Team turn;

    public DatabaseLoadBoardGenerator(List<PieceDto> pieceDtos, Team turn) {
        this.pieceDtos = pieceDtos;
        this.turn = turn;
    }

    @Override
    public Board create() {
        Map<Position, Piece> board = new HashMap<>();
        for (PieceDto pieceDto : pieceDtos) {
            board.put(Position.of(pieceDto.getPosition()), createPiece(pieceDto));
        }
        return new Board(board, turn);
    }

    private Piece createPiece(PieceDto pieceDto) {
        return PieceFactory.of(pieceDto.getType(), Team.of(pieceDto.getTeam()));
    }
}
