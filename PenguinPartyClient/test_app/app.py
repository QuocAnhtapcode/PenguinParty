import os
import joblib
import random
from flask import Flask, render_template, request, redirect, url_for, session
from copy import deepcopy
import numpy as np

app = Flask(__name__)
app.secret_key = "PenguinPartySecret"

CUR_DIR = os.path.dirname(os.path.abspath(__file__))

BASE_DIR = os.path.abspath(os.path.join(CUR_DIR, ".."))

model_files = [
    os.path.join(BASE_DIR, "penguinparty_RandomForest.pkl"),
    os.path.join(BASE_DIR, "penguinparty_LogisticRegression.pkl"),
    os.path.join(BASE_DIR, "penguinparty_DecisionTree.pkl"),
    os.path.join(BASE_DIR, "penguinparty_XGBoost.pkl"),
]

model_names = ["DecisionTree", "LogisticRegression", "RandomForest", "XGBoost"]
models = {name: joblib.load(path) for name, path in zip(model_names, model_files)}

colors = ["green", "red", "blue", "yellow", "purple"]
color_counts = {"green": 8, "red": 7, "blue": 7, "yellow": 7, "purple": 7}

def encode_board(board):
    cnt = [0]*5
    for v in board.values():
        if v in colors:
            cnt[colors.index(v)] += 1
    return cnt

def encode_hand(hand):
    return [hand.count(c) for c in colors]

def encode_sample(board, hand, pos, color):
    bvec = encode_board(board)
    hvec = encode_hand(hand)
    row_idx, col_idx = [int(x) for x in pos.split('-')]
    color_oh = [0]*5
    color_oh[colors.index(color)] = 1
    return hvec + bvec + [row_idx, col_idx] + color_oh

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

def init_deck():
    deck = []
    for color in colors:
        deck += [color] * color_counts[color]
    random.shuffle(deck)
    return deck

def deal_hands(deck):
    hand1 = [deck.pop() for _ in range(14)]
    hand2 = [deck.pop() for _ in range(14)]
    return hand1, hand2

@app.route("/", methods=["GET", "POST"])
def index():
    if request.method == "POST":
        deck = init_deck()
        hand0, hand1 = deal_hands(deck)
        board = {}
        turn = 0
        session["hand0"] = hand0
        session["hand1"] = hand1
        session["board"] = board
        session["turn"] = turn
        session["move_count"] = 0
        session["history"] = []
        return redirect(url_for("game"))
    return render_template("index.html")

@app.route("/game", methods=["GET", "POST"])
def game():
    hand0 = session.get("hand0")
    hand1 = session.get("hand1")
    board = session.get("board")
    turn = session.get("turn")
    move_count = session.get("move_count")
    history = session.get("history")
    if hand0 is None or hand1 is None or board is None:
        return redirect(url_for("index"))
    hands = [hand0, hand1]
    hand = hands[turn]
    board = deepcopy(board)
    moves = legal_moves(board, hand)
    if not moves or not hand:
        winner = 0 if len(hands[0]) < len(hands[1]) else 1 if len(hands[1]) < len(hands[0]) else "Draw"
        return render_template("end.html", winner=winner, board=board, hand0=hands[0], hand1=hands[1], history=history)
    # Predict move scores for all models, then normalize to sum 100%
    move_raw_scores = {name: [] for name in model_names}
    for pos, color in moves:
        x = np.array([encode_sample(board, hand, pos, color)])
        for name, model in models.items():
            prob = model.predict_proba(x)[0, 1] if hasattr(model, "predict_proba") else 0
            move_raw_scores[name].append(prob)
    # Normalize
    move_scores = []
    norm_scores = {}
    for name in model_names:
        total = sum(move_raw_scores[name])
        norm_scores[name] = [(s / total * 100) if total > 0 else 0 for s in move_raw_scores[name]]
    for idx, (pos, color) in enumerate(moves):
        scores = {name: norm_scores[name][idx] for name in model_names}
        move_scores.append({
            "pos": pos, "color": color, "scores": scores
        })
    # Handle player move
    if request.method == "POST":
        move = request.form.get("move")
        if move:
            pos, color = move.split("|")
            board[pos] = color
            hands[turn].remove(color)
            history.append({"turn": int(turn), "pos": pos, "color": color})
            session["hand0"] = hands[0]
            session["hand1"] = hands[1]
            session["board"] = board
            session["turn"] = 1 - turn
            session["move_count"] = move_count + 1
            session["history"] = history
            return redirect(url_for("game"))
    return render_template("game.html", turn=turn, hand=hand, board=board, move_scores=move_scores, history=history, model_names=model_names)

@app.route("/reset")
def reset():
    session.clear()
    return redirect(url_for("index"))

if __name__ == "__main__":
    app.run(debug=True)
