import joblib
import numpy as np
import os
import random

CUR_DIR = os.path.dirname(os.path.abspath(__file__))
MODEL_PATH = os.path.join(CUR_DIR, "new_penguinparty_DecisionTree.pkl")

model = joblib.load(MODEL_PATH)

colors = ["green", "red", "blue", "yellow", "purple"]

def encode_board(board):
    """Encode the current board state into a count vector of each color."""
    cnt = [0]*5
    for v in board.values():
        if v in colors:
            cnt[colors.index(v)] += 1
    return cnt

def encode_hand(hand):
    """Encode the current hand into a count vector of each color."""
    return [hand.count(c) for c in colors]

def encode_sample(board, hand, pos, color):
    """
    Encode all relevant features of a candidate move:
    - current hand
    - current board
    - position of the move (row, col)
    - the color being played (one-hot)
    """
    bvec = encode_board(board)
    hvec = encode_hand(hand)
    row_idx, col_idx = [int(x) for x in pos.split('-')]
    color_oh = [0]*5
    color_oh[colors.index(color)] = 1
    return hvec + bvec + [row_idx, col_idx] + color_oh

def Evaluation(board, hand):
    """
    Evaluate all possible moves for each card color in hand using the AI model.
    Returns a list of [Color, Score] where the sum of scores is ~1.0 (normalized).
    """
    # Generate all legal moves
    legal_moves = []
    row1_cols = [int(pos.split('-')[1]) for pos in board if pos.startswith('1-')]
    cnt_row1 = len(row1_cols)
    min1, max1 = (min(row1_cols), max(row1_cols)) if row1_cols else (None, None)
    if cnt_row1 == 0:
        for color in set(hand):
            legal_moves.append(('1-7', color))
    else:
        if cnt_row1 < 7 and min1 > 1:
            for color in set(hand):
                legal_moves.append((f"1-{min1-1}", color))
        if cnt_row1 < 7 and max1 < 13:
            for color in set(hand):
                legal_moves.append((f"1-{max1+1}", color))
        for row in range(2, 8):
            for col in range(1, 14-row+1):
                pos = f"{row}-{col}"
                below_left = f"{row-1}-{col}"
                below_right = f"{row-1}-{col+1}"
                if below_left in board and below_right in board and pos not in board:
                    for color in set(hand):
                        if board[below_left] == color or board[below_right] == color:
                            legal_moves.append((pos, color))
    # Predict score for each legal move using the model
    X = np.array([encode_sample(board, hand, pos, color) for (pos, color) in legal_moves])
    probs = model.predict_proba(X)[:, 1]  # Probability of label=1 (good move)
    # Aggregate scores by color
    color_scores = {c: 0 for c in colors}
    for i, (pos, color) in enumerate(legal_moves):
        color_scores[color] += probs[i]
    total = sum(color_scores[c] for c in set(hand)) or 1
    eval_list = [[c.capitalize(), round(color_scores[c]/total, 3)] for c in colors]
    return eval_list

def RandomPlay(board, hand):
    """
    Select and return the move with the highest predicted score using the AI model.
    Returns (position, color) or (None, None) if no legal moves.
    """
    legal_moves = []
    row1_cols = [int(pos.split('-')[1]) for pos in board if pos.startswith('1-')]
    cnt_row1 = len(row1_cols)
    min1, max1 = (min(row1_cols), max(row1_cols)) if row1_cols else (None, None)
    if cnt_row1 == 0:
        for color in set(hand):
            legal_moves.append(('1-7', color))
    else:
        if cnt_row1 < 7 and min1 > 1:
            for color in set(hand):
                legal_moves.append((f"1-{min1-1}", color))
        if cnt_row1 < 7 and max1 < 13:
            for color in set(hand):
                legal_moves.append((f"1-{max1+1}", color))
        for row in range(2, 8):
            for col in range(1, 14-row+1):
                pos = f"{row}-{col}"
                below_left = f"{row-1}-{col}"
                below_right = f"{row-1}-{col+1}"
                if below_left in board and below_right in board and pos not in board:
                    for color in set(hand):
                        if board[below_left] == color or board[below_right] == color:
                            legal_moves.append((pos, color))
    if not legal_moves:
        return None, None
    X = np.array([encode_sample(board, hand, pos, color) for (pos, color) in legal_moves])
    probs = model.predict_proba(X)[:, 1]
    idx = np.argmax(probs)
    return legal_moves[idx]
