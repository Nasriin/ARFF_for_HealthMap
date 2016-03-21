package nasrin.phd;

import java.util.Collections;
import java.util.List;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import ir.laali.gate.ml.attributecalculator.AbstractAttributeCalculator;

@SuppressWarnings("serial")
public class SumDomainPolarities extends AbstractAttributeCalculator{
	
	private String annotationSetName;
	private String annotationType;
	private double normalization;

	/**
	 * 
	 * @param name name of the new feature in Weka
	 * @param annotationSetName name of annotation set in Gate where it is going to use as feature
	 * @param annotationType name of annotation tag in Gate
	 */
	public SumDomainPolarities(String name, String annotationSetName, String annotationType, double normalization) {
		setName(name);
		this.annotationSetName = annotationSetName;
		this.annotationType = annotationType;
		this.normalization = normalization;
	}

	@Override
	public List<String> getAttributeValue(Document doc, Annotation instance) {
		AnnotationSet aDomain = doc.getAnnotations(annotationSetName).get(annotationType, instance.getStartNode().getOffset(), instance.getEndNode().getOffset());
		double sum = 0.0;
		for (Annotation domain: aDomain){
			for (Annotation word: doc.getAnnotations().get("PolarToken", domain.getStartNode().getOffset(), domain.getEndNode().getOffset())){
				double polarity = Double.parseDouble(word.getFeatures().get("priorpolarity").toString());
				sum += polarity;
			}
		}
//		System.out.println(sum);
		return Collections.singletonList("" + sum * normalization);
	}
}
