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

def risk_of_move(board, move):
    """
    Đo 'rủi ro' của 1 nước đi = số nước đi mới mà đối thủ có thể thực hiện ngay trên vị trí vừa đặt ở lượt sau.
    Càng ít nước đi mới thì càng an toàn.
    """
    pos, color = move
    row, col = map(int, pos.split('-'))
    # Sau khi đặt move, có thể tạo các vị trí trên hàng trên (row+1)
    next_pos_1 = f"{row+1}-{col}"
    next_pos_2 = f"{row+1}-{col-1}" if col > 1 else None
    next_positions = [next_pos_1]
    if next_pos_2: next_positions.append(next_pos_2)
    # Nếu các vị trí này chưa có trên bàn thì là rủi ro
    risk = 0
    for npos in next_positions:
        if npos not in board:
            risk += 1
    return risk

def safe_first_move(board, hand, legal):
    if not legal:
        return None
    # Đánh giá mức độ "rủi ro" của từng nước đi
    risks = [risk_of_move(board, move) for move in legal]
    min_risk = min(risks)
    # Ưu tiên nước có min risk, nếu nhiều hơn 1 chọn random
    candidate_moves = [move for i, move in enumerate(legal) if risks[i] == min_risk]
    return random.choice(candidate_moves)

def Evaluation(board, hand):
    moves = legal_moves(board, hand)
    # Safe First: ưu tiên nước đi tạo ít vị trí tiếp theo nhất
    color_scores = {c: 0 for c in colors}
    risks = [risk_of_move(board, move) for move in moves]
    min_risk = min(risks) if risks else 1
    for i, (pos, color) in enumerate(moves):
        if risks[i] == min_risk:
            color_scores[color] += 1
    total = sum(color_scores[c] for c in set(hand)) or 1
    eval_list = [[c.capitalize(), round(color_scores[c]/total, 3)] for c in colors]
    return eval_list

def RandomPlay(board, hand):
    moves = legal_moves(board, hand)
    if not moves:
        return None, None
    return safe_first_move(board, hand, moves)
