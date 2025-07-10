/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package penguin.game;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * ２０２３ゲーム　アンギャルド　それぞれのプレイヤーが持つ情報
 * @author ktajima
 */
public class PenguinPlayer {
    /** プレイヤーの点数 */
    protected int playerScore;
    /** 保持しているカード */
    private ArrayList<PenguinCard> playerHand;
    /** 各カードの評価値（内部保持情報） */
    private HashMap<String,Double> evaluationValues;
    
    public PenguinPlayer(){
        this.playerHand = new ArrayList<>();
        this.evaluationValues = new HashMap<>();
    }
    public HashMap<String,Double> getEvaluetionValueTable(){
        return this.evaluationValues;
    }
    public int getNumberofCards(){
        return this.playerHand.size();
    }
    public int getNumberofCards(String color){
        int count = 0;
        for(PenguinCard cv:this.playerHand){
            if(cv.getColor().equals(color)){
                count++;
            }
        }
        return count;
    }
    
    public ArrayList<PenguinCard> getCardList(){
        return this.playerHand;
    }
    
    public int getScore(){
        return this.playerScore;
    }
    
    public int addScore(int point){
        this.playerScore += point;
        return this.playerScore;
    }
    
    public void clearCards(){
        this.playerHand.clear();
    }
    public void setCardList(ArrayList<PenguinCard> list){
        this.playerHand.clear();
        this.playerHand.addAll(list);
    }
    
    public boolean playACard(String cardValue){
        if(!checkPlayableCards(cardValue)){
            return false;
        }
        int index = -1;
        for(int i=0;i<this.playerHand.size();i++){
            PenguinCard p = this.playerHand.get(i);
            if(p.getColor().equals(cardValue)){
                index = i;
            }
        }
        if(index != -1){
            this.playerHand.remove(index);
            return true;
        } else {
            return false;
        }
    }
    
    public boolean checkPlayableCards(String color){
        int count = getNumberofCards(color);
        if(count > 0 ){
            return true;
        } else {
            return false;
        }
    }
    
    public int getUnusedCardsCount(){
        return this.playerHand.size();
    }
    
    public void setEvalationValues(HashMap<String,Double> input){
        for(String key:input.keySet()){
            this.evaluationValues.put(key, input.get(key));
        }
    }

    //第一引数はカードの色
    //第二引数はカードの評価値
    public void setEvalationValueOf(String cardColor, double value){
        this.evaluationValues.put(cardColor, value);
    }
    public double getEvaluetionValueOf(String cardColor){
        if(this.evaluationValues.containsKey(cardColor)){
            return this.evaluationValues.get(cardColor);
        }
        return 0.0;
    }
    
}
