import socket,json,time
import Prioritize_Versatility_ai

def messageHandling(json_message,board_list,hand_list):
    message = ""
    head = "{"
    append = "}\n"
    Connect = "\"From\":\"Client\",\"To\":\"Server\""

    #先攻か後攻か判別
    if(json_message["Type"] == "ConnectionStart"):
        Type = "\"Type\":\"PlayerName\","
        name = "\"Name\":\"{}\",".format("QuocAnh")
        message = head + Type + name + Connect + append
        s.send(message.encode("utf-8"))
    
    if(json_message["Type"] == "NameReceived"):
        print("Start")

    #ボード情報を配列に格納
    if(json_message["Type"]=="BoardInfo"):
        board_list = json_message["Cardlist"]

    #手札情報を配列に格納
    if(json_message["Type"]=="HandInfo"):
        hand_list = json_message["Cardlist"]

    #TypeがDoPlayの時の処理
    if(json_message["Type"]=="DoPlay"):
        Type = "\"Type\":\"Evaluation\","
        
        #評価値を算出
        evaluation = ""
        for c in Prioritize_Versatility_ai.Evaluation(board_list, hand_list):
            evaluation += "\"{}\":\"{}\",".format(c[0], c[1])

        message = head + Type + evaluation + Connect + append
        s.send(message.encode("utf-8"))
        
    if(json_message["Type"]=="Accept"):
        position, card = Prioritize_Versatility_ai.RandomPlay(board_list,hand_list)
        print(position,card)
        Position = "\"Position\":\"{}\",".format(position)
        Card = "\"Card\":\"{}\",".format(card)

        Type = "\"Type\":\"Play\","

        message = head + Type + Position + Card + Connect + append
        s.send(message.encode("utf-8"))

    if(json_message["Type"]=="GameEnd"):
        message = "END"
    
    return message,board_list,hand_list

if __name__=="__main__":
    #サーバー接続処理
    ip = "localhost"
    port = 12052
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.connect((ip, port))
    print("サーバに接続しました。IPアドレス {} ポート {}".format(ip,str(port)))

    #ゲームプレイ処理
    message = ""
    buffer_str = ""
    board_list = []
    hand_list = []
    while message != "END":
        receive = s.recv(4096).decode()
        #サーバーから受信したデータを出力
        buffer_str += str(receive)
        ln=buffer_str.find("\n")
        while ln != -1 :
            remessage = buffer_str[:ln]
            json_message = json.loads(remessage)

            message,board_list,hand_list = messageHandling(json_message,board_list,hand_list)
            
            buffer_str = buffer_str[ln+1:]
            ln = buffer_str.find("\n")
        time.sleep(0.1) #ここを増やすと表示が遅くなります
            
    s.close()