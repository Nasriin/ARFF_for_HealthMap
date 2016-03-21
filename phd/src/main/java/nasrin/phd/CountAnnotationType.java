package nasrin.phd;

import java.util.Collections;
import java.util.List;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import ir.laali.gate.ml.attributecalculator.AbstractAttributeCalculator;

@SuppressWarnings("serial")
public class CountAnnotationType extends AbstractAttributeCalculator{
	private String annotationType;
	private String annotationSetName;
	
	/**
	 * 
	 * @param name It is the name of the new feature in Weka
	 * @param annotationSetName It is the name of annotation set in Gate where the element planning to use as feature sits there
	 * @param annotationType It is the name of annotation in Gate where the element planning to use as feature sits there
	 */
	public CountAnnotationType(String name, String annotationSetName, String annotationType) {
		setName(name);
		this.annotationSetName = annotationSetName;
		this.annotationType = annotationType;
	}
	@Override
	public List<String> getAttributeValue(Document doc, Annotation instance) {
		AnnotationSet targetAnnotations = doc.getAnnotations(annotationSetName).get(annotationType , instance.getStartNode().getOffset(), instance.getEndNode().getOffset());
		return Collections.singletonList("" + targetAnnotations.size());
	}

}
