/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package penguin.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 *
 * @author ktaji
 */
public class PenguinCard {
    public static String Color_Green = "green";
    public static String Color_Red = "red";
    public static String Color_Blue = "blue";
    public static String Color_Yellow = "yellow";
    public static String Color_Purple = "purple";
    public static String[] colorNames = {Color_Green,Color_Red,Color_Blue,Color_Yellow,Color_Purple};
    
    private String mycolor;
    public String getColor(){
        return this.mycolor;
    }
    
    public PenguinCard(int i){
        if(i<0) {
            throw new IllegalStateException("Util instance construction is not allowed.");
        }
        if(i>4) {
            throw new IllegalStateException("Util instance construction is not allowed.");
        }
        this.mycolor = colorNames[i];
    }
    
    @Override
    public String toString(){
        return this.mycolor;
    }
    @Override
    public boolean equals(Object o){
        if(o instanceof PenguinCard){
            PenguinCard o1 = (PenguinCard)o;
            return this.mycolor.equals(o1.getColor());
        } else {
            return false;
        }
    }
    
    public static ArrayList<PenguinCard> makeCards(String color, int count){
        ArrayList<PenguinCard> list = new ArrayList<>();
        int colorid = -1;
        for(int i=0;i<5;i++){
            if(colorNames[i].equals(color)){
                colorid = i;
            }
        }
        if(colorid != -1){
            for(int i=0;i<count;i++){
                PenguinCard card = new PenguinCard(colorid);
                list.add(card);
            }
        }
        return list;
    }
    
    public static ArrayList<PenguinCard> makeCardDeck(Random rand){
        ArrayList<PenguinCard> list = new ArrayList<>();
        list.addAll(makeCards(Color_Green,8));
        list.addAll(makeCards(Color_Red,7));
        list.addAll(makeCards(Color_Blue,7));
        list.addAll(makeCards(Color_Yellow,7));
        list.addAll(makeCards(Color_Purple,7));
                
        Collections.shuffle(list, rand);
        return list;
    }
    
    
            
}
