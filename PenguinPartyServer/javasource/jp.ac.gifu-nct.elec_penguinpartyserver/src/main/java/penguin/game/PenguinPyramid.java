/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package penguin.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author ktaji
 */
public class PenguinPyramid {
    private HashMap<String,String> pyramid;
    public PenguinPyramid(){
        this.pyramid = new HashMap<>();
    }
    
    public boolean setCardonPyramid(String placeID, String cardColor){
        if(this.isPlaceAble(placeID, cardColor)){
            this.pyramid.put(placeID, cardColor);
            return true;
        } else {
            return false;
        }
    }
    
    public HashMap<String,String> getPyramidInformation(){
        return this.pyramid;
    }
    
    /** ピラミッドのどこに置けるかを返す
     * @return ArrayList<String> */
    public ArrayList<String> getPlaceAblePlaces(){
        ArrayList<String> list = new ArrayList<String>();
        //何も置いていない時
        if(!this.pyramid.containsKey("1-7")){
            list.add("1-7");
            return list;
        }
        //それ以外は順番に抽出する
        HashSet<String> set = new HashSet<>();
        //1段目
        for(int p=2;p<=12;p++){
            String cpos =  Integer.toString(1) + "-" + Integer.toString(p);
            String cpos_l =  Integer.toString(1) + "-" + Integer.toString(p-1);
            String cpos_r =  Integer.toString(1) + "-" + Integer.toString(p+1);
            if(this.pyramid.containsKey(cpos)){
                set.add(cpos_l);
                set.add(cpos_r);
            }
        }
        //2段目以降
        for(int line=2;line<=7;line++){
            for(int p=1;p<=13-line;p++){
                String cpos =  Integer.toString(line) + "-" + Integer.toString(p);
                String cpos_l =  Integer.toString(line-1) + "-" + Integer.toString(p);
                String cpos_r =  Integer.toString(line-1) + "-" + Integer.toString(p+1);
                if(this.pyramid.containsKey(cpos_l) && this.pyramid.containsKey(cpos_r) ){
                    set.add(cpos);
                }
            }
        }
        //リスト化
        for(String place:set){
            list.add(place);
        }
        return list;
    }
    
    public boolean isPlaceAble(String placeID, String cardColor){
        if(this.pyramid.containsKey("1-7")){
            //1-7が置いてあるときは次の順に処理して確認
            //置きたい場所にカードがある　→　置けない
            if(this.pyramid.containsKey(placeID)){
                return false;
            }
            //置きたい場所が1段目の場合は7枚以下かを調べる
            Pattern placeIDPattern = Pattern.compile("(\\d)-(\\d+)");
            Matcher mc = placeIDPattern.matcher(placeID);
            if(mc.matches()){
                int line = Integer.parseInt(mc.group(1));
                int position = Integer.parseInt(mc.group(2));
                if(line == 1){
                    int count_first_line = 0;
                    for(String keys:this.pyramid.keySet()){
                        if(keys.matches("1-(\\d+)")){
                            count_first_line++;
                        }
                    }
                    if(count_first_line >= 7){
                        return false;
                    }
                    //隣にカードがあればおける。なければ置けない。
                    if(position != 1){
                        String cpos = "1-" + Integer.toString(position-1);
                        if(this.pyramid.containsKey(cpos)){
                            return true;
                        }
                    }
                    if(position != 13){
                        String cpos = "1-" + Integer.toString(position+1);
                        if(this.pyramid.containsKey(cpos)){
                            return true;
                        }
                    }
                    //ここに来た場合は隣接カードがない
                    return false;
                } else {
                    //2段目以上の場合は下を調べる
                    int maxposition = 14 - line;
                    //下２枚にカードがあり、どちらかが同じ色ならばおける。なければ置けない。
                    String cpos0 =  Integer.toString(line-1) + "-" + Integer.toString(position);
                    String cpos1 =  Integer.toString(line-1) + "-" + Integer.toString(position+1);
                    if(!this.pyramid.containsKey(cpos0)){
                        return false;
                    } else if(!this.pyramid.containsKey(cpos1)){
                        return false;
                    } else {
                        if(this.pyramid.get(cpos0).equals(cardColor)){
                            return true;
                        } else if(this.pyramid.get(cpos1).equals(cardColor)){
                            return true;
                        } else {
                            return false;
                        }
                    }
                }
            }
            return false;
        } else {
            //１枚もカードがないときは　1-7 しか置けない
            if(placeID.equals("1-7")){
                return true;
            } else {
                return false;
            }
        }
    }
    
    public void printPyramid(){
        System.out.println(this.toString());
    }
    @Override
    public String toString(){
        StringBuilder pyramidString = new StringBuilder();
        for(int i=7;i>0;i--){
            StringBuilder sbuf = new StringBuilder();
            for(int sp=0;sp<i;sp++){
                sbuf.append(" ");
            }
            for(int j=0;j<7+(7-i);j++){
                String cpos =  Integer.toString(i) + "-" + Integer.toString(j);
                if(this.pyramid.containsKey(cpos)){
                    sbuf.append(" ");
                    sbuf.append(this.pyramid.get(cpos).charAt(0));
                } else {
                    sbuf.append("  ");
                }
            }
            pyramidString.append(sbuf.toString());
            pyramidString.append("\n");
        }
        return pyramidString.toString();
    }
    
    
    
}
