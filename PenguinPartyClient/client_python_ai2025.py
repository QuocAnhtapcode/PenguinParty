import socket, json, time
import ai

def messageHandling(json_message, board_list, hand_list):
    message = ""
    head = "{"
    append = "}\n"
    Connect = "\"From\":\"Client\",\"To\":\"Server\""

    # Nhận biết giai đoạn bắt đầu - xác nhận là người chơi mới
    if(json_message["Type"] == "ConnectionStart"):
        Type = "\"Type\":\"PlayerName\","
        name = "\"Name\":\"{}\",".format("TajimaLab") # Gửi tên client cho server
        message = head + Type + name + Connect + append
        s.send(message.encode("utf-8"))
    
    # Server xác nhận đã nhận tên, game bắt đầu
    if(json_message["Type"] == "NameReceived"):
        print("Bắt đầu game!")

    # Cập nhật trạng thái bàn chơi từ server (vị trí các lá bài trên bàn)
    if(json_message["Type"] == "BoardInfo"):
        board_list = json_message["Cardlist"]

    # Cập nhật các lá bài trên tay từ server
    if(json_message["Type"] == "HandInfo"):
        hand_list = json_message["Cardlist"]

    # Khi server yêu cầu gửi đánh giá (Evaluation)
    if(json_message["Type"] == "DoPlay"):
        Type = "\"Type\":\"Evaluation\","
        # Tính toán giá trị đánh giá cho từng màu (sử dụng hàm bên file ai.py)
        evaluation = ""
        for c in ai.Evaluation():
            evaluation += "\"{}\":\"{}\",".format(c[0], c[1])
        message = head + Type + evaluation + Connect + append
        s.send(message.encode("utf-8"))
        
    # Khi server báo đã nhận đánh giá, đến lượt chọn vị trí và lá bài để đánh
    if(json_message["Type"] == "Accept"):
        position, card = ai.RandomPlay(board_list, hand_list)
        print("Đánh lá bài", card, "vào vị trí", position)
        Position = "\"Position\":\"{}\",".format(position)
        Card = "\"Card\":\"{}\",".format(card)
        Type = "\"Type\":\"Play\","
        message = head + Type + Position + Card + Connect + append
        s.send(message.encode("utf-8"))

    # Khi nhận thông báo kết thúc game
    if(json_message["Type"] == "GameEnd"):
        message = "END"
    
    return message, board_list, hand_list

if __name__ == "__main__":
    # Kết nối đến server game (chạy trên localhost, port 12052)
    ip = "localhost"
    port = 12052
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.connect((ip, port))
    print("Đã kết nối tới server! IP: {} Port: {}".format(ip, str(port)))

    # Bắt đầu vòng lặp chơi game
    message = ""
    buffer_str = ""
    board_list = []
    hand_list = []
    while message != "END":
        # Nhận dữ liệu từ server (có thể nhận nhiều message cùng lúc)
        receive = s.recv(4096).decode()
        buffer_str += str(receive)
        ln = buffer_str.find("\n")
        # Tách từng message theo ký tự xuống dòng
        while ln != -1:
            remessage = buffer_str[:ln]
            json_message = json.loads(remessage)
            # Xử lý message và cập nhật lại trạng thái bàn chơi/lá bài
            message, board_list, hand_list = messageHandling(json_message, board_list, hand_list)
            buffer_str = buffer_str[ln+1:]
            ln = buffer_str.find("\n")
        time.sleep(0.1) # Có thể tăng lên nếu muốn chạy chậm lại cho dễ quan sát
            
    s.close() # Đóng kết nối khi game kết thúc