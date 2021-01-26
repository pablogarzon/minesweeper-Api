package com.example.minesweeperAPI.services.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.springframework.stereotype.Service;

import com.example.minesweeperAPI.models.Cell;
import com.example.minesweeperAPI.models.CellCoordinates;
import com.example.minesweeperAPI.models.CellState;
import com.example.minesweeperAPI.models.Game;
import com.example.minesweeperAPI.models.GameState;
import com.example.minesweeperAPI.repository.GameRepository;
import com.example.minesweeperAPI.services.GameService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {
	
	private final GameRepository repository;
	
	@Override
	public Game create(int columns, int rows, int mines) {
		if (mines > rows * columns) {
			// throw error
		}

		var game = Game.builder()
				.id(1) //generated id
				.rows(rows)
				.columns(columns)
				.mines(mines)
				.build();
		
		repository.save(game);
		
		return game;
	}
	
	@Override
	public Set<Cell> start(int gameId, int col, int row) {
		Game game = repository.findById(gameId).get();
		
		var board = createMinefield(game, col, row);
		countMinesAroundCell(board);
		
		var uncoveredCells = new HashSet<Cell>(); 
		uncoverCell(board, uncoveredCells, board[row][col]);
		game.incrementUncoveredCells(uncoveredCells.size());
		game.setBoard(board);
		repository.save(game);
		
		return uncoveredCells;
	}
	
	@Override
	public Set<Cell> move(int gameId, int col, int row) {
		var game = repository.findById(gameId).get();
		
		final var uncoveredCells = new HashSet<Cell>(); 
		uncoverCell(game.getBoard(), uncoveredCells, game.getBoard()[row][col]);
		game.incrementUncoveredCells(uncoveredCells.size());
		
		return uncoveredCells;
	}
	
	@Override
	public void pause(int gameId, long time) {
		Game game = repository.findById(gameId).get();
		game.setTime(time);
		game.setState(GameState.PAUSED);
		repository.save(game);
	}

	@Override
	public void resume(int gameId) {
		Game game = repository.findById(gameId).get();
		game.setState(GameState.ACTIVE);
		repository.save(game);		
	}

	@Override
	public void saveResult(int gameId, GameState gameState) {
		Game game = repository.findById(gameId).get();
		if (gameState.isFailed() || gameState.isVictory()) {
			game.endGame(gameState);
		} // else throw exceptions
	}

	private Cell[][] createMinefield(Game game, int xFirstRevealed, int yFirstRevealed) {
		int rows = game.getRows();
		int cols = game.getColumns();
		int mines = game.getMines();
		
		var board = new Cell[rows][cols];
		int y, x = 0;

		for (int i = 0; i < mines; i++) {
			do {
				y = (int) (Math.random() * (rows));
				x = (int) (Math.random() * (cols));
			} while (board[y][x] != null || (x == xFirstRevealed && y == yFirstRevealed));

			var cell = new Cell(new CellCoordinates(x, y), true);

			board[y][x] = cell;
		}
		
		return board;
	}

	private void countMinesAroundCell(Cell[][] board) {
		int columns = board[0].length;
		int rows = board.length;
		
		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < columns; x++) {

				if (board[y][x] != null && board[y][x].isHasMine()) {
					continue;
				}

				var cell = new Cell(new CellCoordinates(x, y), false);
				
				inspectAdjacentCells(board, cell, (currentCell) -> {
					if (currentCell != null && currentCell.isHasMine()) {
						cell.addMinesAround();
					}
				});				

				board[y][x] = cell;
			}
		}
	}
	
	private void uncoverCell(Cell[][] board, Set<Cell> uncoveredCells, Cell cell) {
		if (cell.getState().isUnCovered()) {
			return;
		}
		cell.setState(CellState.UNCOVERED);
		uncoveredCells.add(cell);
		
		if (cell.getValue() == 0 && !cell.isHasMine()) {
			inspectAdjacentCells(board, cell, (current) -> {
				uncoverCell(board, uncoveredCells, current);
			});
		}
	}

	private void inspectAdjacentCells(Cell[][] board, Cell current, Consumer<Cell> callback) {
		int columns = board[0].length;
		int rows = board.length;
		
		int x = current.getCoordinates().getX();
		int y = current.getCoordinates().getY();
		
		int up = y - 1;
		int down = y + 1;
		int left = x - 1;
		int right = x + 1;
		
		if (up >= 0 && left >= 0) {
			callback.accept(board[up][left]);
		}
		if (up >= 0) {
			callback.accept(board[up][x]);
		}
		if (up >= 0 && right <= columns - 1) {
			callback.accept(board[up][right]);
		}
		if (right <= columns - 1) {
			callback.accept(board[y][right]);
		}
		if (down <= rows - 1 && right <= columns - 1) {
			callback.accept(board[down][right]);
		}
		if (down <= rows - 1) {
			callback.accept(board[down][x]);
		}
		if (down <= rows - 1  && left >= 0) {
			callback.accept(board[down][left]);
		}
		if (left >= 0) {
			callback.accept(board[y][left]);
		}
	}
}
