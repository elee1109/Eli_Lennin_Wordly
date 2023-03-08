package net.fandm.eli_lenin.wordly;
import java.util.*;

/**
 * Graph class for Wordly. Made with ChatGPT3
 */
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

    public ArrayList<String> shortestPath(String start, String end) {
        if (!wordList.containsKey(start)) {
            addVertex(start);
        }
        if (!wordList.containsKey(end)) {
            addVertex(end);
        }
        Map<String, String> prev = new HashMap<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(start);
        prev.put(start, null);
        //this needs to account for words that are not in the list
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

        ArrayList<String> path = new ArrayList<>();
        String current = end;
        while (current != null) {
            path.add(0, current);
            current = prev.get(current);
        }

        return path;
    }

    public void buildGraph(ArrayList<String> words) {
        for (String word : words) {
            addVertex(word);
        }
        for (String word : words) {
            for (String otherWord : words) {
                if (word.equals(otherWord)) {
                    continue;
                }
                if (isOneLetterOff(word, otherWord)) {
                    addEdge(word, otherWord);
                }
            }
        }
    }

    private boolean isOneLetterOff(String word, String otherWord) {
        if (word.length() != otherWord.length()) {
            return false;
        }
        int numDifferences = 0;
        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) != otherWord.charAt(i)) {
                numDifferences++;
            }
        }
        return numDifferences == 1;
    }
}
