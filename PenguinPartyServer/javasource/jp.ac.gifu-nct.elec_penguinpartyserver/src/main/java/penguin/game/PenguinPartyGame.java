/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package penguin.game;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.Stack;

/**
 *
 * @author ktajima
 */
public class PenguinPartyGame {
    /** 勝利までのポイント数 */
    public static int defalutGamePlayCount = 100;

    /** プレイヤー情報 */
    private PenguinPlayer[] player = new PenguinPlayer[2];
    private String[] playerNames = new String[2];
    private PenguinPyramid pyramid;
    private ArrayList<PenguinCard> deck;
    private int currentStartPlayer;
    private int currentPlayer;
    private int gameCount = 0;
    private Random rand;
    private int winner = -1;
    
    private int gameStatus;
    
    public static final int GAMESTATUS_INIT                 = 0;
//    public static final int GAMESTATUS_CONNECTING           = 1;
    public static final int GAMESTATUS_PLAYERWAITING_0      = 10;
    public static final int GAMESTATUS_PLAYERWAITING_1      = 11;


    public static final int GAMESTATUS_INGAME_PLAYER0_PLAY  = 20;
    public static final int GAMESTATUS_INGAME_PLAYER1_PLAY  = 21;
    public static final int GAMESTATUS_INGAME_PLAYER0_EVALUATED = 22;
    public static final int GAMESTATUS_INGAME_PLAYER1_EVALUATED = 23;
    
    public static final int GAMESTATUS_CALC_SCORE           = 4;
    public static final int GAMESTATUS_ROUND_END            = 41;
    public static final int GAMESTATUS_GAMEEND              = 5;
    
    private PropertyChangeListener listner;

    //GUI等への通知用
    private PropertyChangeSupport changes;
     /**
     * リスナの追加を行います.
     * @param l 追加するリスナ
     */
    public void addPropertyChangeListener(PropertyChangeListener l){
        changes.addPropertyChangeListener(l);
    }
    
    /**
     * リスナの削除を行います.
     * @param l 削除するリスナ
     */
    public void removePropertyChangeListener(PropertyChangeListener l){
        changes.removePropertyChangeListener(l);
    }
    
    
    public PenguinPartyGame(){
        Date dt = new Date();
        Random r = new Random(dt.getTime());
        long seed = r.nextLong();
        System.out.println("seed is "+ seed);
        this.init(seed);
    }
    
    public PenguinPartyGame(long seed){
        this.init(seed);
    }
    
    private void init(long seed){
        this.rand = new Random(seed);
        this.changes = new PropertyChangeSupport(this);
        
        this.pyramid = new PenguinPyramid();
        this.deck = PenguinCard.makeCardDeck(this.rand);
        // 最初の8枚すてる
        for(int i=0;i<8;i++){
            this.deck.remove(0);
        }
        
        this.gameStatusChange(GAMESTATUS_PLAYERWAITING_0);
        
    }
    
    private void initPlayers(){
        this.player[0] = new PenguinPlayer();
        this.player[1] = new PenguinPlayer();

        ArrayList<PenguinCard> deck0 = new ArrayList<PenguinCard>();
        ArrayList<PenguinCard> deck1 = new ArrayList<PenguinCard>();
        // 次の14枚は配る
        for(int i=0;i<14;i++){
            deck0.add(this.deck.get(0));
            this.deck.remove(0);
        }
        for(int i=0;i<14;i++){
            deck1.add(this.deck.get(0));
            this.deck.remove(0);
        }

        this.player[0].setCardList(deck0);
        this.player[1].setCardList(deck1);
        
        
        this.currentStartPlayer = 0;
        this.currentPlayer = this.currentStartPlayer;
        this.gameStatusChange(GAMESTATUS_INGAME_PLAYER0_PLAY);
        
    }
    
    public boolean isState(int state){
        return this.gameStatus == state;
    }
    
    public boolean PlayerConnected(String playerName){
        if(this.gameStatus == GAMESTATUS_PLAYERWAITING_0){
            this.playerNames[0] = playerName;
            this.player[0] = new PenguinPlayer();
            this.gameStatusChange(GAMESTATUS_PLAYERWAITING_1);
            return true;
        } else if(this.gameStatus == GAMESTATUS_PLAYERWAITING_1){
            this.playerNames[1] = playerName;
            this.initPlayers();
            return true;
        }
        return false;
    }
    public int getCurrentPlayer(){
        return this.currentPlayer;
    }    
    public void startNewRound(){
        this.pyramid = new PenguinPyramid();

        this.deck = PenguinCard.makeCardDeck(this.rand);
        // 最初の8枚すてる
        for(int i=0;i<8;i++){
            this.deck.remove(0);
        }
        
        this.player[0].clearCards();
        this.player[1].clearCards();
        
        ArrayList<PenguinCard> deck0 = new ArrayList<PenguinCard>();
        ArrayList<PenguinCard> deck1 = new ArrayList<PenguinCard>();
        // 次の14枚は配る
        for(int i=0;i<14;i++){
            deck0.add(this.deck.get(0));
            this.deck.remove(0);
        }
        for(int i=0;i<14;i++){
            deck1.add(this.deck.get(0));
            this.deck.remove(0);
        }

        this.player[0].setCardList(deck0);
        this.player[1].setCardList(deck1);

        if(this.currentStartPlayer == 0){
            this.currentStartPlayer = 1;
            this.currentPlayer = this.currentStartPlayer;
            this.gameStatusChange(GAMESTATUS_INGAME_PLAYER1_PLAY);
        } else {
            this.currentStartPlayer = 0;
            this.currentPlayer = this.currentStartPlayer;
            this.gameStatusChange(GAMESTATUS_INGAME_PLAYER0_PLAY);
        }        
        this.winner = -1;
    }
    
    public ArrayList<PenguinCard> getHandofPlayer(int playerID){
        return this.player[playerID].getCardList();
    }
    
    public HashMap<String, String> getPyramidInformation(){
        return this.pyramid.getPyramidInformation();
    }
    public PenguinPyramid getPyramid(){
        return this.pyramid;
    }

    public int[] getPlayerScores(){
        //トラックの情報はリセット時に書き直すのでこちらからとらないと正しくない
        int[] socres = new int[2];
        socres[0] = this.player[0].playerScore;
        socres[1] = this.player[1].playerScore;
        return socres;
    }
    
    public int getGameStatus(){
        return this.gameStatus;
    }
    
    public boolean placeACard(int playerID,String position,String color){
        if(!checkPlayeAblePlayer(playerID)){
            return false;
        }
        if(!this.pyramid.isPlaceAble(position, color)){
            return false;
        }
        this.pyramid.setCardonPyramid(position, color);
        this.player[playerID].playACard(color);

        //手番の切り替え
        this.playerChangeProcess(playerID);

        return true;
    }
    
    private void playerChangeProcess(int playerID){
        //手番の切り替え
        if(this.isPlayAble(this.getOppnentID(playerID))){
            //相手が置ける
            this.currentPlayer = this.getOppnentID(playerID);
            if(playerID == 0){
                this.gameStatusChange(GAMESTATUS_INGAME_PLAYER1_PLAY);
            } else if(playerID == 1){
                this.gameStatusChange(GAMESTATUS_INGAME_PLAYER0_PLAY);
            }
        } else if(this.isPlayAble(playerID)){
            //自分がまだ置けるなら自分の番を続ける
            this.currentPlayer = playerID;
            if(playerID == 0){
                this.gameStatusChange(GAMESTATUS_INGAME_PLAYER0_PLAY);
            } else if(playerID == 1){
                this.gameStatusChange(GAMESTATUS_INGAME_PLAYER1_PLAY);
            }
        } else {
            //どちらも置けなくなったらラウンド終了処理に入る
            this.determinateProcess();
        }
    }
    
    /** どちらも置けなくなった時の処理 */
    private void determinateProcess(){
        this.gameStatusChange(GAMESTATUS_CALC_SCORE);
        //残ったカードをペナルティとして加算
        this.player[0].addScore(this.player[0].getUnusedCardsCount());
        this.player[1].addScore(this.player[1].getUnusedCardsCount());
        //０の場合はマイナス２点
        if(this.player[0].getUnusedCardsCount() == 0){
            this.player[0].addScore(-2);
        }
        if(this.player[1].getUnusedCardsCount() == 0){
            this.player[1].addScore(-2);
        }
        if(this.player[0].getUnusedCardsCount() < this.player[1].getUnusedCardsCount()){
            this.winner = 0;
        } else if(this.player[0].getUnusedCardsCount() > this.player[1].getUnusedCardsCount()){
            this.winner = 1;
        } else {
            this.winner = -1;
        }
        //指定した試合数だけ実施する。
        this.gameCount++;
        // 旧仕様 if(this.player[playerID].getScore() >= defalutVictoryPointSize){
        if(this.gameCount >= defalutGamePlayCount){
            //ゲーム終了
            this.gameStatusChange(GAMESTATUS_GAMEEND);
        } else {
            //次のラウンドへ移る
            this.gameStatusChange(GAMESTATUS_ROUND_END);
            this.startNewRound();
        }
    }
    
    public int[] getWinner(){
        int[] info = new int[3];
        info[2] = this.winner;
        info[0] = this.getPlayerScores()[0];
        info[1] = this.getPlayerScores()[1];
        return info;
    }
    
    
    /** 指定したプレイヤーがカードを置けるか判定する */
    private boolean isPlayAble(int playerID){
        ArrayList<String> placeList = this.pyramid.getPlaceAblePlaces();
        ArrayList<PenguinCard> cardList = this.player[playerID].getCardList();
        for(PenguinCard cardColor:cardList){
            for(String placeID:placeList){
                if(this.pyramid.isPlaceAble(placeID, cardColor.getColor())){
                    return true;
                }
            }
        }
        return false;
    }
    
    private int getOppnentID(int playerID){
        return (playerID+1)%2;
    }
    
    public HashMap<String,Double> getEvaluationValues(int playerID){
        return this.player[playerID].getEvaluetionValueTable();
    }
    public boolean setEvaluationValues(int playerID,HashMap<String,Double> evalue){
        if(playerID == 0){
            if(this.gameStatus == GAMESTATUS_INGAME_PLAYER0_PLAY){
                this.player[0].setEvalationValues(evalue);
                this.gameStatusChange(GAMESTATUS_INGAME_PLAYER0_EVALUATED);
                return true;
            }
            return false;
        } else if (playerID == 1){
            if(this.gameStatus == GAMESTATUS_INGAME_PLAYER1_PLAY){
                this.player[1].setEvalationValues(evalue);
                this.gameStatusChange(GAMESTATUS_INGAME_PLAYER1_EVALUATED);
                return true;
            }
            return false;
        }
        return false;
        
    }
    
    public boolean checkPlayeAblePlayer(int playerID){
        if(playerID == 0){
            if(this.gameStatus == GAMESTATUS_INGAME_PLAYER0_EVALUATED){
                return true;
            }
            return false;
        } else if (playerID == 1){
            if(this.gameStatus == GAMESTATUS_INGAME_PLAYER1_EVALUATED){
                return true;
            }
            return false;
        }
        return false;
    }
    
    public boolean checkPlaceableCard(int playerID,String position,String color){
        if(!checkPlayeAblePlayer(playerID)){
            return false;
        }
        if(!checkPlayableCards(playerID, color)){
            return false;
        }
        return this.pyramid.isPlaceAble(position, color);
    }
    
    public boolean checkPlayableCards(int playerID,String color){
        return this.player[playerID].checkPlayableCards(color);
    }
    
    /** イベントを発火させるために状態チェンジは必ずこのメソッドで呼び出すこと */
    private void gameStatusChange(int status){
        int backState = this.gameStatus;
        this.gameStatus = status;
        this.changes.firePropertyChange("GAME_STATE_CHANGED", backState, this.gameStatus);
    }
    
    /* ゲーム状態の出力メソッド */
    public static void printGameState(PenguinPartyGame game,PrintStream ps){
        ps.println("-----");
        
        ps.println("GameStatus: " + game.getGameStatus());
        
        printCardListAll(game,ps);
        printPyramidStatus(game,ps);
        
        ps.println("-----");
        
    }
    
    public static void printCardListAll(PenguinPartyGame game,PrintStream ps){
        ps.print("player0:");
        printCardList(game.getHandofPlayer(0),ps);
        ps.print("player1:");
        printCardList(game.getHandofPlayer(1),ps);
    }
    
    public static void printPyramidStatus(PenguinPartyGame game,PrintStream ps){
        ps.println(game.getPyramid().toString());
        int[] scores = game.getPlayerScores();
        ps.println("Players score:"+scores[0]+ " vs " + scores[1]);
        
    }
    
    public static void printCardList(ArrayList<PenguinCard> hands,PrintStream ps){
        ps.print("[");
        for(PenguinCard color : hands){
            ps.print(color.getColor());
            ps.print(" ");
        }
        ps.println("]");
    }
}
