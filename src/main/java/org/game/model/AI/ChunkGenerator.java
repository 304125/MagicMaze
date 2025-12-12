package org.game.model.AI;

public class ChunkGenerator {
    public static int countChunks(SearchPath searchPath){
        if(searchPath == null){
            return 0;
        }
        int nodes = searchPath.length();
        return nodes / 5; // assuming each chunk has 5 nodes
    }

    public static float estimateChunks(int totalActions){
        return (float) totalActions / 5; // assuming each chunk has 5 actions
    }
}
