/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logger;

import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ktajima
 */
public class CSVLogger {
    private static SimpleDateFormat log_date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static ArrayList<ArrayList<String>> log;
    public static String GameID;
    public static Date StartDate;

    static {
        CSVLogger.log = new ArrayList<ArrayList<String>>();
    }
    private static File logFile = null;
    private static PrintWriter writer = null;
    public static void setOutputFile(File datafile) throws FileNotFoundException{
        logFile = datafile;
        try {
            writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(datafile),"UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(CSVLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static void closeFile(){
        if(logFile != null){
            writer.flush();
            writer.close();
            logFile = null;
        }
        
    }
    public static void outputAllData(File datafile) throws FileNotFoundException{
        try { 
            PrintWriter output = new PrintWriter(new OutputStreamWriter(new FileOutputStream(datafile),"UTF-8"));
            for(ArrayList<String> line:log){
                for(String elem:line){
                    output.print(elem);
                    output.print(",");
                }
                output.println();
            }
            output.close();
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(CSVLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void printLine(ArrayList<String> line){
        if(logFile == null) return;
        
        for(String elem:line){
            writer.print(elem);
            writer.print(",");
        }
        writer.println();
        writer.flush();
    }
    
    //ゲーム開始ログ
    public static void gameStart(long seed,String playerName0,String playerName1){
        //開始時刻の記録
        StartDate = new Date();
        //ゲームIDの生成と出力
        SimpleDateFormat dformat = new SimpleDateFormat("yyyyMMddHHmmss");
        GameID = dformat.format(StartDate);
        GameID += playerName0;
        
        //ゲーム開始情報の出力
        ArrayList<String> elems = new ArrayList<String>();
        
        elems.add(GameID);
        elems.add(log_date_format.format(StartDate));
        elems.add("[Event]Game Start");
        elems.add(Long.toString(seed));
        elems.add(playerName0);
        elems.add(playerName1);
        
        printLine(elems);
        log.add(elems);
    }
    
    public static void gameEnd(int winner,String playerName){
        ArrayList<String> elems = new ArrayList<String>();
        elems.add(GameID);
        elems.add(log_date_format.format(new Date()));
        elems.add("[Event]Game End");
        elems.add(Integer.toString(winner));
        elems.add(playerName);
        
        printLine(elems);
        log.add(elems);
    }
    

    
}
