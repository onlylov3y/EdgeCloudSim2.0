package org.cloudbus.cloudsim.edge;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class CplexOptimizerExample {

	public CplexOptimizerExample() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		System.getProperty("java.library.path");
		model();
	}
	
	public static void model() {
		try {
			IloCplex cplex = new IloCplex();
			
			//varibles 
			IloNumVar x = cplex.numVar(0, Double.MAX_VALUE, "x");
			IloNumVar y = cplex.numVar(0, Double.MAX_VALUE, "y");
			
			// Expressions
			IloLinearNumExpr objective = cplex.linearNumExpr();
			objective.addTerm(0.12, x);
			objective.addTerm(0.15, y);
			
			//define objective
			cplex.addMinimize(objective);
			
			//define constrains
			cplex.addGe(cplex.sum(cplex.prod(60, x), cplex.prod(60, y)), 300);
			cplex.addGe(cplex.sum(cplex.prod(12, x), cplex.prod(6, y)), 36);
			cplex.addGe(cplex.sum(cplex.prod(10, x), cplex.prod(30, y)), 90);
			
			//solve
			cplex.solve();
			
		}catch (IloException exc){
			exc.printStackTrace();
		}
	}

}
