import edu.cmu.ark.*;
import edu.stanford.nlp.trees.Tree;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//import java.text.NumberFormat;
//import weka.classifiers.functions.LinearRegression;
//import edu.cmu.ark.ranking.WekaLinearRegressionRanker;

public class GenerateFromImageCaption {
    public GenerateFromImageCaption() {}
    public static void main(String[] args) {
        QuestionTransducer qt = new QuestionTransducer();
        InitialTransformationStep trans = new InitialTransformationStep();
        QuestionRanker qr = null;

        qt.setAvoidPronounsAndDemonstratives(false);

        //pre-load
        AnalysisUtilities.getInstance();

        String[] buf = new String[3];
        Tree parsed;
        boolean printVerbose = false;
        String modelPath = null;

        List<Question> outputQuestionList = new ArrayList<Question>();
        boolean preferWH = false;
        boolean doNonPronounNPC = false;
        boolean doPronounNPC = true;
        Integer maxLength = 1000;
        boolean downweightPronouns = false;
        boolean avoidFreqWords = false;
        boolean dropPro = true;
        boolean justWH = false;
        String filename = "image_caption_20000.csv";
        int numberOfComments = 20971;

        for(int i=0;i<args.length;i++){
            if(args[i].equals("--debug")){
                GlobalProperties.setDebug(true);
            }else if(args[i].equals("--verbose")){
                printVerbose = true;
            }else if(args[i].equals("--model")){ //ranking model path
                modelPath = args[i+1];
                i++;
            }else if(args[i].equals("--keep-pro")){
                dropPro = false;
            }else if(args[i].equals("--downweight-pro")){
                dropPro = false;
                downweightPronouns = true;
            }else if(args[i].equals("--downweight-frequent-answers")){
                avoidFreqWords = true;
            }else if(args[i].equals("--properties")){
                GlobalProperties.loadProperties(args[i+1]);
            }else if(args[i].equals("--prefer-wh")){
                preferWH = true;
            }else if(args[i].equals("--just-wh")){
                justWH = true;
            }else if(args[i].equals("--full-npc")){
                doNonPronounNPC = true;
            }else if(args[i].equals("--no-npc")){
                doPronounNPC = false;
            }else if(args[i].equals("--max-length")){
                maxLength = new Integer(args[i+1]);
                i++;
            }else if(args[i].equals("--file-name")){
                filename = args[i+1];
                i++;
            }
        }

        qt.setAvoidPronounsAndDemonstratives(dropPro);
        trans.setDoPronounNPC(doPronounNPC);
        trans.setDoNonPronounNPC(doNonPronounNPC);

        if(modelPath != null){
            System.err.println("Loading question ranking models from "+modelPath+"...");
            qr = new QuestionRanker();
            qr.loadModel(modelPath);
        }

        try{
            BufferedReader br = new BufferedReader(new FileReader(filename));
            PrintWriter writer = new PrintWriter("QA_image_caption_20000.csv", "UTF-8");
            writer.println(String.join("\t", "img", "comment", "related text", "question", "answer"));
            br.readLine();
            if(GlobalProperties.getDebug()) System.err.println("\nInput Text:");
            String doc;

            int total = 0;
            int valid = 0;
            int NofQA = 0;
            int flag = 0;
            while(total < numberOfComments){
                outputQuestionList.clear();
                if (flag == 0) {
                    String line = br.readLine();
                    if (line == null)
                        break;
                    String[] contents = line.split("\t");
                    buf = contents;
                    doc = contents[1];
                    if (total % 1000 == 0) {
                        System.out.println("now processing no." + total + " instance.");
                    }
                } else {
                    doc = buf[2];
                }

                if(doc.length() == 0){
                    break;
                }

                long startTime = System.currentTimeMillis();
                List<String> sentences = AnalysisUtilities.getSentences(doc);

                //iterate over each segmented sentence and generate questions
                List<Tree> inputTrees = new ArrayList<Tree>();

                for(String sentence: sentences){
                    if(GlobalProperties.getDebug()) System.err.println("Question Asker: sentence: "+sentence);

                    parsed = AnalysisUtilities.getInstance().parseSentence(sentence).parse;
                    inputTrees.add(parsed);
                }

                if(GlobalProperties.getDebug()) System.err.println("Seconds Elapsed Parsing:\t"+((System.currentTimeMillis()-startTime)/1000.0));

                //step 1 transformations
                List<Question> transformationOutput = trans.transform(inputTrees);

                //step 2 question transducer
                for(Question t: transformationOutput){
                    if(GlobalProperties.getDebug()) System.err.println("Stage 2 Input: "+t.getIntermediateTree().yield().toString());
                    qt.generateQuestionsFromParse(t);
                    outputQuestionList.addAll(qt.getQuestions());
                }

                //remove duplicates
                QuestionTransducer.removeDuplicateQuestions(outputQuestionList);

                //step 3 ranking
                if(qr != null){
                    qr.scoreGivenQuestions(outputQuestionList);
                    boolean doStemming = true;
                    QuestionRanker.adjustScores(outputQuestionList, inputTrees, avoidFreqWords, preferWH, downweightPronouns, doStemming);
                    QuestionRanker.sortQuestions(outputQuestionList, false);
                }

                //now print the questions
                //double featureValue;
                if(flag == 0)
                    writer.println(String.join("\t", buf[0], buf[1], "", "", ""));
                int temp = NofQA;
                for(Question question: outputQuestionList){
                    if(question.getTree().getLeaves().size() > maxLength){
                        continue;
                    }
                    if(justWH && question.getFeatureValue("whQuestion") != 1.0){
                        continue;
                    }

                    if(printVerbose) System.out.print("\t"+AnalysisUtilities.getCleanedUpYield(question.getSourceTree()));
                    Tree ansTree = question.getAnswerPhraseTree();
                    if(printVerbose) System.out.print("\t");
                    if(ansTree != null){
                        if(printVerbose) System.out.print(AnalysisUtilities.getCleanedUpYield(question.getAnswerPhraseTree()));
                    }
                    if(printVerbose) System.out.print("\t"+question.getScore());
                    //System.err.println("Answer depth: "+question.getFeatureValue("answerDepth"));

                    if (question.getAnswerPhraseTree()!=null) {
                        NofQA ++;
                        writer.println(String.join("\t", "", "",
                                AnalysisUtilities.getCleanedUpYield(question.getSourceTree()),
                                question.yield(),
                                AnalysisUtilities.getCleanedUpYield(question.getAnswerPhraseTree())));
                    }
                }
                if (NofQA > temp) {
                    total++;
                    valid++;
                    flag = 0;
                } else if (flag == 1) {
                    total++;
                    flag = 0;
                } else {
                    flag ++;
                }
                if(GlobalProperties.getDebug()) System.err.println("Seconds Elapsed Total:\t"+((System.currentTimeMillis()-startTime)/1000.0));
                //prompt for another piece of input text
                if(GlobalProperties.getDebug()) System.err.println("\nInput Text:");
            }

            writer.close();
            System.out.println("Number of sentences: " + total + ", Number of valid sentences: " + valid + ", Number of QA pairs: " + NofQA);


        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void printFeatureNames(){
        List<String> featureNames = Question.getFeatureNames();
        for(int i=0;i<featureNames.size();i++){
            if(i>0){
                System.out.print("\n");
            }
            System.out.print(featureNames.get(i));
        }
        System.out.println();
    }
}
