import socket, json, time
import ai

def messageHandling(json_message, board_list, hand_list):
    message = ""
    head = "{"
    append = "}\n"
    Connect = "\"From\":\"Client\",\"To\":\"Server\""

    # Identify the start phase - confirm as a new player
    if(json_message["Type"] == "ConnectionStart"):
        Type = "\"Type\":\"PlayerName\","
        name = "\"Name\":\"{}\",".format("TajimaLab")  # Send client name to server
        message = head + Type + name + Connect + append
        s.send(message.encode("utf-8"))
    
    # Server confirms receipt of player name, game starts
    if(json_message["Type"] == "NameReceived"):
        print("Game started!")

    # Update board state received from server (current card positions)
    if(json_message["Type"] == "BoardInfo"):
        board_list = json_message["Cardlist"]

    # Update hand cards received from server
    if(json_message["Type"] == "HandInfo"):
        hand_list = json_message["Cardlist"]

    # When server requests an Evaluation
    if(json_message["Type"] == "DoPlay"):
        Type = "\"Type\":\"Evaluation\","
        # Use AI to evaluate (call new Evaluation in ai.py)
        evals = ai.Evaluation(board_list, hand_list, turn=0)
        evaluation = ""
        for color, score in evals:
            evaluation += "\"{}\":\"{}\",".format(color, score)
        message = head + Type + evaluation + Connect + append
        s.send(message.encode("utf-8"))
        
    # When server acknowledges evaluation, it's time to select a move
    if(json_message["Type"] == "Accept"):
        position, card = ai.PlayCard(board_list, hand_list, turn=0)
        print("Play card", card, "at position", position)
        Position = "\"Position\":\"{}\",".format(position)
        Card = "\"Card\":\"{}\",".format(card)
        Type = "\"Type\":\"Play\","
        message = head + Type + Position + Card + Connect + append
        s.send(message.encode("utf-8"))

    # When game ends
    if(json_message["Type"] == "GameEnd"):
        message = "END"
    
    return message, board_list, hand_list

if __name__ == "__main__":
    # Connect to the game server (localhost, port 12052)
    ip = "localhost"
    port = 12052
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.connect((ip, port))
    print("Connected to server! IP: {} Port: {}".format(ip, str(port)))

    # Start the main game loop
    message = ""
    buffer_str = ""
    board_list = []
    hand_list = []
    while message != "END":
        # Receive data from server (may include multiple messages at once)
        receive = s.recv(4096).decode()
        buffer_str += str(receive)
        ln = buffer_str.find("\n")
        # Split messages by newline character
        while ln != -1:
            remessage = buffer_str[:ln]
            json_message = json.loads(remessage)
            # Process message and update board/hand state
            message, board_list, hand_list = messageHandling(json_message, board_list, hand_list)
            buffer_str = buffer_str[ln+1:]
            ln = buffer_str.find("\n")
        time.sleep(0.1) # Increase if you want the game to play slower for observation
            
    s.close() # Close connection when game ends
