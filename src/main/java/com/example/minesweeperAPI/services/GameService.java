package com.example.minesweeperAPI.services;

import java.util.Set;

import com.example.minesweeperAPI.models.Cell;
import com.example.minesweeperAPI.models.Game;
import com.example.minesweeperAPI.models.GameState;

public interface GameService {
	
	public Game start(int rows, int columns, int mines, int xFirstRevealed, int yFirstRevealed);
	
	public Set<Cell> uncoverCell(int gameId, int row, int col);
	
	public void pause(int gameId, long time);
	
	public void resume(int gameId);
	
	public void saveResult(int gameId, GameState gameState);
	
	
}
