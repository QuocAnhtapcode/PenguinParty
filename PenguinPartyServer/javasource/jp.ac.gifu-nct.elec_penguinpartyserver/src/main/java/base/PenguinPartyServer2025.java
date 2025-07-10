/*
 * Thay đổi license nếu cần thiết
 * Đây là file chính của server game Penguin Party 2025
 */
package base;

import engarde.gui.Constants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import penguin.game.PenguinPartyGame;
import engarde.gui.EvaluationDialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import logger.CSVLogger;
import penguin.game.PenguinCard;

/**
 * Lớp chính điều khiển server cho game Penguin Party 2025.
 * Quản lý kết nối client, điều phối lượt chơi, xử lý logic game và giao tiếp với client.
 * Tác giả: ktajima
 */
public class PenguinPartyServer2025 {

    // Tiêu đề chương trình
    public static final String PROGRAM_TITLE = "Penguin Party Server 2025";
    // Phiên bản chương trình
    public static final String PROGRAM_VERSION = "2025.04.10 ver 1.2";

    /**
     * Tìm một cổng còn trống để server lắng nghe kết nối từ client.
     * Bắt đầu từ cổng 12052, thử tối đa 100 lần.
     * @return ServerSocket đã được khởi tạo hoặc null nếu không tìm được cổng.
     */
    public static ServerSocket getWatingPort() {
        int baseumber = 12052;
        ServerSocket soc = null;
        for (int i = 0; i < 100; i++) {
            try {
                soc = new ServerSocket(baseumber);
                return soc;
            } catch (IOException e) {
            }
            baseumber++;
        }
        return soc;
    }

    // Các biến liên quan đến giao diện hiển thị
    private SimpleCUIFrame cuiframe;
//    private EnGardeGUI gui;
    private EvaluationDialog ev0;
    private EvaluationDialog ev1;

    // Các biến liên quan đến kết nối mạng
    private int waitingPort;
    private boolean waiting = false;
    private ServerSocket serverSocket;
    private serverThread[] clients;
    private int connectedCount;

    // Các biến điều khiển game
    protected PenguinPartyGame game;
    private static File logDir;

    /**
     * Hàm khởi tạo server, tạo giao diện và sinh seed ngẫu nhiên cho game.
     */
    public PenguinPartyServer2025() {
        this.cuiframe = new SimpleCUIFrame(this);
        this.cuiframe.setTitle(PROGRAM_TITLE + PROGRAM_VERSION);
        this.cuiframe.setVersion(PROGRAM_VERSION);
        this.cuiframe.setVisible(true);
        long seed = Math.abs(new Random(new Date().getTime()).nextInt());
        this.cuiframe.setSeedText(seed);

//        this.gui = new EnGardeGUI();
//        this.ev0 = new EvaluationDialog(this.gui,false);
//        this.ev0.setTitle("Evalation Values of Player0");
//        this.ev1 = new EvaluationDialog(this.gui,false);
//        this.ev1.setTitle("Evalation Values of Player1");
//        gui.setVisible(true);
//        ev0.setVisible(true);
//        ev1.setVisible(true);

    }
    
    /**
     * Đặt lại server về trạng thái ban đầu, đóng socket cũ và tạo lại giao diện.
     */
    public void resetServer(){
        try {
            this.serverSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(PenguinPartyServer2025.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.cuiframe.setVisible(false);
        this.cuiframe.dispose();
//        this.gui.setVisible(false);
//        this.gui.dispose();
//        this.ev0.setVisible(false);
//        this.ev1.setVisible(false);
//        this.ev0.dispose();
//        this.ev1.dispose();
        
        this.cuiframe = new SimpleCUIFrame(this);
        this.cuiframe.setTitle(PROGRAM_TITLE + PROGRAM_VERSION);
        this.cuiframe.setVersion(PROGRAM_VERSION);
        this.cuiframe.setVisible(true);
        long seed = Math.abs(new Random(new Date().getTime()).nextInt());
        this.cuiframe.setSeedText(seed);

//        this.gui = new EnGardeGUI();
//        this.ev0 = new EvaluationDialog(this.gui,false);
//        this.ev0.setTitle("Evalation Values of Player0");
//        this.ev1 = new EvaluationDialog(this.gui,false);
//        this.ev1.setTitle("Evalation Values of Player1");
//        gui.setVisible(true);
//        ev0.setVisible(true);
//        ev1.setVisible(true);
        
    }

    /**
     * Khởi tạo server với seed xác định, bắt đầu lắng nghe kết nối client và điều phối game.
     * @param seed seed ngẫu nhiên để sinh bộ bài và trạng thái game
     */
    public void initWithSeed(long seed) {
        this.initServer();

        // Bắt đầu chờ client kết nối
        while (this.waiting) {
            try {
                Socket csoc = this.serverSocket.accept();
                this.printMessage(csoc.getInetAddress() + " đã kết nối.");
                if (connectedCount < clients.length) {
                    this.clients[connectedCount] = new serverThread(connectedCount, this, csoc);
                    this.clients[connectedCount].start();
                    connectedCount++;
                    if (connectedCount == 2) {
                        // Đủ 2 client, bắt đầu khởi tạo game
                        this.gameInit(seed, null);
                    }
                    this.clients[connectedCount - 1].sendInitMessage();
                } else {
                    // Đã đủ số lượng client tối đa
                    this.sendConnectionErrorMessage(csoc);
                }

                // Bắt đầu luồng xử lý cho client
            } catch (IOException ex) {
                this.waiting = false;
            }
        }
    }

    /**
     * Khi đã đủ 2 client, khởi tạo game mới với seed và tạo file log.
     * @param seed seed ngẫu nhiên
     * @param ld thư mục log (có thể null)
     */
    private void gameInit(long seed, File ld) {
        try {
            logDir = ld;
            if (logDir == null) {
                logDir = new File("." + File.separator + "log");
                logDir.mkdirs();
            }
            SimpleDateFormat dformat = new SimpleDateFormat("yyyyMMddHHmmss");
            String filename = dformat.format(new Date()) + ".csv";
            File logFile = new File(logDir.getPath() + File.separator + filename);
            CSVLogger.setOutputFile(logFile);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PenguinPartyServer2025.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.printMessage("Seed của game này là: " + seed);
        this.game = new PenguinPartyGame(seed);
        this.game.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                PenguinPartyServer2025.this.propertyChanged(evt);
            }
        });

    }

    /**
     * Xử lý sự kiện thay đổi trạng thái game, cập nhật giao diện và gửi thông tin tới client.
     * @param evt sự kiện thay đổi thuộc tính
     */
    public void propertyChanged(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("GAME_STATE_CHANGED")) {

            int state = (int) evt.getNewValue();
            if (state == PenguinPartyGame.GAMESTATUS_INIT) {
                // Trạng thái khởi tạo game
            } else if (state == PenguinPartyGame.GAMESTATUS_INGAME_PLAYER0_PLAY) {
                this.showGameState(100);
                this.clients[0].sendBoardInfo();
                this.clients[1].sendBoardInfo();
                this.resendRequest(0);
            } else if (state == PenguinPartyGame.GAMESTATUS_INGAME_PLAYER0_EVALUATED) {
                // Có thể hiển thị biểu đồ đánh giá cho player 0 nếu cần
            } else if (state == PenguinPartyGame.GAMESTATUS_INGAME_PLAYER1_PLAY) {
                this.showGameState(100);
                this.clients[0].sendBoardInfo();
                this.clients[1].sendBoardInfo();
                this.resendRequest(1);
            } else if (state == PenguinPartyGame.GAMESTATUS_INGAME_PLAYER1_EVALUATED) {
                // Có thể hiển thị biểu đồ đánh giá cho player 1 nếu cần
            } else if (state == PenguinPartyGame.GAMESTATUS_CALC_SCORE) {
                this.showGameState(100);
            } else if (state == PenguinPartyGame.GAMESTATUS_ROUND_END) {
                this.showGameState(1000);//
                int[] info = this.game.getWinner();
                String message = "Kết thúc vòng này. Người thắng là player" + info[2] + ". Điểm số: " + info[0] + " vs " + info[1];
                this.clients[0].sendRoundEndMessage(info[2], info[0], info[1], message);
                this.clients[1].sendRoundEndMessage(info[2], info[0], info[1], message);
            } else if (state == PenguinPartyGame.GAMESTATUS_GAMEEND) {
                this.showGameState(2000);
                this.clients[0].sendBoardInfo();
                this.clients[1].sendBoardInfo();
                // Kết thúc game, thông báo tới client và hiển thị thông báo
                int[] info = this.game.getWinner();
                String message = "Trò chơi kết thúc. Người thắng là player" + info[2] + ". Điểm số: " + info[0] + " vs " + info[1];
                this.clients[0].sendGameEndMessage(info[2], info[0], info[1], message);
                this.clients[1].sendGameEndMessage(info[2], info[0], info[1], message);
                JOptionPane.showMessageDialog(null, message, "Game END", JOptionPane.INFORMATION_MESSAGE);
                //System.exit(0);
            }
        }
    }

    private synchronized void showEvaluationGraph(int playerID, int delay) {
        //EngardeGame.printGameState(this.game, System.out);
        if(playerID == 0){
            HashMap<String,Double> map0 = this.game.getEvaluationValues(0);
            for(String key:map0.keySet()){
                double v = map0.get(key);
                this.ev0.appendData(key, v);
            }
            this.ev0.validate();
            this.ev0.repaint();
        } else if(playerID == 1){
            HashMap<String,Double> map1 = this.game.getEvaluationValues(1);
            for(String key:map1.keySet()){
                double v = map1.get(key);
                this.ev1.appendData(key, v);
            }
            this.ev1.validate();
            this.ev1.repaint();
        }
        
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ex) {
            Logger.getLogger(PenguinPartyServer2025.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    private synchronized void showGameState(int delay) {
        PenguinPartyGame.printGameState(this.game, System.out);
        //GUI infomation
        int cp = this.game.getCurrentPlayer();
        int p0s = this.game.getPlayerScores()[0];
        int p1s = this.game.getPlayerScores()[1];
        ArrayList<PenguinCard> hand0 = this.game.getHandofPlayer(0);
        ArrayList<PenguinCard> hand1 = this.game.getHandofPlayer(1);
        //
//        gui.setDrawData(cp, p0s, p1s, p0p, p1p, dc, hand0, hand1);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ex) {
            Logger.getLogger(PenguinPartyServer2025.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //** サーバー満員時にエラーを返す */
    private void sendConnectionErrorMessage(Socket soc) throws IOException {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(soc.getOutputStream()));

        HashMap<String, String> data = new HashMap<String, String>();
        data.put("Type", "ConnectionError");
        data.put("From", "Server");
        data.put("To", "Client");
        data.put("MessageID", "400");
        data.put("Message", "Server is full.");

        StringBuilder response = new StringBuilder();
        //response.append("<json>");
        ObjectMapper mapper = new ObjectMapper();
        response.append(mapper.writeValueAsString(data));
        //response.append("</json>");

        writer.println(response.toString());
        writer.flush();
        writer.close();
        soc.close();
    }

    private void initServer() {
        this.printMessage("Server 起動完了 " + PenguinPartyServer2025.PROGRAM_VERSION);
        this.clients = new serverThread[2];
        this.connectedCount = 0;
        this.serverSocket = PenguinPartyServer2025.getWatingPort();
        if (this.serverSocket == null) {
            this.printMessage("待ち受けが開始できません。");
            return;
        }
        this.waitingPort = this.serverSocket.getLocalPort();
        this.printMessage(this.waitingPort + "で待ち受け開始しました。");
        this.waiting = true;

    }

    public static void main(String[] args) {
        //Nomal Mode
        Constants.setFiles();
        //
        //Debug for ktajima
//        File dataDir = new File("C:\\Users\\ktajima\\OneDrive - 独立行政法人 国立高等専門学校機構\\NetBeansProjects\\novaluna\\img");
//        Constants.setFiles(dataDir);
        //
        PenguinPartyServer2025 server = new PenguinPartyServer2025();
    }

    public void receiveMessageFromClient(int playerID, String smessage) {
        try {
            //JSON -> HashMAP
            ObjectMapper mapper = new ObjectMapper();
            HashMap<String, String> de_map = mapper.readValue(smessage, HashMap.class);
            this.receiveMessage(playerID, de_map);
        } catch (JsonProcessingException ex) {
        }
    }

    /**
     * サーバのメッセージハンドリングメソッド
     */
    public void receiveMessage(int playerID, HashMap<String, String> data) {
        //タイプがない場合は捨てる
        if (!data.containsKey("Type")) {
            return;
        }
        //適切なクライアントからのメッセージでない場合は捨てる（本来はここに認証キーを組み込む）
        if (!data.get("From").equals("Client")) {
            return;
        }
        if (!data.get("To").equals("Server")) {
            return;
        }

        //DEBUG
        this.printMessage("[Recv" + playerID + "]" + data.toString());
        //DEBUG

        HashMap<String, String> response = new HashMap<>();
        response.put("From", "Server");
        response.put("To", "Client");

        //以後メッセージ毎の処理
        if (data.get("Type").equals("PlayerName")) {
            //コネクション初期化メッセージ → 名前を返して登録してもらう
            String ClientName = data.get("Name");
            this.clients[playerID].setPlayerName(ClientName);
            response.put("Type", "NameReceived");
            this.clients[playerID].sendMessage(response);

            //もう一方のプレイヤー名が確定していたら両方準備完了とする。（配ったクライアントでは理論的には0->1だが、プロトコル的に逆の場合もある）
            if (this.game != null) {
                int other = (playerID + 1) % 2;
                if (this.clients[other] != null) {
                    if (this.clients[other].getPlayerName() != null) {
                        this.game.PlayerConnected(this.clients[0].getPlayerName());
                        this.game.PlayerConnected(this.clients[1].getPlayerName());
                    }
                }
            }

        } else if (data.get("Type").equals("Evaluation")) {
            if ((playerID == 0 && this.game.isState(PenguinPartyGame.GAMESTATUS_INGAME_PLAYER0_PLAY))
                    || (playerID == 1 && this.game.isState(PenguinPartyGame.GAMESTATUS_INGAME_PLAYER1_PLAY))) {
                
                HashMap<String,Double> evaldata = new HashMap<>();
                if (data.containsKey("Green")) {
                    evaldata.put(PenguinCard.Color_Green, Double.parseDouble(data.get("Green")));
                } else if (data.containsKey("Red")) {
                    evaldata.put(PenguinCard.Color_Red, Double.parseDouble(data.get("Red")));
                } else if (data.containsKey("Blue")) {
                    evaldata.put(PenguinCard.Color_Blue, Double.parseDouble(data.get("Blue")));
                } else if (data.containsKey("Yellow")) {
                    evaldata.put(PenguinCard.Color_Yellow, Double.parseDouble(data.get("Yellow")));
                } else if (data.containsKey("Purple")) {
                    evaldata.put(PenguinCard.Color_Purple, Double.parseDouble(data.get("Purple")));
                }
                this.game.setEvaluationValues(playerID,evaldata);
                response.put("Type", "Accept");
                response.put("MessageID", "200");
                this.clients[playerID].sendMessage(response);
            } else {
                response.put("Type", "Accept");
                response.put("MessageID", "500");
                this.clients[playerID].sendMessage(response);
            }
        } else if (data.get("Type").equals("Play")) {
            //サーバがPLAYメッセージを受け取れない場合はエラー処理に回す
            if ((playerID == 0 && this.game.isState(PenguinPartyGame.GAMESTATUS_INGAME_PLAYER0_EVALUATED))
                    || (playerID == 1 && this.game.isState(PenguinPartyGame.GAMESTATUS_INGAME_PLAYER1_EVALUATED))) {
            //正しい状態
            if (data.get("Position") == null) {
                this.sendMessageErrorMessage(playerID);
                return;
            }
            if (data.get("Card") == null) {
                this.sendMessageErrorMessage(playerID);
                return;
            }
            //カードの色と場所が問題ないか確認
            String position = data.get("Position");
            String color = data.get("Card");
            //手札にあるか確認
            if(!this.game.checkPlayableCards(playerID, color)){
                //おけない場合はエラー
                this.sendMessageErrorMessage(playerID);
                return;
            }
            //おけるか確認
            if(!this.game.checkPlaceableCard(playerID, position, color)){
                //おけない場合はエラー
                this.sendMessageErrorMessage(playerID);
                return;
            }
            //正しく置けることが確認できた
            //仕様上先にメッセージを転送してから、駒を動かす
            response.put("Type", "Played");
            response.put("PlayerID", Integer.toString(playerID));
            response.put("Position", position);
            response.put("Card", color);
            this.clients[(playerID + 1) % 2].sendMessage(response);
            //カードを置く→イベントが発火してDoplayも相手に届く
            this.game.placeACard(playerID, position, color);
            //ログに表示する
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(bos);
            PenguinPartyGame.printGameState(this.game, ps);
            this.cuiframe.addMessage(bos.toString());
            
        }
        } else if (data.get("Type").equals("")) {
        } else if (data.get("Type").equals("")) {
        } else if (data.get("Type").equals("")) {

        }

    }

    public void sendMessageErrorMessage(int playerID) {
        this.clients[playerID].sendErrorMessage(404, "Message fromat error.");
        resendRequest(playerID);
    }

    public void sendStateErrorMessage(int playerID) {
        this.clients[playerID].sendErrorMessage(403, "Do NOT allow this command now.");
        resendRequest(playerID);
    }

    private void resendRequest(int playerID) {
        if (playerID == 0) {
            if (this.game.isState(PenguinPartyGame.GAMESTATUS_INGAME_PLAYER0_PLAY)) {
                this.clients[playerID].sendHandInfo(playerID);
                this.clients[playerID].sendDoPlayMessage(103, "Plase send evaluation information.");
            } else if (this.game.isState(PenguinPartyGame.GAMESTATUS_INGAME_PLAYER0_EVALUATED)) {
                this.clients[playerID].sendHandInfo(playerID);
                this.clients[playerID].sendDoPlayMessage(101, "Plase select your card.");
            }
        } else if (playerID == 1) {
            if (this.game.isState(PenguinPartyGame.GAMESTATUS_INGAME_PLAYER1_PLAY)) {
                this.clients[playerID].sendHandInfo(playerID);
                this.clients[playerID].sendDoPlayMessage(103, "Plase send evaluation information.");
            } else if (this.game.isState(PenguinPartyGame.GAMESTATUS_INGAME_PLAYER1_EVALUATED)) {
                this.clients[playerID].sendHandInfo(playerID);
                this.clients[playerID].sendDoPlayMessage(101, "Plase select your card.");
            }
        }
    }

    public void printMessage(String message) {
        this.cuiframe.addMessage(message);
        System.out.println(message);
    }

}

class serverThread extends Thread {

    private int playerID;
    private String PlayerName;
    private PenguinPartyServer2025 parent;
    private BufferedReader clientReader;
    private PrintWriter clientWriter;
    private StringBuilder sbuf;

    public static Pattern jsonStartEnd = Pattern.compile(".*?\\{(.*)\\}.*?");
    public static Pattern jsonEnd = Pattern.compile("(.*)\\}.*?");
    public static Pattern jsonStart = Pattern.compile(".*?\\{(.*)");

    public serverThread(int pid, PenguinPartyServer2025 p, Socket csoc) {
        this.playerID = pid;
        this.parent = p;
        this.sbuf = new StringBuilder();
        try {
            this.clientReader = new BufferedReader(new InputStreamReader(csoc.getInputStream()));
            this.clientWriter = new PrintWriter(new OutputStreamWriter(csoc.getOutputStream()));
        } catch (IOException ex) {

        }

    }

    public void setPlayerName(String name) {
        this.PlayerName = name;
    }

    public String getPlayerName() {
        return this.PlayerName;
    }

    public void sendBoardInfo() {
        HashMap<String, Object> response = new HashMap<>();
        response.put("From", "Server");
        response.put("To", "Client");
        response.put("Type", "BoardInfo");

        HashMap<String, String> info = this.parent.game.getPyramidInformation();
        response.put("Cardlist", info);
        
        int[] scores = this.parent.game.getPlayerScores();
        response.put("Numofcards", Integer.toString(info.keySet().size()));

        if (this.parent.game.getGameStatus() == PenguinPartyGame.GAMESTATUS_INGAME_PLAYER0_PLAY) {
            response.put("CurrentPlayer", "0");
        } else if (this.parent.game.getGameStatus() == PenguinPartyGame.GAMESTATUS_INGAME_PLAYER1_PLAY) {
            response.put("CurrentPlayer", "1");
        }
        response.put("PlayerScore_0", Integer.toString(scores[0]));
        response.put("PlayerScore_1", Integer.toString(scores[1]));

        this.sendMessageObject(response);
    }

    public void sendHandInfo(int playerID) {
        HashMap<String, Object> response = new HashMap<>();
        response.put("From", "Server");
        response.put("To", "Client");
        response.put("Type", "HandInfo");
        ArrayList<PenguinCard> hands = this.parent.game.getHandofPlayer(playerID);
        
        response.put("Numofcards", Integer.toString(hands.size()));
        ArrayList<String> handsCard = new ArrayList<String>();
        for(PenguinCard pc:hands){
            handsCard.add(pc.getColor());
        }
        response.put("Cardlist", handsCard);

        this.sendMessageObject(response);

    }

    public void sendDoPlayMessage(int id, String message) {
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("To", "Client");
        data.put("From", "Server");
        data.put("Type", "DoPlay");
        data.put("Message", message);
        this.sendMessage(data);
    }

//    public void sendPlayedMessage(int id, Object message) {
//        HashMap<String, String> data = new HashMap<String, String>();
//        data.put("To", "Client");
//        data.put("From", "Server");
//        data.put("Type", "Played");
//        this.sendMessage(data);
//    }

    public void sendErrorMessage(int id, String message) {
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("To", "Client");
        data.put("From", "Server");
        data.put("Type", "Error");
        data.put("MessageID", Integer.toString(id));
        data.put("Message", message);
        this.sendMessage(data);
    }

    public void sendInitMessage() {
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("Type", "ConnectionStart");
        data.put("To", "Client");
        data.put("From", "Server");
        data.put("ClientID", Integer.toString(playerID));
        this.sendMessage(data);
    }

    public void sendRoundEndMessage(int winner, int score0, int socre1, String message) {
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("To", "Client");
        data.put("From", "Server");
        data.put("Type", "RoundEnd");
        data.put("RWinner", Integer.toString(winner));
        data.put("Score0", Integer.toString(score0));
        data.put("Score1", Integer.toString(socre1));
        data.put("Message", message);
        this.sendMessage(data);
    }

    public void sendGameEndMessage(int winner, int score0, int socre1, String message) {
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("To", "Client");
        data.put("From", "Server");
        data.put("Type", "GameEnd");
        data.put("Winner", Integer.toString(winner));
        data.put("Score0", Integer.toString(score0));
        data.put("Score1", Integer.toString(socre1));
        data.put("Message", message);
        this.sendMessage(data);
    }

    public void sendMessage(String message) {
        this.clientWriter.println(message);
        this.clientWriter.flush();
    }

    public void sendMessage(HashMap<String, String> data) {
        try {
            StringBuilder response = new StringBuilder();

            //response.append("<json>");
            ObjectMapper mapper = new ObjectMapper();
            response.append(mapper.writeValueAsString(data));
            //response.append("</json>");

            this.sendMessage(response.toString());

            //DEBUG
            parent.printMessage("[Sent" + playerID + "]" + data.toString());
            //DEBUG

        } catch (JsonProcessingException ex) {
            Logger.getLogger(serverThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void sendMessageObject(HashMap<String, Object> data) {
        try {
            StringBuilder response = new StringBuilder();

            //response.append("<json>");
            ObjectMapper mapper = new ObjectMapper();
            response.append(mapper.writeValueAsString(data));
            //response.append("</json>");

            this.sendMessage(response.toString());

            //DEBUG
            parent.printMessage("[Sent" + playerID + "]" + data.toString());
            //DEBUG

        } catch (JsonProcessingException ex) {
            Logger.getLogger(serverThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = this.clientReader.readLine()) != null) {
                Matcher startend = jsonStartEnd.matcher(line);
                Matcher end = jsonEnd.matcher(line);
                Matcher start = jsonStart.matcher(line);
                if (startend.matches()) {
                    sbuf = new StringBuilder();
                    sbuf.append("{");
                    sbuf.append(startend.group(1));
                    sbuf.append("}");
                    this.parent.receiveMessageFromClient(playerID, sbuf.toString());
                } else if (end.matches()) {
                    sbuf.append(end.group(1));
                    sbuf.append("}");
                    this.parent.receiveMessageFromClient(playerID, sbuf.toString());
                } else if (start.matches()) {
                    sbuf = new StringBuilder();
                    sbuf.append("{");
                    sbuf.append(start.group(1));
                } else {
                    sbuf.append(line);
                }
            }
        } catch (IOException ex) {
        }
    }

}
