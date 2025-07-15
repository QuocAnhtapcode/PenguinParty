import random
from copy import deepcopy

colors = ["green", "red", "blue", "yellow", "purple"]

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

def versatility_score(board, hand, pos, color):
    # Giả lập đánh move này, rồi đếm số nước đi hợp lệ tiếp theo cho hand còn lại
    new_board = deepcopy(board)
    new_hand = hand.copy()
    new_board[pos] = color
    new_hand.remove(color)
    next_moves = legal_moves(new_board, new_hand)
    return len(next_moves)

def prioritize_versatility_move(board, hand, moves):
    # Đánh giá từng move: move nào giữ lại nhiều lựa chọn nhất cho lượt sau thì ưu tiên
    scores = [versatility_score(board, hand, pos, color) for (pos, color) in moves]
    max_score = max(scores)
    candidate_moves = [mv for i, mv in enumerate(moves) if scores[i] == max_score]
    # Nếu nhiều move cùng điểm, chọn random cho đa dạng
    return random.choice(candidate_moves)

def Evaluation(board, hand):
    moves = legal_moves(board, hand)
    color_scores = {c: 0 for c in colors}
    # Tính versatility cho từng move
    scores = [versatility_score(board, hand, pos, color) for (pos, color) in moves]
    total = sum(scores) or 1
    for idx, (pos, color) in enumerate(moves):
        color_scores[color] += scores[idx]
    eval_list = [[c.capitalize(), round(color_scores[c]/total, 3)] for c in colors]
    return eval_list

def RandomPlay(board, hand):
    moves = legal_moves(board, hand)
    if not moves:
        return None, None
    return prioritize_versatility_move(board, hand, moves)

# ==== TEST ====
if __name__ == "__main__":
    # Dummy board and hand to test
    board = {}
    hand = ["green", "green", "blue", "yellow", "red", "purple"]
    print("Evaluation:", Evaluation(board, hand))
    print("Move chosen:", RandomPlay(board, hand))
