# Structure
## 1. Question Generation
We use [Heilman's method](http://www.cs.cmu.edu/~ark/mheilman/questions/) as our question generation model. This part is inside `QuestionGeneration` folder. `GenerateFromText` class is used to generate QA pairs from [SemArt](http://researchdata.aston.ac.uk/380/) dataset. `GenerateFromImageCaptionAddOnThereBe` class will generate QA pairs from an image caption file, and it will try to add *'there be'* in front of image captions if the model fails to generate any QA pairs. `GenerateFromImageCaption` class takes in image caption phrases and sentences (preprocessing is needed -- `QuestionGeneration/TemplateQG/convert2sentence.py`) together to generate more QA pairs.
To run the model, StanfordNLP server needs to be set up:
```
cd QuestionGeneration/HeilmansQG
bash runStanfordParserServer.sh
```
Then run the classes in `QuestionGeneration/TemplateQG` to generate QA pairs.

## 2. BERT Sequence Pair Classifier
First, process the outputs from TF-IDF model using `toBERT.py`, or unzip `QuestionAnswering/Data/Bert/bert_data.zip`.
Download the [pretrained model](https://drive.google.com/open?id=14_9iA5f6eBSrnTwE2rhQRI3J3KNta8dl), and store the pretrained weights and the config file in `QuestionAnswering/Models/Bert/` folder.
To retrain the model, run
`python trainBert.py`
To do the inference, first put `need_external_knowledge_comment_prediction.json` from [TF-IDF model](https://github.com/Zihua-Liu/QA_Pipeline_SemArt/tree/master/TF-IDF%20Comment%20Selection) into `QuestionAnswering/Data/Bert/` folder, then run
```
# Get test file from the previous stage of the pipeline 
python toTestBert.py
# Test
python testBert.py
```
The test results will be stored in `QuestionAnswering/Data/Bert/xlnet-clean.json` and `QuestionAnswering/Data/Bert/xlnet-pipeline.json`.
## 3. XLNet for QA
Download the [pretrained model](https://drive.google.com/open?id=14_9iA5f6eBSrnTwE2rhQRI3J3KNta8dl), and store the pretrained weights and the config file in `QuestionAnswering/Models/XLNet/` folder.
To retrain the model, run
```
python toXLNet.py
bash trainXLNet.sh
```
To do the inference, run 
```
# get pipeline result
bash testXLNet.sh
# get clean (remove all propagating errors) result
bash testXLNet.sh clean
```
The test result will be stored in `QuestionAnswering/Models/XLNet/predictions_.json`
