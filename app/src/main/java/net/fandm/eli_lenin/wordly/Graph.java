package net.fandm.eli_lenin.wordly;
import java.util.*;

public class Graph {
    private Map<String, List<String>> wordList;

    public Graph() {
        wordList = new HashMap<>();
    }

    public void addVertex(String v) {
        if (!wordList.containsKey(v)) {
            wordList.put(v, new ArrayList<>());
        }
    }

    public void addEdge(String v1, String v2) {
        addVertex(v1);
        addVertex(v2);
        wordList.get(v1).add(v2);
        wordList.get(v2).add(v1); // undirected graph
    }

    public List<String> shortestPath(String start, String end) {
        Map<String, String> prev = new HashMap<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(start);
        prev.put(start, null);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(end)) {
                break;
            }
            for (String neighbor : wordList.get(current)) {
                if (!prev.containsKey(neighbor)) {
                    prev.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        if (!prev.containsKey(end)) {
            return null;
        }

        List<String> path = new ArrayList<>();
        String current = end;
        while (current != null) {
            path.add(0, current);
            current = prev.get(current);
        }

        return path;
    }
}
