import random

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

def match_move(board, hand, moves):
    """Prioritize moves where card color matches at least one card directly below."""
    preferred = []
    for pos, color in moves:
        row, col = [int(x) for x in pos.split('-')]
        if row == 1:
            continue  # Cannot be on top of another card
        below_left = f"{row-1}-{col}"
        below_right = f"{row-1}-{col+1}"
        # Check if color matches any below
        if (below_left in board and board[below_left] == color) or \
           (below_right in board and board[below_right] == color):
            preferred.append((pos, color))
    if preferred:
        return random.choice(preferred)
    return random.choice(moves) if moves else (None, None)

def Evaluation(board, hand):
    """Score each color by its appearance in preferred match moves among all legal moves."""
    moves = legal_moves(board, hand)
    color_scores = {c: 0 for c in colors}
    total = 0
    for pos, color in moves:
        row, col = [int(x) for x in pos.split('-')]
        if row > 1:
            below_left = f"{row-1}-{col}"
            below_right = f"{row-1}-{col+1}"
            if (below_left in board and board[below_left] == color) or \
               (below_right in board and board[below_right] == color):
                color_scores[color] += 1
                total += 1
    if total == 0:  # No preferred moves, fall back to all legal
        for pos, color in moves:
            color_scores[color] += 1
        total = sum(color_scores[c] for c in colors)
    # Normalize to sum = 1
    eval_list = [[c.capitalize(), round(color_scores[c]/total, 3) if total > 0 else 0] for c in colors]
    return eval_list

def RandomPlay(board, hand):
    moves = legal_moves(board, hand)
    if not moves:
        return None, None
    return match_move(board, hand, moves)
