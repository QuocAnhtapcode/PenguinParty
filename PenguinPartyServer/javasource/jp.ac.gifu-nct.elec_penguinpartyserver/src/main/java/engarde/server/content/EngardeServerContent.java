/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package engarde.server.content;

import penguin.game.PenguinPartyGame;
import java.util.ArrayList;
import java.util.HashMap;
import penguin.game.PenguinCard;

/**
 *
 * @author ktajima
 */
public class EngardeServerContent {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        long seed = Long.parseLong("7632102757062152144");
        PenguinPartyGame game = new PenguinPartyGame(seed);
        game.PlayerConnected("test1");
        game.PlayerConnected("test2");
        printGameState(game);
        
        //デモンストレーション　１手目：プレイヤー０がおく
        game.setEvaluationValues(0, new HashMap<String,Double>());
        System.out.println(game.checkPlaceableCard(0, "2-7", "purple"));
        System.out.println(game.checkPlaceableCard(0, "1-6", "purple"));
        System.out.println(game.checkPlaceableCard(0, "1-7", "purple"));
        game.placeACard(0, "1-7", "purple");
        printGameState(game);
        
        //２手目
        game.setEvaluationValues(1, new HashMap<String,Double>());
        System.out.println(game.checkPlaceableCard(1, "2-7", "blue"));
        System.out.println(game.checkPlaceableCard(1, "1-6", "blue"));
        System.out.println(game.checkPlaceableCard(1, "1-7", "blue"));
        System.out.println(game.checkPlaceableCard(0, "1-6", "blue"));
        System.out.println(game.checkPlaceableCard(0, "1-7", "blue"));
        game.placeACard(1, "1-6", "blue");
        printGameState(game);
        
    }

    
    public static void printGameState(PenguinPartyGame game){
        System.out.println("-----");
        System.out.println("GameStatus: " + game.getGameStatus());
        printCardListAll(game);
        printPyramid(game);
        System.out.println("-----");
        
    }
    
    public static void printCardListAll(PenguinPartyGame game){
        System.out.print("player0:");
        printCardList(game.getHandofPlayer(0));
        System.out.print("player1:");
        printCardList(game.getHandofPlayer(1));
    }
    
    public static void printPyramid(PenguinPartyGame game){
        game.getPyramid().printPyramid();
        System.out.println(game.getPyramid().getPlaceAblePlaces());
        int[] scores = game.getPlayerScores();
        System.out.println("Players score:"+scores[0]+ " vs " + scores[1]);
        
    }
    
    public static void printCardList(ArrayList<PenguinCard> hands){
        System.out.print("[");
        for(PenguinCard i : hands){
            System.out.print(i.getColor());
            System.out.print(" ");
        }
        System.out.println("]");
    }
    
    
}
