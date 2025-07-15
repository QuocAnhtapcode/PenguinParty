import random
import numpy as np
import os

# List of card colors
colors = ["green", "red", "blue", "yellow", "purple"]

def encode_board(board):
    """Encode the board state into color counts."""
    cnt = [0]*5
    for v in board.values():
        if v in colors:
            cnt[colors.index(v)] += 1
    return cnt

def encode_hand(hand):
    """Encode the hand into color counts."""
    return [hand.count(c) for c in colors]

def encode_sample(board, hand, pos, color):
    """Encode all relevant features for a candidate move."""
    bvec = encode_board(board)
    hvec = encode_hand(hand)
    row_idx, col_idx = [int(x) for x in pos.split('-')]
    color_oh = [0]*5
    color_oh[colors.index(color)] = 1
    return hvec + bvec + [row_idx, col_idx] + color_oh

def legal_moves(board, hand):
    """Return all legal moves as (pos, color) tuples for the current hand and board."""
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
            for col in range(1, 14 - row + 1):
                pos = f"{row}-{col}"
                below_left = f"{row-1}-{col}"
                below_right = f"{row-1}-{col+1}"
                if below_left in board and below_right in board and pos not in board:
                    for color in set(hand):
                        if board[below_left] == color or board[below_right] == color:
                            moves.append((pos, color))
    return moves

def hybrid_move(board, hand, legal):
    """
    Hybrid strategy:
    - Priority 1: Play a card whose color matches at least one card directly below (match-move).
    - Priority 2: If several such moves, choose the color that appears most in hand (greedy).
    - If no match-move is possible, choose greedy (color most available in hand).
    """
    # Find moves that match color below
    preferred = []
    for pos, color in legal:
        row, col = map(int, pos.split('-'))
        if row == 1:
            continue
        below_left = f"{row-1}-{col}"
        below_right = f"{row-1}-{col+1}"
        if (board.get(below_left) == color or board.get(below_right) == color):
            preferred.append((pos, color))
    candidate_moves = preferred if preferred else legal
    # Among candidates, pick one with the most available color in hand
    color_count = {c: hand.count(c) for c in set(hand)}
    sorted_moves = sorted(candidate_moves, key=lambda x: color_count.get(x[1], 0), reverse=True)
    return sorted_moves[0]  # always returns one move

def Evaluation(board, hand):
    """
    Evaluate the possible moves by color using the hybrid logic.
    Returns a list: [["Green", score], ...] (sum of scores is 1.0, all colors included).
    """
    legal = legal_moves(board, hand)
    # For each color, score is proportional to number of moves matching that color
    color_scores = {c: 0 for c in colors}
    for pos, color in legal:
        # Score for color if it's in at least one match-move, else normal move
        row, col = map(int, pos.split('-'))
        if row > 1:
            below_left = f"{row-1}-{col}"
            below_right = f"{row-1}-{col+1}"
            if board.get(below_left) == color or board.get(below_right) == color:
                color_scores[color] += 2  # Match-move: higher weight
            else:
                color_scores[color] += 1
        else:
            color_scores[color] += 1
    total = sum(color_scores[c] for c in set(hand)) or 1
    eval_list = [[c.capitalize(), round(color_scores[c]/total, 3)] for c in colors]
    return eval_list

def RandomPlay(board, hand):
    """
    Select and return the best move according to hybrid logic.
    Returns (position, color) or (None, None) if no move is possible.
    """
    legal = legal_moves(board, hand)
    if not legal:
        return None, None
    return hybrid_move(board, hand, legal)
