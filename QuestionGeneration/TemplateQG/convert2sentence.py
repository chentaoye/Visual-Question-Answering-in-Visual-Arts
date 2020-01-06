import json
import spacy
import pandas as pd
nlp = spacy.load("en_core_web_sm")

col = ['img','caption', 'sentence']
data = []

filename = "image_caption_10000/image_caption_0.json"
def processFile(filename, data, nlp):
    json_decode=json.load(open(filename))
    for key, value in json_decode.items():
        # print (key, value)
        doc = nlp(value)
        # for chunk in doc.noun_chunks:
        #     print(chunk.text, chunk.root.text, chunk.root.dep_,chunk.root.head.text)
        strs = []
        first = True
        for token in doc:
            # print(token.text, token.lemma_, token.pos_, token.tag_, token.dep_,
            #     token.shape_, token.is_alpha, token.is_stop)
            if token.tag_ == 'VBG' and first == True:
                strs.append(token.lemma_)
                first = False
            else:
                strs.append(token.text)
        # print (' '.join(strs))
        data.append([key, value, ' '.join(strs)])
        # value = "a group of people standing in front of a church"
        # print (value)
        # doc = nlp(value)
        # verbs = set()
        # for possible_subject in doc:
        #     if possible_subject.dep_ == 'nsubj' or possible_subject.dep_ == "ROOT":# and possible_subject.head.pos == 'VERB':
        #         print(possible_subject.text, possible_subject.dep_)
        #         verbs.add(possible_subject.head)
        # print(verbs)
        # break

for i in range(20):
    filename = "image_caption_10000/image_caption_"+str(i)+".json"
    processFile(filename, data, nlp)
for i in range(20, 42):
    filename = "image_caption_20000/image_caption_"+str(i)+".json"
    processFile(filename, data, nlp)

df = pd.DataFrame(data=data, columns=col)
df.to_csv('image_caption_20000.csv', index=False, sep = '\t')