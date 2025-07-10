import random

def Evaluation():
    color = ["Green", "Red", "Blue", "Yellow", "Purple"]
    evaluation = [[s,0.2] for s in color]
    return evaluation

def RandomPlay(board_list,hand_list):
    def sort_key(key):
        parts = key.split('-')
        return (int(parts[0]), int(parts[1]))
    
    if len(board_list)==0:
        return "1-7", random.choice(hand_list)
    else:
        board_list = {key: board_list[key] for key in sorted(board_list.keys(), key=sort_key)}
        koho = []
        gd = [int(x.split("-")[1]) for x in board_list if x.startswith("1-")]
        if max(gd)-min(gd)<6:
            koho.append(["1-{}".format(max(gd)+1),hand_list])
            koho.append(["1-{}".format(min(gd)-1),hand_list])
        rcd1 = [-1,-1,"color"]
        for p in board_list:
            rcd2 = [int(n) for n in p.split("-")]+[board_list[p]]
            if rcd1[0]==rcd2[0] and rcd1[1]==rcd2[1]-1:
                act = "{}-{}".format(rcd1[0]+1,rcd1[1])
                if act not in board_list and (board_list[p] in hand_list or board_list["{}-{}".format(rcd1[0],rcd1[1])] in hand_list):
                    koho.append([act,[x for x in hand_list if (x==board_list[p] or x==board_list["{}-{}".format(rcd1[0],rcd1[1])])]])
            rcd1 = rcd2
        position = random.choice(koho)
        card = random.choice(position[1])
        return position[0], card