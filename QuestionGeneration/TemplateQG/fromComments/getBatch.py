import pandas as pd
fileName = 'qa_comments_train_0.csv'
col = ['IMAGE_FILE','DESCRIPTION', 'QUESTION', 'ANSWER']
data = []
f = open(fileName)
img, desc = '', ''
count = 0
for i in range(2000):
    line = f.readline()
    # print(line)
    cols = line[:-1].split('\t')
    if cols[0] == '':
        data.append([img, desc, cols[3], cols[4]])
        count+=1
        if (count + 51) % 100 == 0: 
            data.append(['07635-89d_stil.jpg', 'The attribution of this still-life to Caravaggio is doubtful. From a thematic point of view, large scale still-lifes in the Galleria Borghese are similar to outstanding early works by Caravaggio, but they lack the brilliance and the concentration on individual objects. This painting and another are usually attributed to Caravaggio. Because it is rather unconvincing to attribute such work to Caravaggio, some scholars distinguish this Caravaggesque painter of still-lifes from the master by naming him - after a splendid example of the genre - the Painter of the Still-Life in the Wadsworth Athenaeum, Hartford.', 'What lack the brilliance and the concentration on individual objects?', 'They'])
            count += 1
        if count >= 989:
            break
    else:
        img = cols[0]
        desc = cols[1]
    # print(cols)
df = pd.DataFrame(data=data, columns=col)
df.to_csv('batch_989.csv', index=False)