package pacman.entries.pacman;

import pacman.controllers.Controller;
import pacman.game.Constants;
import pacman.game.Constants.MOVE;
import pacman.game.GameView;
import pacman.game.Game;
import pacman.game.internal.AStar;

import java.awt.*;
import java.util.ArrayList;

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
		int current=game.getPacmanCurrentNodeIndex();

		//Strategy 1: if any non-edible ghost is too close (less than MIN_DISTANCE), run away
		for(Constants.GHOST ghost : Constants.GHOST.values())
			if(game.getGhostEdibleTime(ghost)==0 && game.getGhostLairTime(ghost)==0)
				if(game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost))<MIN_DISTANCE)
					return game.getNextMoveAwayFromTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(ghost), Constants.DM.PATH);

		//Get all indexes of Pills and Power pills
		int[] pills = game.getPillIndices();
		int[] powerPills = game.getPowerPillIndices();

		//Strategy 3: go after the pills and power pills
		ArrayList<Integer> targets=new ArrayList<Integer>();

		//Strategy 2: find the nearest edible ghost and go after them
		int minDistance=Integer.MAX_VALUE;
		Constants.GHOST minGhost=null;

		for(Constants.GHOST ghost : Constants.GHOST.values())
			if(game.getGhostEdibleTime(ghost)>0)
			{
				int distance=game.getShortestPathDistance(current,game.getGhostCurrentNodeIndex(ghost));

				if(distance<minDistance)
				{
					minDistance=distance;
					minGhost=ghost;
				}
			}

		if(minGhost!=null)	//we found an edible ghost
			return game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(minGhost), Constants.DM.PATH);


		if(!game.isGhostEdible(Constants.GHOST.BLINKY)&&!game.isGhostEdible(Constants.GHOST.INKY)&&!game.isGhostEdible(Constants.GHOST.PINKY)&&!game.isGhostEdible(Constants.GHOST.SUE) && 	game.getGhostLastMoveMade(Constants.GHOST.SUE) != MOVE.NEUTRAL && game.getNumberOfPowerPills() > 0) {							   	//check if a power pill is active
			for (int i = 0; i < powerPills.length; i++)            	//check with power pills are available
				if (game.isPowerPillStillAvailable(i))
					targets.add(powerPills[i]);
		} else {
			for(int i=0;i<pills.length;i++)								//check which pills are available
				if(game.isPillStillAvailable(i))
					targets.add(pills[i]);
		}

		int[] targetsArray=new int[targets.size()];					//convert from ArrayList to array

		for(int i=0;i<targetsArray.length;i++)
			targetsArray[i]=targets.get(i);

		AStar aStar = new AStar();

		aStar.createGraph(game.getCurrentMaze().graph);


		int target = targetsArray[0];
		System.out.print("CURRENT: "+ current +" [");
		for (int index:targetsArray) {
			System.out.print(index + ", ");
			if(current - index <= current - target){
				target = index;
			}
		}
		System.out.println("]");
		System.out.println("TARGET: " + target);

		int[] path = aStar.computePathsAStar(current,target, game);

		//add the path that Ms Pac-Man is following
		GameView.addPoints(game, Color.GREEN,game.getShortestPath(game.getPacmanCurrentNodeIndex(),path[path.length -1]));
		return game.getNextMoveTowardsTarget(current, path[1], Constants.DM.EUCLID);
	}
}
