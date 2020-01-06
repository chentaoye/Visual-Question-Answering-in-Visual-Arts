import java.io.*;
//import java.text.NumberFormat;
import java.util.*;

//import weka.classifiers.functions.LinearRegression;

//import edu.cmu.ark.ranking.WekaLinearRegressionRanker;
import edu.cmu.ark.*;
import edu.stanford.nlp.trees.Tree;

public class GenerateFromText {
    public GenerateFromText() {}
    public static void main(String[] args) {
        QuestionTransducer qt = new QuestionTransducer();
        InitialTransformationStep trans = new InitialTransformationStep();
        QuestionRanker qr = null;


        qt.setAvoidPronounsAndDemonstratives(false);

        //pre-load
        AnalysisUtilities.getInstance();

        String buf;
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
        String filename = "../SemArt/semart_test.csv";
        int numberOfComments = 19245;

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
            PrintWriter writer = new PrintWriter("fromComments/qa_comments_test_0.csv", "UTF-8");
            br.readLine();
            if(GlobalProperties.getDebug()) System.err.println("\nInput Text:");
            String doc;

            int curNumberOfComments = 0;
            while(curNumberOfComments < numberOfComments){
                if (curNumberOfComments % 10 == 0) {
                    System.out.println("now processing no." + curNumberOfComments + " instance.");
                    if (curNumberOfComments % 500 == 0) {
                        writer.close();
                        writer = new PrintWriter("fromComments/qa_comments_test_"+curNumberOfComments+".csv", "UTF-8");
                        writer.println(String.join("\t", "img", "comment", "related text", "question", "answer"));

                    }
                }
                curNumberOfComments++;
                outputQuestionList.clear();
                doc = "";
                String line = br.readLine();
                if (line == null)
                    break;
                String[] contents = line.split("\t");
                buf = contents[1];
                if(buf == null){
                    break;
                }
                doc += buf;

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
                writer.println(String.join("\t", contents[0], contents[1], "", "", ""));
                int qanumber = 0;
                for(Question question: outputQuestionList){
                    if(question.getTree().getLeaves().size() > maxLength){
                        continue;
                    }
                    if(justWH && question.getFeatureValue("whQuestion") != 1.0){
                        continue;
                    }
//                    System.out.print(question.yield());
                    if(printVerbose) System.out.print("\t"+AnalysisUtilities.getCleanedUpYield(question.getSourceTree()));
                    Tree ansTree = question.getAnswerPhraseTree();
                    if(printVerbose) System.out.print("\t");
                    if(ansTree != null){
                        if(printVerbose) System.out.print(AnalysisUtilities.getCleanedUpYield(question.getAnswerPhraseTree()));
                    }
                    if(printVerbose) System.out.print("\t"+question.getScore());
                    //System.err.println("Answer depth: "+question.getFeatureValue("answerDepth"));

//                    System.out.println();
                    if (question.getAnswerPhraseTree()!=null) {
                        writer.println(String.join("\t", "", "",
                                AnalysisUtilities.getCleanedUpYield(question.getSourceTree()),
                                question.yield(),
                                AnalysisUtilities.getCleanedUpYield(question.getAnswerPhraseTree())));
                        qanumber++;
                        if(qanumber > 5) break;
                    }
                }

                if(GlobalProperties.getDebug()) System.err.println("Seconds Elapsed Total:\t"+((System.currentTimeMillis()-startTime)/1000.0));
                //prompt for another piece of input text
                if(GlobalProperties.getDebug()) System.err.println("\nInput Text:");
            }

            writer.close();


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
