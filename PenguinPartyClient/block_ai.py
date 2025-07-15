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

def is_block_move(board, hand, move):
    """
    Returns True if the move is a block move: after this move, 
    there will be no legal move at that position for opponent
    (i.e., if not played, opponent could play there).
    """
    pos, color = move
    # Simulate if NOT playing this move, does opponent have this move next turn?
    simulated_board = board.copy()
    # Do NOT play the move
    # See if opponent could play (pos, color) in their legal moves if we skip
    # For board state, the spot is still empty
    # So, after our move (if skipped), is it legal for opponent with their hand?
    # We can't know opponent's hand, so assume if they have the color, they can play
    row, col = [int(x) for x in pos.split('-')]
    below_left = f"{row-1}-{col}"
    below_right = f"{row-1}-{col+1}"
    if below_left in board and below_right in board and pos not in board:
        # This pos is a legal move if opponent has color
        return True
    return False

def block_move(board, hand, moves):
    """Pick a block move if any; otherwise, pick random legal move."""
    block_moves = []
    for move in moves:
        if is_block_move(board, hand, move):
            block_moves.append(move)
    if block_moves:
        return random.choice(block_moves)
    return random.choice(moves) if moves else (None, None)

def Evaluation(board, hand):
    """Score each color by its appearance among block moves."""
    moves = legal_moves(board, hand)
    color_scores = {c: 0 for c in colors}
    block_moves = [move for move in moves if is_block_move(board, hand, move)]
    total = 0
    for pos, color in block_moves:
        color_scores[color] += 1
        total += 1
    if total == 0:
        for pos, color in moves:
            color_scores[color] += 1
        total = sum(color_scores[c] for c in colors)
    eval_list = [[c.capitalize(), round(color_scores[c]/total, 3) if total > 0 else 0] for c in colors]
    return eval_list

def RandomPlay(board, hand):
    moves = legal_moves(board, hand)
    if not moves:
        return None, None
    return block_move(board, hand, moves)
