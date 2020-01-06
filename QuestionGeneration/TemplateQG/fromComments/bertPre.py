import pandas as pd 
import csv
import random

# fileName = 'qa_comments_train_0.csv'
pre = 'qa_comments_train_'
post = '.csv'
col = ['QUESTION', 'SENTENCE', 'LABEL']
data = []

for mid in range(3500, 3600, 500):
    fileName = pre + str(mid) + post
    f = open(fileName)
    lines = f.readlines()
    flag = False
    for i in range(1, len(lines)):
        line = lines[i]
        # print(line)
        cols = line[:-1].split('\t')
        if cols[0] == '' and flag:
            data.append([cols[3], cols[2], 1])
            flag = False
        elif cols[0] != '':
            flag = True

size = len(data)
for i in range(size):
    rdN = 0
    if i % 2 == 0:
        rdN = random.randrange(1, size, 2)
    else:
        rdN = random.randrange(0, size, 2)
    data.append([data[i][0], data[rdN][1], 0])

        

df = pd.DataFrame(data=data, columns=col)
df.to_csv('qaFromComments_test.csv', index=False)
