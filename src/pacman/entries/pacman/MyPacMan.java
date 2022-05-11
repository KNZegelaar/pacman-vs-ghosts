package pacman.entries.pacman;

import pacman.controllers.Controller;
import pacman.game.Constants;
import pacman.game.Constants.MOVE;
import pacman.game.GameView;
import pacman.game.Game;
import pacman.game.internal.AStar;
import pacman.game.internal.DepthFirstSearch;
import pacman.game.internal.Dijkstra;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

/*
 * This is the class you need to modify for your entry. In particular, you need to
 * fill in the getAction() method. Any additional classes you write should either
 * be placed in this package or sub-packages (e.g., game.entries.pacman.mypackage).
 */
public class MyPacMan extends Controller<MOVE>
{
	private static final int MIN_DISTANCE=5;	//if a ghost is this close, run away

	public MOVE getMove(Game game, long timeDue)
	{
		//Current PacMan Location
		int current = game.getPacmanCurrentNodeIndex();

		//Check all available targets and go to the closest.
		ArrayList<Integer> targets = targetStrategy(game);
		int target = goForTheClosestTarget(game, targets, current);

		//if any non-edible ghost is too close (less than MIN_DISTANCE), run away
		MOVE runAway = runAwayWhenNonEdibleGhostIsNearby(game, current);
		if(runAway != null) return runAway;

		//find the nearest edible ghost and go after them
		MOVE goEatGhost = findTheNearestEdibleGhostAndGoAfterThem(game, current);
		if(goEatGhost != null) return goEatGhost;

		//AStar
		AStar aStar = new AStar();
		aStar.createGraph(game.getCurrentMaze().graph);
		int[] path = aStar.computePathsAStar(current,target, game);

		//Dijkstra
//		Dijkstra dijkstra = new Dijkstra();
//		dijkstra.createGraph(game.getCurrentMaze().graph);
//		int[] path = dijkstra.computePathsDijkstra(current, target, game);

//		//DFS
//		DepthFirstSearch dfs = new DepthFirstSearch();
//		dfs.createGraph(game.getCurrentMaze().graph);
//		int[] path = dfs.computeDFSPath(current, target, game.getPacmanLastMoveMade());

		System.out.print("TARGET: " + target + " --- ");
		System.out.print("CURRENT: " + current + " ");
		System.out.print("[");
		for (int i = 0; i < path.length; i++) {
			System.out.print(path[i] + ", ");
		}
		System.out.println("]");
		System.out.println("POSSIBLE MOVES:" + Arrays.toString(game.getPossibleMoves(game.getPacmanCurrentNodeIndex())));


		addTestingVisuals(game, path);
		return game.getNextMoveTowardsTarget(current, path[1], Constants.DM.EUCLID);
	}

	public ArrayList<Integer> targetStrategy(Game game){
		//Get all indexes of Pills and Power pills
		int[] pills = game.getPillIndices();
		int[] powerPills = game.getPowerPillIndices();

		ArrayList<Integer> targets=new ArrayList<Integer>();

		if(goAfterPowerPillStrategy(game)) {							   	//check if a power pill is active
			for (int i = 0; i < powerPills.length; i++)            	//check which power pills are available
				if (game.isPowerPillStillAvailable(i))
					targets.add(powerPills[i]);
		} else {
			for(int i=0;i<pills.length;i++)								//check which pills are available
				if(game.isPillStillAvailable(i))
					targets.add(pills[i]);
		}

		return targets;
	}

	public int goForTheClosestTarget(Game game, ArrayList<Integer> targets, int currentPacManLocation){
		Constants.DM PATHTYPE = Constants.DM.PATH;
		int target = targets.get(0);

		double shortestDistance = game.getDistance(currentPacManLocation, target, PATHTYPE);
//		System.out.print("CURRENT LOCATION: " + currentPacManLocation + " [");

		for (int index : targets) {
			double distance = game.getDistance(currentPacManLocation, index, PATHTYPE);
//			System.out.print("{" + index + ", " + distance + "}");
			if (distance < shortestDistance) {
				shortestDistance = distance;
				target = index;
			}
		}
//		System.out.println("]");
		return target;


//		int target = targets.get(0);
//		System.out.print("CURRENT LOCATION: " + currentPacManLocation + " [");
//		for (int index:targets) {
//			System.out.print(index + ", ");
//			if(currentPacManLocation - index <= currentPacManLocation - target){
//				target = index;
//			}
//		}
//
//		System.out.println("]");
//
//		return target;
	}

	public Boolean goAfterPowerPillStrategy(Game game){
		return !game.isGhostEdible(Constants.GHOST.BLINKY)&&!game.isGhostEdible(Constants.GHOST.INKY)&&!game.isGhostEdible(Constants.GHOST.PINKY)&&!game.isGhostEdible(Constants.GHOST.SUE) && 	game.getGhostLastMoveMade(Constants.GHOST.SUE) != MOVE.NEUTRAL && game.getActivePowerPillsIndices().length > 0;
	}

	public MOVE runAwayWhenNonEdibleGhostIsNearby(Game game, int currentPacManLocation){
		for(Constants.GHOST ghost : Constants.GHOST.values()) {
			if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0)
				if(game.getShortestPathDistance(currentPacManLocation,game.getGhostCurrentNodeIndex(ghost))<MIN_DISTANCE)
					return game.getNextMoveAwayFromTarget(currentPacManLocation,game.getGhostCurrentNodeIndex(ghost), Constants.DM.EUCLID);
		}
		return null;
	}

	public MOVE findTheNearestEdibleGhostAndGoAfterThem(Game game, int currentPacManLocation){
		int minDistance=Integer.MAX_VALUE;
		Constants.GHOST minGhost=null;

		for(Constants.GHOST ghost : Constants.GHOST.values())
			if(game.getGhostEdibleTime(ghost)>0)
			{
				int distance=game.getShortestPathDistance(currentPacManLocation,game.getGhostCurrentNodeIndex(ghost));

				if(distance<minDistance)
				{
					minDistance=distance;
					minGhost=ghost;
				}
			}

		if(minGhost!=null)	//we found an edible ghost
			return game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(minGhost), Constants.DM.PATH);

		return null;
	}

	public void addTestingVisuals(Game game, int[] path ){
		//add the path that Ms Pac-Man is following
		GameView.addPoints(game, Color.GREEN,game.getShortestPath(game.getPacmanCurrentNodeIndex(),path[path.length -1]));
	}
}
