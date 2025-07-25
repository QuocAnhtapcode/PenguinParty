/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package engarde.gui;

import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 *
 * @author ktaji
 */
public class EvaluationDialog extends javax.swing.JDialog {

    public static String key_1F = "1F";
    public static String key_1B = "1B";
    public static String key_2F = "2F";
    public static String key_2B = "2B";
    public static String key_3F = "3F";
    public static String key_3B = "3B";
    public static String key_4F = "4F";
    public static String key_4B = "4B";
    public static String key_5F = "5F";
    public static String key_5B = "5B";
    public static String[] keys = {key_1F,key_1B,key_2F,key_2B,key_3F,key_3B,key_4F,key_4B,key_5F,key_5B};

    public HashMap<String,ArrayList<Double>> dataValues = new HashMap<>();
    public GraphPanel gp1;
    public GraphPanel gp2;
    public GraphPanel gp3;
    public GraphPanel gp4;
    public GraphPanel gp5;
    /**
     * Creates new form EvaluetionValues
     */
    public EvaluationDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        for(String key:keys){
            this.dataValues.put(key, new ArrayList<>());
        }
        
        this.getContentPane().setLayout(new GridLayout(5,1));
        this.gp1 = new GraphPanel();
        this.gp1.setText("1");
        this.getContentPane().add(this.gp1);
        this.gp2 = new GraphPanel();
        this.gp2.setText("2");
        this.getContentPane().add(this.gp2);
        this.gp3 = new GraphPanel();
        this.gp3.setText("3");
        this.getContentPane().add(this.gp3);
        this.gp4 = new GraphPanel();
        this.gp4.setText("4");
        this.getContentPane().add(this.gp4);
        this.gp5 = new GraphPanel();
        this.gp5.setText("5");
        this.getContentPane().add(this.gp5);
        
        this.pack();
    }
    
    public void appendData(String key,double value){
        ArrayList<Double> datalist = this.dataValues.get(key);
        datalist.add(value);
        this.dataValues.put(key, datalist);
        
        if(key.equals(key_1F) || key.equals(key_1B)){
            gp1.resetGraph();
            gp1.addData(this.dataValues.get(key_1F), Color.red);
            gp1.addData(this.dataValues.get(key_1B), Color.blue);
        } else if(key.equals(key_2F) || key.equals(key_2B)){
            gp2.resetGraph();
            gp2.addData(this.dataValues.get(key_2F), Color.red);
            gp2.addData(this.dataValues.get(key_2B), Color.blue);
        } else if(key.equals(key_3F) || key.equals(key_3B)){
            gp3.resetGraph();
            gp3.addData(this.dataValues.get(key_3F), Color.red);
            gp3.addData(this.dataValues.get(key_3B), Color.blue);
        } else if(key.equals(key_4F) || key.equals(key_4B)){
            gp4.resetGraph();
            gp4.addData(this.dataValues.get(key_4F), Color.red);
            gp4.addData(this.dataValues.get(key_4B), Color.blue);
        } else if(key.equals(key_5F) || key.equals(key_5B)){
            gp5.resetGraph();
            gp5.addData(this.dataValues.get(key_5F), Color.red);
            gp5.addData(this.dataValues.get(key_5B), Color.blue);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
