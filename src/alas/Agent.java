package alas;

import java.util.ArrayList;
import java.util.Random;

public class Agent {

	// This class implements the agent
	
	public enum Direction {UP, RIGHT, DOWN, LEFT}; // the four directions
	public enum Mode {EXPLORE, GO_BACK};		// the two modes of question 2
	
	private Mode mode;
	
	// The grid is 6x6 during training and 4x4 during testing
	
	private int[][] grid;		// The grid where the agent moves. grid[row][column] stores the 
									// id of the static agent of that tile. Rows are incremented bottom-top
	private int max;			// this is simply grid.length-1
		
	private boolean[][] inVisited;	// 2D boolean array with the same dimensions and indicates
									// if a tile has been visited during a foray
	
	private String [][] allStaticAgents; 	// Static agents are stored like arrays of strings.
										// This array stores all the static agents read by
										// the 'table1.txt' file. Every time out agent visits
										// a tile, reads the attributes of the static agent from
										// this array
		
	private ArrayList<int[]> path; 	// the path the agent has followed during the current foray
	private ArrayList<Integer> collectedSA; //the static agents(SA) collected during the current foray
	private ArrayList<Integer> allKnownSA; //the static agents(SA) collected totally
	private ArrayList<Integer> energySequence; // the sequence of the energy values(was used only for testing)	
	
	private int row; // the current row
	private int col; // the current column
	private int energy;	// the current energy
	private int extraEnergy;	// this is used to determine when the agent should stop exploring and go back
	
	// intialization (Reset Action)
	// The constructor accepts a grid and an array with all the static agents(loaded from 'Table1.txt')
	// The total SA collection is initialized here. It will be used to store all the SA collected from
	// the subsequent forays (all known SA). Each time the agent returns from a successful foray,
	// the SA collected during the foray will be added in this list (allKnownSA) if they are not already there
	public Agent(int[][] gd, String [][] all_ags) {
		grid = gd;
		max = grid.length -1;	// just the maximum allowed grid dimension
		allStaticAgents = all_ags;	// the array with all the SA
		allKnownSA = new ArrayList<Integer>();	// total SA collection (stores the Ids)
		inVisited = new boolean[grid.length][grid.length];	// this is done every a foray starts actually,
															// but it is also done here
		energy = 22;
	}
	
	// This function prepares for a foray. It accepts the initial energy and the extra energy as parameters.
	// The 'collectedSA' list will store all the SA ids during the foray. Each SA is stored only
	// the first time a tile is visited, but the same SA will be stored twice if is present in two tiles.
	// The 'path' collection contains all the visited tiles as integer arrays [row, column] 
	// in the order they were visited.
	
	// The agent is placed at the SW corner (grid[0,0]) and the collections are updated accordingly.
	
	public void prepareForay(int initEng, int extraEng) {
		inVisited = new boolean[grid.length][grid.length]; //false initially
		energy = initEng;
		extraEnergy = extraEng;
		row = col = 0;
		path = new ArrayList<int[]>();
		collectedSA = new ArrayList<Integer>();
		mode = Mode.EXPLORE; // go into explore mode
		
		// add grid[0,0]
		path.add(new int[]{0,0});
		collectedSA.add(grid[0][0]);
		inVisited[0][0] = true;
		
	}
	
	// This function implements the choice of the next move. The following strategy is implemented in this function
	public void step() {

		Random rnd = new Random();
		
		ArrayList<Direction> dirs =  getAvailableMoves();	// get all the valid moves directions 
		
		if (mode == Mode.EXPLORE) {
			
			ArrayList<Direction> unvisitedDirs = getUnvisited(dirs);	// get only the unvisited ones
			
			
			int n = unvisitedDirs.size();
			if (n > 0) {									// if there unvisited directions
				move(unvisitedDirs.get(rnd.nextInt(n)));	// choose one at random	
			}
			else {											
				move(dirs.get(rnd.nextInt(dirs.size())));	// else choose a random valid direction
			}
						
			int SWCornerDistance = row + col + 1;		

			if (SWCornerDistance >= energy - extraEnergy) { //if (energy <= SWCornerDistance + extraEnergy) "CHANGE_MODE"
				mode = Mode.GO_BACK;
			}
		}
		else if (mode == Mode.GO_BACK) {
			
			//get only the LEFT, DOWN directions
			ArrayList<Direction> leftDownDirs = getSublistOfDirections(dirs, new Direction[]{Direction.LEFT, Direction.DOWN});
			
			// like before from here
			ArrayList<Direction> unvisitedDirs = getUnvisited(leftDownDirs);
			
			int n = unvisitedDirs.size();
			if (n > 0) {
				move(unvisitedDirs.get(rnd.nextInt(n)));
			}
			else {
				move(leftDownDirs.get(rnd.nextInt(leftDownDirs.size())));
			}
			
		}
		
	}
	

	// This function moves the agent to the tile indicated by the direction argument
	// No validity check is implemented here. The validity of the move is responsibility of the caller
	public boolean move(Direction d) {
		
		if (energy<1) return false;
		
		switch (d) {
			case UP:
				row+=1; break;
			case RIGHT:
				col+=1; break;
			case DOWN:
				row-=1; break;
			case LEFT:
				col-=1; break;
		}
		
		path.add(new int[]{row,col});	// the path is updated
		
		String [] staticAgent = allStaticAgents[grid[row][col]];	//the SA id is read and the related 
																	// attributes values are read by 
																	// the allStaticAgents array.
																	// (grid[row][col] contains the id of the SA)
																	
		// read the reward attribute 
		String rewardAttribute = staticAgent[AlasFrame.REWARD_INDEX];
		
		int reward;
		if (rewardAttribute.equals("N/A")) // if the REWARD value is unknown (test environment) predict it
			reward = AlasFrame.decisionTreeForReward(staticAgent);		// the prediction function is part of the AlasFrame class
		else		//this is for known values of REWARD
		  reward = Integer.valueOf(rewardAttribute);

		energy -= 1;		// this is the step cost, applied always
		
		// if the tile is visited for the first time collections are updated and extra energy change applied
		if (!inVisited[row][col]) {
			energy += reward; 		// this is the additional energy change by the reward attribute
			inVisited[row][col] = true;
			collectedSA.add(grid[row][col]);
		}
		
		//System.out.println("Move:\t(" + row + ", " + col + ")\t\tagent:\t" + grid[row][col] + "\treward:\t" + reward + "\tenergy:\t" + energy + "\tdirection:\t" + d);
		
		return true;
	}
	
	// checks the validity of a tile (if it is inside the bounds)
	public boolean isValidTile(int r, int c) {
		return (r>=0 && r<=max && c>=0 && c<=max);
	}
	
	// returns true if the tile has been visited
	public boolean hasVisited(int r, int c) {
		return inVisited[r][c];
	}
	
	// like above but it accepts a direction as argument
	public boolean hasVisited(Direction d) {
		switch (d) {
			case UP:	return hasVisited(row+1, col);
			case RIGHT:	return hasVisited(row, col+1);
			case DOWN:	return hasVisited(row-1, col);
			case LEFT:	return hasVisited(row, col-1);
			default: return false;
		}
	};
	
	// returns all the directions corresponding to valid moves 
	private ArrayList<Direction> getAvailableMoves() {
		ArrayList<Direction> dirs = new ArrayList<Direction>();
		if (isValidTile(row+1, col)) dirs.add(Direction.UP);
		if (isValidTile(row, col+1)) dirs.add(Direction.RIGHT);
		if (isValidTile(row-1, col)) dirs.add(Direction.DOWN);
		if (isValidTile(row, col-1)) dirs.add(Direction.LEFT);
		return dirs;
	}
	
	// get a sublist of a direction list containing only certain directions
	private ArrayList<Direction> getSublistOfDirections(ArrayList<Direction> dirs, Direction [] subList) {
		ArrayList<Direction> sl = new ArrayList<Direction>();
		for (Direction d: subList) {
			if (dirs.contains(d)) 	sl.add(d);
		}
		return sl;
	}
	
	// from a direction list get only the directions towards unvisited tiles
	private ArrayList<Direction> getUnvisited(ArrayList<Direction> dirs) {
		ArrayList<Direction> unv = new ArrayList<Direction>();
		for (Direction d : dirs) {
			if (!hasVisited(d)) unv.add(d);
		}
		return unv;
	}
	
	// this function is called when a successful foray ends to add any new SA found during the foray
	public void updateKnowledgeBase() {
		for (Integer staticAgentId : collectedSA) {
			if (!allKnownSA.contains(staticAgentId)) {
				allKnownSA.add(staticAgentId);
			}
		}
	}
	

	
	// *******************************************************************************
	// last question, route search
	public ArrayList<String> searchTestEnvironment(ArrayList<int[]> allPaths, int initialEnergy) {
		
		ArrayList<String> goodRoutes = new ArrayList<String>();
		
		for (int[] path: allPaths) {

			prepareForay(initialEnergy, 0);
			
			int i = 0; 
			
			while (energy>0 && i<path.length) {
				Direction dir = (path[i] == 1 ? Direction.RIGHT : Direction.UP);
				move(dir);
				i++;
			}
			
			if (energy>0) {	//successful route
				String routeString = "";
				
				for (int v: path) {
					routeString += " " + (v==0 ? "u" : "r");
				}
				
				goodRoutes.add(routeString);
			}
			
		}
		return goodRoutes;
	}

	// getters
	public int getRow() {return row;}
	public int getColumn() {return col;}
	public int getEnergy() {return energy;}
	public ArrayList<int[]> getPath() {return path;};
	public ArrayList<Integer> getCollectedSA() {return collectedSA;}
	public ArrayList<Integer> getEnergySequence() {return energySequence;}
	public ArrayList<Integer> getAllKnownSA() {return allKnownSA;}
	
}
