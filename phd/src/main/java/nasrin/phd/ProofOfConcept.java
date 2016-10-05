package nasrin.phd;

import gate.Corpus;
import gate.DataStore;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.LanguageAnalyser;
import gate.corpora.CorpusImpl;
import gate.creole.ANNIEConstants;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.persist.PersistenceException;
import gate.persist.SerialDataStore;
import gate.util.GateException;
import ir.laali.gate.ml.MlConfiguration;
import ir.laali.gate.ml.WekaClassifierPR;
import ir.laali.gate.ml.attributecalculator.FeatureValue;
import ir.laali.gate.ml.ecoder.BooleanVectorEncoder;
import ir.laali.gate.ml.ecoder.NumaratorEncoder;
import ir.laali.gate.ml.ecoder.NumberEncoder;
import ir.laali.gate.ml.instancegenerator.AnnotationInstance;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
/**
 * This class gets a GATE document and create an arff file out of the annotations in the GATE document
 */
public class ProofOfConcept {
	private static final String TOKEN_CLASSIFIER_FEATURE_NAME = "classifier";
	private String arffFilePath;
	private String idxTrain;
	private String modelFile;
	private String corpusPath;
	
	private Corpus corpus; 
	
	public ProofOfConcept(String arffFilePath, String idxTrain, String modelFile, String corpus) throws GateException {
		this.arffFilePath = arffFilePath;
		this.idxTrain = idxTrain;
		this.modelFile = modelFile;
		this.corpusPath = corpus;
		new File(this.idxTrain).mkdirs();
	}

	public void run() throws PersistenceException, MalformedURLException, ResourceInstantiationException, ExecutionException{
		loadCorpus();
		extractArffFile();
		
	}
	
	private void extractArffFile() throws ResourceInstantiationException, ExecutionException {
		MlConfiguration mlConfiguration = makeConfiguration();
		mlConfiguration.setTraining(true);
		runPipeline(mlConfiguration);
	}

	private void loadCorpus() throws PersistenceException, MalformedURLException, ResourceInstantiationException{
		  URL u = new File(corpusPath).toURI().toURL(); 
		  SerialDataStore sds = new SerialDataStore(u.toString()); 
		  sds.open(); //
//		  sds.create();
		  
		 
		  // getLrIds returns a list of LR Ids, so we get the first one
		  
		  Object lrId = sds.getLrIds("gate.corpora.SerialCorpusImpl").get(0); 
		 
		  // we need to tell the factory about the LR’s ID in the data 
		  // store, and about which datastore it is in − we do this 
		  // via a feature map: 
		  FeatureMap features = Factory.newFeatureMap(); 
		  features.put(DataStore.LR_ID_FEATURE_NAME, lrId); 
		  features.put(DataStore.DATASTORE_FEATURE_NAME, sds); 
		 
		  // read the document back 
		  corpus = (Corpus) 
		    Factory.createResource("gate.corpora.SerialCorpusImpl", features);

	}
	
	private void runPipeline(MlConfiguration mlConfiguration)
			throws ResourceInstantiationException, ExecutionException {
		SerialAnalyserController controller = (SerialAnalyserController) Factory.createResource(SerialAnalyserController.class.getName());
		
		FeatureMap featureMap = Factory.newFeatureMap();
		featureMap.put(WekaClassifierPR.ML_CONFIGURATION, mlConfiguration);
		LanguageAnalyser languageAnalyser = (LanguageAnalyser) Factory.createResource(WekaClassifierPR.class.getName(), featureMap);
		controller.add(languageAnalyser);
		
		controller.setCorpus(corpus);
		controller.execute();
	}
	
	private MlConfiguration makeConfiguration() {
		MlConfiguration mlConfiguration = new MlConfiguration();

		mlConfiguration.setIdxTrain(idxTrain);	//use for both training and testing
		mlConfiguration.setArffFilePath(arffFilePath);	//use only in training
		mlConfiguration.setModelFile(modelFile);	//use only in testing
		mlConfiguration.configClassifier("weka.classifiers.trees.J48 -C 0.25 -M 2"); //use when the model has to be automatically generated

		mlConfiguration.setInstanceExtractor(new AnnotationInstance("Original markups", "tweet"));
		
//		mlConfiguration.setInstanceExtractor(new AnnotationInstance(null, ANNIEConstants.TOKEN_ANNOTATION_TYPE));
		
		
//		mlConfiguration.addAttribute(new FeatureValue(ANNIEConstants.TOKEN_STRING_FEATURE_NAME, 
//				null, ANNIEConstants.TOKEN_ANNOTATION_TYPE, ANNIEConstants.TOKEN_STRING_FEATURE_NAME), new NumaratorEncoder());
//		mlConfiguration.addAttribute(new FeatureValue(ANNIEConstants.TOKEN_LENGTH_FEATURE_NAME, 
//				null, ANNIEConstants.TOKEN_ANNOTATION_TYPE, ANNIEConstants.TOKEN_LENGTH_FEATURE_NAME), new NumberEncoder());
//		mlConfiguration.setClassLabeler(new FeatureValue(ANNIEConstants.TOKEN_KIND_FEATURE_NAME, 
//				null, ANNIEConstants.TOKEN_ANNOTATION_TYPE, ANNIEConstants.TOKEN_KIND_FEATURE_NAME), new NumaratorEncoder());
		
//		mlConfiguration.addAttribute(new CountAnnotationType("NegativeAttr", "Polarity", "Negative"), new NumberEncoder());
//		mlConfiguration.addAttribute(new CountAnnotationType("PositiveAttr", "Polarity", "Positive"), new NumberEncoder());
//		mlConfiguration.addAttribute(new CountAnnotationType("NeturalAttr", "Polarity", "Neutral"), new NumberEncoder());
//		
//		mlConfiguration.addAttribute(new SumDomainPolarities("ExNegDomain", null, "explicitNegDomainNN", -0.5), new NumberEncoder());
//		mlConfiguration.addAttribute(new SumDomainPolarities("ImNegDomain", null, "implicitNegDomainNN", -0.5), new NumberEncoder());
//		mlConfiguration.addAttribute(new SumDomainPolarities("ModalDomain", null, "modalityDomainNN", 0.5), new NumberEncoder());
				
		String[] scopes = {"explicitNegDomainNN", "implicitNegDomainNN", "modalityDomainNN", "Tense_empty","Tense_future", "Tense_past", "Tense_present",
				"Tense_modal_past", "Tense_modal_present", "aspect_empty", "aspect_indefinite", "aspect_perfect", "aspect_progressive", "voice_active", "voice_passive"};
		
		String[] wordLists = {"vaers_all_2_uniques", "med_effect_2_uniques","Common_media_terms2_2_uniques","chv_vaers_2_uniques"};//"from_OVAE_2_uniques",
		//String[] wordLists = {"afinn"};
		for(String wordList: wordLists){
			mlConfiguration.addAttribute(new CountAnnotationType(wordList, null, wordList), new NumberEncoder());
			for(String scope: scopes){
				mlConfiguration.addAttribute(new AnnotationInTheDomain(wordList+"_IN_"+scope, null, "wordList_"+wordList, null, scope), new NumberEncoder());
			}
		}
		
		
		mlConfiguration.setClassLabeler(new FeatureValue("SentimentGOld", "Original markups", "tweet", "sentiment"), new NumaratorEncoder());

		return mlConfiguration;
	}
	
	public static void main(String[] args) throws MalformedURLException, GateException {
		//String installedGateDir = args[0]; //"/Applications/GATE_Developer_8.1"
		String installedGateDir = "/Applications/GATE_Developer_8.1";
		Properties props = System.getProperties();
		String gateHome = "gate.home";
		props.setProperty(gateHome, installedGateDir);
		Gate.init();
		Gate.getCreoleRegister().registerComponent(WekaClassifierPR.class);

//		String corpusPath = args[1];
//		String arffFilePath = args[2];
//		String idxTrain =  args[3];
//		String modelFile = args[4];
		
		String corpusPath = "/Users/Nasrin/Documents/Concordia/Thesis_PhD/DataSet/SemEval_Twitts/output_batchPipeline2/output_batchPipeline2"; 
		String arffFilePath = "/Users/Nasrin/Documents/Concordia/Thesis_PhD/DataSet/SemEval_Twitts/output_batchPipeline2/b-dev-dist.arff";
		String idxTrain =  "/Users/Nasrin/Documents/Concordia/Thesis_PhD/DataSet/SemEval_Twitts/output_batchPipeline2/idxTrain";
		String modelFile = "/Users/Nasrin/Documents/Concordia/Thesis_PhD/DataSet/SemEval_Twitts/output_batchPipeline2/something";
		new ProofOfConcept(arffFilePath,idxTrain,modelFile, corpusPath).run();
		System.out.println("ProofOfConcept.main()");
	}
}
