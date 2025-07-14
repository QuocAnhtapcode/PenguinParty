import joblib
import numpy as np

MODEL_PATH = "penguinparty_LogisticRegression.pkl"
model = joblib.load(MODEL_PATH)

colors = ["green", "red", "blue", "yellow", "purple"]

def encode_board(board):
    cnt = [0]*5
    for v in board.values():
        if v in colors:
            cnt[colors.index(v)] += 1
    return cnt

def encode_hand(hand):
    return [hand.count(c) for c in colors]

def encode_sample(turn, board, hand, pos, color):
    bvec = encode_board(board)
    hvec = encode_hand(hand)
    row_idx, col_idx = [int(x) for x in pos.split('-')]
    color_oh = [0]*5
    color_oh[colors.index(color)] = 1
    return hvec + bvec + [row_idx, col_idx] + color_oh + [turn]

def get_row1_range(board):
    hang1_pos = [int(pos.split('-')[1]) for pos in board if pos.startswith('1-')]
    if not hang1_pos:
        return None, None
    return min(hang1_pos), max(hang1_pos)

def legal_moves(board, hand):
    moves = []
    min1, max1 = get_row1_range(board)
    cnt_hang1 = sum([1 for pos in board if pos.startswith('1-')])
    if cnt_hang1 == 0:
        for color in set(hand):
            moves.append(('1-7', color))
    else:
        if cnt_hang1 < 7 and min1 > 1:
            for color in set(hand):
                moves.append((f"1-{min1-1}", color))
        if cnt_hang1 < 7 and max1 < 13:
            for color in set(hand):
                moves.append((f"1-{max1+1}", color))
        for row in range(2, 8):  # Hàng 2 đến 7
            for col in range(1, 14 - row + 1):
                pos = f"{row}-{col}"
                below_left = f"{row-1}-{col}"
                below_right = f"{row-1}-{col+1}"
                if below_left in board and below_right in board and pos not in board:
                    for color in set(hand):
                        if board[below_left] == color or board[below_right] == color:
                            moves.append((pos, color))
    return moves


def Evaluation(board, hand, turn):
    """Trả về list [[color, score_percent], ...] với tổng score = 1"""
    moves = legal_moves(board, hand)
    if not moves:
        # Nếu không có nước đi nào, trả lại giá trị đều
        return [[c, 1.0/len(colors)] for c in colors if c in hand]
    # Tính tổng xác suất theo từng màu
    X = np.array([encode_sample(turn, board, hand, pos, color) for (pos, color) in moves])
    probs = model.predict_proba(X)[:, 1]
    color_score = {c: 0.0 for c in set(hand)}
    for (move, prob) in zip(moves, probs):
        _, color = move
        color_score[color] += prob
    # Chuẩn hóa tổng về 1
    total = sum(color_score.values())
    if total == 0:
        # Trường hợp đặc biệt (không có xác suất), chia đều
        n = len(color_score)
        return [[c, 1.0/n] for c in color_score]
    return [[c, color_score[c]/total] for c in color_score]

def PlayCard(board, hand, turn):
    """Chọn nước đi xác suất cao nhất từ model, trả về (position, card)"""
    moves = legal_moves(board, hand)
    if not moves:
        return None, None
    X = np.array([encode_sample(turn, board, hand, pos, color) for (pos, color) in moves])
    probs = model.predict_proba(X)[:, 1]
    idx = np.argmax(probs)
    return moves[idx]
