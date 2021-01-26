package com.example.minesweeperAPI.services;

import java.util.Set;

import com.example.minesweeperAPI.models.Cell;
import com.example.minesweeperAPI.models.Game;
import com.example.minesweeperAPI.models.GameState;

public interface GameService {
	
	public Game create(int columns, int rows, int mines);
	
	public Set<Cell> start(int gameId, int col, int row);
	
	public Set<Cell> move(int gameId, int col, int row);
	
	public void pause(int gameId, long time);
	
	public void resume(int gameId);
	
	public void saveResult(int gameId, GameState gameState);	
}
