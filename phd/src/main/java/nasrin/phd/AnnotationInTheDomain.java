package nasrin.phd;

import java.util.Collections;
import java.util.List;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import ir.laali.gate.ml.attributecalculator.AbstractAttributeCalculator;
import ir.laali.gate.ml.attributecalculator.AttributeCalculator;

public class AnnotationInTheDomain extends AbstractAttributeCalculator {

	String annotationSetName; 
	String annotationType_tense; 
	String annotationSetName_scope;
	String annotationType_scope;
	/**
	 * 
	 */

	public AnnotationInTheDomain(String name, String annotationSetName, String annotationType_tense, 
			String annotationSetName_scope, String annotationType_scope) {
		setName(name);
		this.annotationSetName = annotationSetName;
		this.annotationType_tense = annotationType_tense;
		this.annotationSetName_scope = annotationSetName_scope;
		this.annotationType_scope = annotationType_scope;
	}



	@Override
	public List<String> getAttributeValue(Document doc, Annotation instance) {
		
//		AnnotationSet targetAnnotations = doc.getAnnotations(annotationSetName).get(annotationType , instance.getStartNode()
//				.getOffset(), instance.getEndNode().getOffset());
		
		
		AnnotationSet inputAnnotationSet = doc.getAnnotations();
		AnnotationSet scopes = inputAnnotationSet.get(annotationType_scope , instance.getStartNode()
				.getOffset(), instance.getEndNode().getOffset());
		AnnotationSet tenses = inputAnnotationSet.get(annotationType_tense , instance.getStartNode()
				.getOffset(), instance.getEndNode().getOffset());
		int cnt = 0;
		for(Annotation scope: scopes){
			for (Annotation tense: tenses){
				if (intersection(scope, tense)){
					++cnt;
				}
			}
		}
			
		return Collections.singletonList(""+cnt);
	}



	private boolean intersection(Annotation scope, Annotation tense) {
		return isBetween(scope.getStartNode().getOffset(), tense.getStartNode().getOffset(), tense.getEndNode().getOffset()) ||
				isBetween(scope.getEndNode().getOffset(), tense.getStartNode().getOffset(), tense.getEndNode().getOffset());
	}



	private boolean isBetween(Long target, Long start, Long end) {
		return (start <= target) && (target <= end);
	}
}
