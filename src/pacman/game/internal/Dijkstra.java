package pacman.game.internal;

import pacman.game.Constants;
import pacman.game.Game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.PriorityQueue;

public class Dijkstra {
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

    public synchronized int[] computePathsDijkstra(int s, int t, Constants.MOVE lastMoveMade, Game game) {
        N start = graph[s];
        N target = graph[t];

        PriorityQueue<N> open = new PriorityQueue<N>();
        ArrayList<N> closed = new ArrayList<N>();

        start.reached = lastMoveMade;

        open.add(start);

        while (!open.isEmpty()) {
            N currentNode = open.poll();
            closed.add(currentNode);

            if (currentNode.isEqual(target))
                break;

            for (E next : currentNode.adjacent) {
                if (next.move != currentNode.reached.opposite()) {
                    double currentDistance = next.cost;

                    if (!open.contains(next.node) && !closed.contains(next.node)) {
                        next.node.pathCost = currentDistance + currentNode.pathCost;
                        next.node.parent = currentNode;

                        next.node.reached = next.move;

                        open.add(next.node);
                    }
                }
            }
        }

        return extractPath(target);
    }

    public synchronized int[] computePathsDijkstra(int s, int t, Game game) {
        return computePathsDijkstra(s, t, Constants.MOVE.NEUTRAL, game);
    }

    private synchronized int[] extractPath(N target) {
        ArrayList<Integer> route = new ArrayList<Integer>();
        N current = target;
        route.add(current.index);

        while (current.parent != null) {
            route.add(current.parent.index);
            current = current.parent;
        }

        Collections.reverse(route);

        int[] routeArray = new int[route.size()];

        for (int i = 0; i < routeArray.length; i++)
            routeArray[i] = route.get(i);

        return routeArray;
    }
}
