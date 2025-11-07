package org.game.model.AI;

public class ChunkGenerator {

    public static int generateChunkSize(){
        int startingPoint = 7;
        // generate a random number between -2 and 2 inclusive
        int randomOffset = (int)(Math.random() * 5) - 2;
        return startingPoint + randomOffset;
    }

    public static int countChunks(ActionTree actionTree){
        int edges = actionTree.getTotalNumberOfEdges();
        return edges / 5; // assuming each chunk has 5 edges
    }

    public static int countChunks(SearchPath searchPath){
        int nodes = searchPath.length();
        return nodes / 5; // assuming each chunk has 5 nodes
    }
}
