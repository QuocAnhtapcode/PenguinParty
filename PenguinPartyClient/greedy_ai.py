import random

colors = ["green", "red", "blue", "yellow", "purple"]

def encode_board(board):
    # Không cần dùng nếu không training
    cnt = [0]*5
    for v in board.values():
        if v in colors:
            cnt[colors.index(v)] += 1
    return cnt

def encode_hand(hand):
    return [hand.count(c) for c in colors]

def legal_moves(board, hand):
    moves = []
    row1_cols = [int(pos.split('-')[1]) for pos in board if pos.startswith('1-')]
    cnt_row1 = len(row1_cols)
    min1, max1 = (min(row1_cols), max(row1_cols)) if row1_cols else (None, None)
    if cnt_row1 == 0:
        for color in set(hand):
            moves.append(('1-7', color))
    else:
        if cnt_row1 < 7 and min1 > 1:
            for color in set(hand):
                moves.append((f"1-{min1-1}", color))
        if cnt_row1 < 7 and max1 < 13:
            for color in set(hand):
                moves.append((f"1-{max1+1}", color))
        for row in range(2, 8):
            for col in range(1, 14-row+1):
                pos = f"{row}-{col}"
                below_left = f"{row-1}-{col}"
                below_right = f"{row-1}-{col+1}"
                if below_left in board and below_right in board and pos not in board:
                    for color in set(hand):
                        if board[below_left] == color or board[below_right] == color:
                            moves.append((pos, color))
    return moves

def greedy_move(board, hand, moves):
    # Chọn nước đi có màu nhiều nhất trên tay (nếu hòa chọn ngẫu nhiên)
    color_count = {c: hand.count(c) for c in set(hand)}
    moves_sorted = sorted(moves, key=lambda x: color_count.get(x[1], 0), reverse=True)
    return moves_sorted[0] if moves_sorted else (None, None)

def Evaluation(board, hand):
    # Với greedy: chia đều điểm cho màu có nhiều nhất trên tay, 0 cho màu không có
    moves = legal_moves(board, hand)
    color_count = {c: hand.count(c) for c in colors}
    color_scores = {c: 0 for c in colors}
    # Đếm số move của mỗi màu trong các nước đi hợp lệ
    for (pos, color) in moves:
        color_scores[color] += 1
    total = sum(color_scores[c] for c in set(hand)) or 1
    eval_list = [[c.capitalize(), round(color_scores[c]/total, 3)] for c in colors]
    return eval_list

def RandomPlay(board, hand):
    moves = legal_moves(board, hand)
    if not moves:
        return None, None
    return greedy_move(board, hand, moves)
