package pacman.game.internal;

import pacman.game.Constants;
import pacman.game.Game;

import java.util.*;

public class DepthFirstSearch {

    private N[] graph;

    public void createGraph(Node[] nodes) {
        graph = new N[nodes.length];

        //create graph
        for (int i = 0; i < nodes.length; i++)
            graph[i] = new N(nodes[i].nodeIndex);

        //add neighbours
        for (int i = 0; i < nodes.length; i++) {
            EnumMap<Constants.MOVE, Integer> neighbours = nodes[i].neighbourhood;
            Constants.MOVE[] moves = Constants.MOVE.values();

            for (int j = 0; j < moves.length; j++)
                if (neighbours.containsKey(moves[j]))
                    graph[i].adjacent.add(new E(graph[neighbours.get(moves[j])], moves[j], 1));
        }
    }

    public synchronized int[] computeDFSPath(int startIndex, int targetIndex, Constants.MOVE lastMoveMade) {
        System.out.println("RoOt: " + startIndex + "TaRgEt: " + targetIndex);
        N startNode = graph[startIndex];
        N targetNode = graph[targetIndex];

        if (startIndex == targetIndex) {

            return new int[]{startIndex, targetIndex};1
        }

        N target = dfs(startNode, targetNode, lastMoveMade, 1300);
        if(target != null) {
            System.out.println("------------------------------------------");
            System.out.println("FOUND TARGET: " + target.toString());
            System.out.println("------------------------------------------");
            return extractPath(target);
        } else{
            return new int[]{startIndex, startIndex -1};
        }

    }

    private synchronized int[] extractPath(N target) {
        ArrayList<Integer> route = new ArrayList<Integer>();
        N current = target;
        route.add(current.index);

        while (current.parent != null) {
            int parentIndex = current.parent.index;
            route.add(parentIndex);
            current = current.parent;
        }

        Collections.reverse(route);

        int[] routeArray = new int[route.size()];

        for (int i = 0; i < routeArray.length; i++)
            routeArray[i] = route.get(i);
        return routeArray;
    }

    public synchronized N dfs(N root, N target, Constants.MOVE lastMoveMade, int max) {

        if (root.isEqual(target)) {
            return root;
        } else {
            root.reached = lastMoveMade;

            for (E child : root.adjacent) {
                if (child.move != root.reached.opposite()) {
                    child.node.reached = child.move;
                    if (child.node != root) {
                        child.node.parent = root;
                        if (max > 0) {
                            return dfs(child.node, target, child.move, --max);
                        }
                    }
                }
            }
        }

        return null;
    }
}
