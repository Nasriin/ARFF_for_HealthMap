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

public class ProofOfConcept {
	private static final String TOKEN_CLASSIFIER_FEATURE_NAME = "classifier";
	private String arffFilePath = "/Users/Nasrin/Documents/Concordia/Thesis_PhD/Joa/training-gate-batch-2/MLCorpus/115.arff";
	private String idxTrain =  "/Users/Nasrin/Documents/Concordia/Thesis_PhD/Joa/training-gate-batch-2/MLCorpus/idxTrain/";
	private String modelFile = "/Users/Nasrin/Documents/Concordia/Thesis_PhD/Joa/training-gate-batch-2/MLCorpus/tree.model";
	
	private Corpus corpus; 
	
	public ProofOfConcept() throws GateException {
		new File(idxTrain).mkdirs();
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
		  URL u = new File("/Users/Nasrin/Documents/Concordia/Thesis_PhD/Joa/training-gate-batch-2/corpus").toURI().toURL(); 
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

		mlConfiguration.setInstanceExtractor(new AnnotationInstance("Original markups", "Text"));
		
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
		
//		mlConfiguration.addAttribute(new AnnotationInTheDomain("Tense_present"+"IN"+"ModalityDomainNN", null, "Tense_present", null, "ModalityDomainNN"), new NumberEncoder());
		
		String[] scopes = {"explicitNegDomainNN", "implicitNegDomainNN", "modalityDomainNN", "Tense_empty","Tense_future", "Tense_past", "Tense_present",
				"Tense_modal_past", "Tense_modal_present", "aspect_empty", "aspect_indefinite", "aspect_perfect", "aspect_progressive", "voice_active", "voice_passive"};
//		String[] wordlists = {"additives_word_1_comma_inversion", "Common_media_terms2_0_dequoted" ,"Common_media_terms2_1_comma_inversion", "Common_media_terms2_2_uniques",
//		"Common_media_terms2_3_last_word", "Vaccine_terms_0_dequoted", "Vaccine_terms_1_comma_inversion", "Vaccine_terms_2_uniques","Vaccine_terms_3_last_word",
//		"additives_word_0_dequoted", "additives_word_1_comma_inversion", "additives_word_2_uniques", "additives_word_3_last_word", "body_0_dequoted", 
//		"body_1_comma_inversion", "body_2_uniques", "body_3_last_word", "chv_vaers_0_dequoted", "chv_vaers_1_comma_inversion", "chv_vaers_2_uniques", "chv_vaers_3_last_word", 
//		"from_OVAE_0_dequoted", "from_OVAE_1_comma_inversion", "from_OVAE_2_uniques", "from_OVAE_3_last_word", "med_effect_0_dequoted", "med_effect_1_comma_inversion",
//		"med_effect_2_uniques", "med_effect_3_last_word", "vaers_all_0_dequoted", "vaers_all_1_comma_inversion", "vaers_all_2_uniques", "vaers_all_3_last_word"};
		
		String[] wordlists = {"from_OVAE_2_uniques"};
		for(String wordList: wordlists){
			mlConfiguration.addAttribute(new BooleanAnnotationType(wordList, null, "wordList_"+wordList), new NumaratorEncoder());
			for(String scope: scopes){
				mlConfiguration.addAttribute(new AnnotationInTheDomain(wordList+"_IN_"+scope, null, wordList, null, scope), new NumberEncoder());
			}
		}
		
		
		mlConfiguration.setClassLabeler(new FeatureValue("aefiGOld", "Original markups", "Text", "aefiCategory"), new NumaratorEncoder());

		return mlConfiguration;
	}
	
	public static void main(String[] args) throws MalformedURLException, GateException {
		Properties props = System.getProperties();
		String gateHome = "gate.home";
		props.setProperty(gateHome, "/Applications/GATE_Developer_8.1");
		Gate.init();
		Gate.getCreoleRegister().registerComponent(WekaClassifierPR.class);

		new ProofOfConcept().run();
	}
}
