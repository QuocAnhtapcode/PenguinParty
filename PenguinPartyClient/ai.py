import random

def Evaluation():
    # Màu sắc các lá bài trong game
    color = ["Green", "Red", "Blue", "Yellow", "Purple"]
    # Gán giá trị đánh giá ban đầu cho từng màu
    evaluation = [[s, 0.2] for s in color]
    return evaluation

def RandomPlay(board_list, hand_list):
    # Hàm phụ để sắp xếp vị trí theo hàng-cột (dạng "1-7")
    def sort_key(key):
        parts = key.split('-')
        return (int(parts[0]), int(parts[1]))
    
    # Nếu bàn chơi chưa có lá nào, chọn ngẫu nhiên vị trí đầu tiên
    if len(board_list) == 0:
        return "1-7", random.choice(hand_list)
    else:
        # Sắp xếp các vị trí trên bàn theo thứ tự
        board_list = {key: board_list[key] for key in sorted(board_list.keys(), key=sort_key)}
        koho = []  # Danh sách các nước đi hợp lệ có thể chọn
        # Lấy danh sách cột ở hàng 1
        gd = [int(x.split("-")[1]) for x in board_list if x.startswith("1-")]
        # Nếu khoảng cách các vị trí ở hàng 1 chưa đầy đủ (chưa kín 1-7)
        if max(gd) - min(gd) < 6:
            koho.append(["1-{}".format(max(gd)+1), hand_list])
            koho.append(["1-{}".format(min(gd)-1), hand_list])
        rcd1 = [-1, -1, "color"]
        # Duyệt qua từng vị trí để xác định nước đi hợp lệ theo luật
        for p in board_list:
            rcd2 = [int(n) for n in p.split("-")] + [board_list[p]]
            # Nếu có 2 vị trí liền kề theo chiều dọc
            if rcd1[0] == rcd2[0] and rcd1[1] == rcd2[1] - 1:
                act = "{}-{}".format(rcd1[0]+1, rcd1[1])
                # Nếu vị trí này chưa bị chiếm và trên tay có đúng màu
                if act not in board_list and (board_list[p] in hand_list or board_list["{}-{}".format(rcd1[0], rcd1[1])] in hand_list):
                    koho.append([act, [x for x in hand_list if (x == board_list[p] or x == board_list["{}-{}".format(rcd1[0], rcd1[1])])]])
            rcd1 = rcd2
        # Chọn ngẫu nhiên một nước đi trong các nước hợp lệ
        position = random.choice(koho)
        card = random.choice(position[1])
        return position[0], card