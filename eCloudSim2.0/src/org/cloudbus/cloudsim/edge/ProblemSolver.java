package org.cloudbus.cloudsim.edge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.edge.util.CustomLog;

import ilog.concert.IloException;
import ilog.cp.IloCP;
import ilog.opl.*;

public class ProblemSolver {

	public ProblemSolver() {
		// TODO Auto-generated constructor stub
	}

	public static Map<Integer, Integer> processSolution(int[][] sigma, Object[] ob) {
		// hostList={<2,1>,<4,3>,<6,5>,<8,7>,<10,9>,<10,11>,<12,13>,<14,15>,<16,17>};
		int[] userVm = (int[]) ob[5];
		int[] vm1 = (int[]) ob[6];
		int[] vm2 = (int[]) ob[7];
		int[] vm3 = (int[]) ob[8];		
		Map<Integer, Integer> vmToDc = new HashMap<Integer, Integer>();
		if (sigma != null) {
			int[] vmList = {vm1[2],vm2[2],vm3[2],userVm[4]};
			int[] dcList = {2,4,6,8,10,10,12,14,16};
			for(int i=0; i<sigma.length; i++) {
				for (int j=0; j<sigma[i].length; j++) {
					if (j==3 && sigma[i][j] ==1) {
						vmToDc.put(vmList[j], dcList[i]);
					}
					if (j < 3 && sigma[i][j] ==1) {
						vmToDc.put(vmList[j], dcList[i]);
					}
				}
			}
			CustomLog.printServiceChain("%s\t%s", CloudSim.clock(), 
					"[CPLEX SOLUTION]" +
							"#" + vmToDc.get(userVm[4])+"."+userVm[4] + " <=> " +
							"#" + vmToDc.get(vm1[2])+"."+vm1[2] + " <=> " + 
							"#" + vmToDc.get(vm2[2])+"."+vm2[2] + " <=> " +
							"#" + vmToDc.get(vm3[2])+"."+vm3[2]);
//			System.out.println(					"[CPLEX SOLUTION] " +
//							"#" + vmToDc.get(userVm[4])+"."+userVm[4] + " <=> " +
//							"#" + vmToDc.get(vm1[2])+"."+vm1[2] + " <=> " + 
//							"#" + vmToDc.get(vm2[2])+"."+vm2[2] + " <=> " +
//							"#" + vmToDc.get(vm3[2])+"."+vm3[2]);
		} else {
			vmToDc = null;
			CustomLog.printServiceChain("%s\t%s", CloudSim.clock(), "[NO SOLUTION FOUND]");						
		}		
		//System.out.println(vmToDc);
//		vmToDc.put(4, vm1[2]);
//		vmToDc.put(5, vm2[2]);
//		vmToDc.put(6, vm3[2]);
		return vmToDc;		
	}
	
	public static int[][] readSigma(String string) {

		int[][] sigma = new int[9][4];
	    try {		    
		    BufferedReader br =new BufferedReader(new StringReader(string));
		    String line;
		    boolean found = false;
		    int i = 0; int count = 0;
			while( (line = br.readLine( )) != null) {
		        if (line.indexOf("sigma") != -1) {	
		        	found = true;			                
			    } else if (line.indexOf("pi") != -1 || count > 9) {
			    	found = false;
			    }
		        if (found) {
		        	count += 1;
		        	String num;
		        	String regex ="(\\d+)";
		        	Matcher matcher = Pattern.compile(regex).matcher(line);		        	
		        	int j = 0;
		        	while (matcher.find( )){		        		
		        		num = matcher.group();		        		
		        		sigma[i][j] = Integer.parseInt(num);		        		
		        		j++;		        		
		        	}
		        	i++;
		        }			        
			}
			br.close( );
//			if (found == false) {
//				sigma = null;
//			}
		} catch (IOException e) {
			e.printStackTrace();			
		}    	    	    
	    return sigma;					
	}
	
//	public static Map<Integer, Integer> main(String[] args) {
	public Map<Integer, Integer> findAvailableDatacenter(Object[] ob) {       
		Map<Integer, Integer> results = null;
		// An instance of the environment in which to create model objects
		IloOplFactory oplF = new IloOplFactory();
		// Create an error handler is necessary in the environment to report errors 
		// and warnings during the translation of the model text.
		IloOplErrorHandler errHandler = oplF.createOplErrorHandler();
		//  Adds a settings configuration to this OPL model
		IloOplSettings settings = oplF.createOplSettings(errHandler);
		
		//Pass the model source that provides the text to interpret.
		CplexModel cm = new CplexModel();
		IloOplModelSource modelSource = oplF.createOplModelSourceFromString(cm.model_min_delay(), "INPUT_Placement_model");
		
		// Use same model definition to instantiate one or more models.
		IloOplModelDefinition def = oplF.createOplModelDefinition(modelSource, settings);
		try {
			// Create the instance of the algorithm to use for this model.
			// If the model is to be solved by CP Optimizer engine, you would 
			// instantiate an IloCP object using: IloCP cp = oplF.createCP()
			// otherwise, use: 
			// IloCplex cplex = oplF.createCplex();
			IloCP cp = oplF.createCP();						
			// You can now create the OPL model. The constructor takes a 
			// model definition instance and an instance of IloCplex.
			IloOplModel opl = oplF.createOplModel(def, cp);
			// In order to generate the Concert model, you need to provide data
			// Provide data source from file
	        //IloOplDataSource dataSource = oplF.createOplDataSource(cplexData);
	        // Provide data source from text			
	        CplexData cd = new CplexData();
	        String dataSourceText = cd.processDataMinDelay(ob);
	        IloOplDataSource dataSource = oplF.createOplDataSourceFromString(dataSourceText, "INPUT_Placement_data");
	        
	        opl.addDataSource(dataSource);	       
	        // Once you have specified your data source, you can generate the Concert model.
	        opl.generate();
	        // You can solve the model in the usual way with Concert Technology.
	        if (cp.solve()) {
	        	
	            OutputStream output = new OutputStream()
	            {
	                private StringBuilder string = new StringBuilder();
	                @Override
	                public void write(int b) throws IOException {
	                    this.string.append((char) b );
	                }
	                public String toString(){
	                    return this.string.toString();
	                }
	            };
	            opl.postProcess();
	            opl.printSolution(output);
	            //System.out.println(output.toString());
	    		int[][] sigma = readSigma(output.toString());
	    		
	    		results = processSolution(sigma, ob);
	        }
		} catch (IloException e) {
			e.printStackTrace();
		}		
		return results;
	}		
}
