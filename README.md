# Structure
## 1. Question Generation
We use [Heilman's method](http://www.cs.cmu.edu/~ark/mheilman/questions/) as our question generation model. This part is inside `QuestionGeneration` folder. `GenerateFromText` class is used to generate QA pairs from [SemArt](http://researchdata.aston.ac.uk/380/) dataset. `GenerateFromImageCaptionAddOnThereBe` class will generate QA pairs from an image caption file, and it will try to add **there be** in front of image captions if the model fails to generate any QA pairs. `GenerateFromImageCaption` class takes in image caption phrases and sentences (preprocessing is needed -- `QuestionGeneration/TemplateQG/convert2sentence.py`) together to generate more QA pairs.
To run the model, StanfordNLP server needs to be set up:
```
cd QuestionGeneration/HeilmansQG
bash runStanfordParserServer.sh
```
Then run the classes in `QuestionGeneration/TemplateQG` to generate QA pairs.
