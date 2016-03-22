package nasrin.phd;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import gate.Corpus;
import gate.CorpusController;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.persist.SerialDataStore;
import gate.util.GateException;
import gate.util.persistence.PersistenceManager;

public class CreateCorpus {
	//	private static String folderPath = "/Users/Nasrin/Documents/Concordia/Thesis_PhD/Joa/training-gate-batch-2/";
	//	private static String corpusPath = folderPath + "corpus";

	public static void main(String[] args) throws GateException, IOException {
		String installedGateDir = args[0];
		String folderPath = args[1];
		String corpusPath = args[2];
		Properties props = System.getProperties();
		String gateHome = "gate.home";
		props.setProperty(gateHome, installedGateDir);
		Gate.init();

		File file = new File(corpusPath);
		FileUtils.deleteDirectory(file);
		SerialDataStore sds = (SerialDataStore)
				Factory.createDataStore("gate.persist.SerialDataStore", file.toURI().toURL().toString());


		Corpus corpus = Factory.newCorpus("Nasrin");
		corpus = (Corpus) sds.adopt(corpus);

		//		CorpusController application =
		//				(CorpusController)PersistenceManager.loadObjectFromFile(new File(corpusPath));

		//		File directory = new File(folderPath);
		//		corpus.populate(directory.toURI().toURL(), null, null, false);
		//		corpus.sync();

		File folder = new File(folderPath);
		for(File docFile: folder.listFiles()){
			// load the document (using the specified encoding if one was given)
			System.out.println("Processing document " + docFile + "...");
			Document doc = Factory.newDocument(docFile.toURI().toURL());
			// put the document in the corpus

			corpus.add(doc);
			corpus.sync();

			if(corpus.getLRPersistenceId() != null) {
				System.out.println("CreateCorpus.main()");
				// persistent corpus -> unload the document
				corpus.unloadDocument(doc);
				Factory.deleteResource(doc);
			}

		}
		//		application.execute();
		sds.close();

	}

}
