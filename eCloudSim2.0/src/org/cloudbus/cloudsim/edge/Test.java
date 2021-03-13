
package org.cloudbus.cloudsim.edge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

	public Test() {
	}		
	
	public static Map<Integer, Integer> processSolution(int[][] sigma) {
		// hostList={<2,1>,<4,3>,<6,5>,<8,7>,<10,9>,<10,11>,<12,13>,<14,15>,<16,17>};
		int[] hostList = {1,3,5,7,9,11,13,15,17};
		Map<Integer, Integer> vmToDc = new HashMap<Integer, Integer>();
		for(int i=0; i<sigma.length; i++) {
			for (int j=0; j<sigma[i].length; j++) {
				if (j==3 && sigma[i][j] ==1) {
					vmToDc.put(j, hostList[i]);
				}
				if (j < 3 && sigma[i][j] ==1) {
					vmToDc.put(j, hostList[i]);
				}
			}		
		}		
		System.out.println(vmToDc);
		return vmToDc;		
	}
	
	public static int[][] readSigma(String output) {

		int[][] sigma = new int[9][4];
	    try {		    
		    BufferedReader br =new BufferedReader(new StringReader(output));
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
		        		//System.out.print(i);
//		        		System.out.print(num);
//		        		if (j==3) {
//		        			System.out.println();
//		        		}
		        		sigma[i][j] = Integer.parseInt(num);		        		
		        		j++;		        		
		        	}
		        	i++;		        		        	
		        }				        
			}
			br.close( );			
			
		} catch (IOException e) {
			e.printStackTrace();			
		}    
	    
	    
	    return sigma;					
	}
	
	public static Object[] processOuput(String output) {
			
		int[] vm_id = new int[4];
		int[] dc_id = new int[4];
		int[] host_id = new int[4];
		int count = 0;
	    try {
		    
		    BufferedReader br =new BufferedReader(new StringReader(output));
		    String line;
		    boolean found = false;
			while( (line = br.readLine( )) != null) {
		        if (found && count < 4) {
		        	count++;		        	
		        	vm_id[count-1] = Integer.parseInt(line.split(" ")[0]);
		        	dc_id[count-1] = Integer.parseInt(line.split(" ")[1]);
		        	host_id[count-1] = Integer.parseInt(line.split(" ")[2]);		        	
		        }
			        if (line.indexOf("vm_id") != -1) {			                
			                found = true;			                
			            }
			    }			
			br.close( );
			
		} catch (IOException e) {
			e.printStackTrace();			
		}    
	    return new Object[]{vm_id,dc_id,host_id};				
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String output = " @vm_id; dc_id; host_id\r\n" + 
				"2452 6 5\r\n" + 
				"2454 12 13\r\n" + 
				"2455 12 13\r\n" + 
				"2453 14 15\r\n" + 
				" \r\n" + 
				"// solution with objective 190\r\n" + 
				"pi = [[0 0 0 0 0 0]\r\n" + 
				"             [0 0 0 0 0 0]\r\n" + 
				"             [0 0 0 0 1 0]\r\n" + 
				"             [0 0 0 0 0 0]\r\n" + 
				"             [0 0 0 0 0 0]\r\n" + 
				"             [0 0 0 0 0 0]\r\n" + 
				"             [0 0 0 0 0 0]\r\n" + 
				"             [0 1 0 0 0 0]\r\n" + 
				"             [0 0 0 0 0 0]\r\n" + 
				"             [0 0 0 0 0 0]\r\n" + 
				"             [0 0 0 0 0 0]\r\n" + 
				"             [0 0 0 0 0 0]\r\n" + 
				"             [0 0 0 0 0 1]\r\n" + 
				"             [0 0 0 0 0 0]\r\n" + 
				"             [0 0 0 0 0 0]\r\n" + 
				"             [0 0 0 0 0 0]\r\n" + 
				"             [0 0 0 0 0 0]\r\n" + 
				"             [1 0 0 0 0 0]\r\n" + 
				"             [0 0 0 0 0 0]\r\n" + 
				"             [0 0 0 0 0 0]\r\n" + 
				"             [0 0 0 0 0 0]\r\n" + 
				"             [0 0 0 0 0 0]];\r\n" + 
				"sigma = [[0 0 0 0]\r\n" + 
				"             [0 0 0 0]\r\n" + 
				"             [0 0 0 1]\r\n" + 
				"             [0 0 0 0]\r\n" + 
				"             [0 0 0 0]\r\n" + 
				"             [0 0 0 0]\r\n" + 
				"             [0 1 1 0]\r\n" + 
				"             [1 0 0 0]\r\n" + 
				"             [0 0 0 0]];";
	
		int[][] sigma = readSigma(output);
		processSolution(sigma);
	}

}




