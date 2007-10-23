import java.util.*;
import java.io.*;
import weka.core.*;

public class Converter {

  public Converter() {
  }

  public void convertLibSVMtoArff(String sourceFilename, String targetFilename, String relationName) throws Exception {
	  BufferedReader aReader = null;

	  int numLabels = 0;
	  int numAttributes = 0;
	  int numInstances = 0;
	  double meanParsedAttributes = 0;
	  
	  // Calculate number of labels and attributes
      aReader = new BufferedReader(new FileReader(sourceFilename));
      String Line;

      while ((Line = aReader.readLine()) != null) {
    	  numInstances++;

	      StringTokenizer strTok = new StringTokenizer(Line, " ");	
	      while (strTok.hasMoreTokens()) {
	    	  String token = strTok.nextToken();
	        	
	    	  if (token.indexOf(":") == -1) {
	    		  // parse label info
	    	      StringTokenizer labelTok = new StringTokenizer(token, ",");
	    	      while (labelTok.hasMoreTokens()) {
	    	          String strLabel = labelTok.nextToken();
	    	          int intLabel = Integer.parseInt(strLabel);
	    	          if (intLabel > numLabels)
	    	        	  numLabels = intLabel;
	    	      }        		
	    	  } else {
	    		  // parse attribute info
	    		  meanParsedAttributes++;
	    		  StringTokenizer attrTok = new StringTokenizer(token, ":");
	    		  String strAttrIndex = attrTok.nextToken();
	    		  int intAttrIndex = Integer.parseInt(strAttrIndex);
	    		  if (intAttrIndex > numAttributes)
	    			  numAttributes = intAttrIndex;
	    	  }
	      }
               
      }
      numLabels++;
      aReader.close();
      
      System.out.println("Number of attributes: " + numAttributes);
      System.out.println("Number of instances: " + numInstances);
      System.out.println("Number of classes: " + numLabels);
      meanParsedAttributes /= numInstances;
      boolean Sparse = false;
      if (meanParsedAttributes < numAttributes)
      {
    	  Sparse = true;
    	  System.out.println("Dataset is sparse.");
      }
      
      // Define Instances class to hold data
      FastVector attInfo = new FastVector(numAttributes+numLabels);
      Attribute[] att = new Attribute[numAttributes+numLabels];

      for (int i=0; i<numAttributes; i++) {
    	  att[i] = new Attribute("Att"+(i+1));
    	  attInfo.addElement(att[i]);
      }
      FastVector ClassValues = new FastVector(2);
      ClassValues.addElement("0");
      ClassValues.addElement("1");
      for (int i=0; i<numLabels; i++) {
    	  att[numAttributes+i] = new Attribute("Class"+(i+1), ClassValues);
    	  attInfo.addElement(att[numAttributes+i]);
      }

      // Re-read file and convert into multi-label arff
      int countInstances = 0;

      BufferedWriter aWriter = new BufferedWriter(new FileWriter(targetFilename));
      Instances data = new Instances(relationName, attInfo, 0);
      aWriter.write(data.toString());

      aReader = new BufferedReader(new FileReader(sourceFilename));

      while ((Line = aReader.readLine()) != null) {
    	  countInstances++;

    	  // set all  values to 0
    	  double[] attValues = new double[numAttributes+numLabels];
    	  Arrays.fill(attValues, 0);
    	  Instance tempInstance = new Instance(1, attValues);
    	  tempInstance.setDataset(data);

    	  // separate class info from attribute info
    	  // ensure class info exists
    	  StringTokenizer strTok = new StringTokenizer(Line, " ");

    	  while (strTok.hasMoreTokens()) {
    		  String token = strTok.nextToken();
    		  
    		  if (token.indexOf(":") == -1) {
        		  // parse label info
        		  StringTokenizer labelTok = new StringTokenizer(token, ",");
        		  while (labelTok.hasMoreTokens()) {
        			  String strLabel = labelTok.nextToken();
        			  int intLabel = Integer.parseInt(strLabel);
        			  tempInstance.setValue(numAttributes+intLabel,1);
        		  }    			  
    		  } else {
    			  // parse attribute info
   		    	  StringTokenizer AttrTok = new StringTokenizer(token, ":");
   		    	  String strAttrIndex = AttrTok.nextToken();
   		    	  String strAttrValue = AttrTok.nextToken();
   		    	  tempInstance.setValue(Integer.parseInt(strAttrIndex)-1,Double.parseDouble(strAttrValue));
    		  }
    	  }
        
    	  if (Sparse) {
	    	  SparseInstance tempSparseInstance = new SparseInstance(tempInstance);
	    	  aWriter.write(tempSparseInstance.toString() + "\n");
    	  } else {
    	  	  aWriter.write(tempInstance.toString() + "\n");
	  	  }
    		  
      }
      aWriter.close();
  }

  public static void main(String[] args) throws Exception {
    Converter test = new Converter();
    String path = "e:/work/datasets/multilabel/rcv1v2/subset/";
    String sourceFilename = "rcv1v2subset1.svm";
    String targetFilename = "rcv1v2subset1.arff";
    String relationName = "rcv1v2subset1";
    test.convertLibSVMtoArff(path+sourceFilename, path+targetFilename, relationName);
  }
}

