package org.cloudbus.cloudsim.edge;

public class CplexModel {

	public CplexModel() {
	}
	/*********************************************
	 * OPL 12.6.0.0 Model
	 * Author: Constantinos Vassilakis
	 * Creation Date: 18, Dec 2017
	 *********************************************/	
	public String model_min_delay() {
		//Use constraint programming engine (CP)
		StringBuilder md = new StringBuilder("using CP;"); md.append(System.lineSeparator());	
		// *** Declarations ***
		// a tuple for representing application tier - component  
		md.append("tuple tier {key int tierid;}");	md.append(System.lineSeparator());		
		// a tuple for representing channels (between application tiers)
		md.append("tuple channel {key tier fromtier;key tier totier;int channelid;}"); md.append(System.lineSeparator());
		// a tuple for representing a host - hypervisor  
		md.append("tuple host {key int dcid;key int hostid;}"); md.append(System.lineSeparator());
		// a tuple for representing links (between hosts) 
		md.append("tuple link {key host fromhost;key host tohost;int linkid;}"); md.append(System.lineSeparator());
		// external data - loading values from .dat
		md.append("{tier} T=...;"); md.append(System.lineSeparator());
		md.append("{host} H=...;"); md.append(System.lineSeparator());
		md.append("{channel} C=...;"); md.append(System.lineSeparator());
		md.append("{link} L=...;"); md.append(System.lineSeparator());		
		// Static placed tiers - subset of T
 		md.append("{tier} S=...;"); md.append(System.lineSeparator());
		// hosts where 1-1 static placed apps are actually placed - subset of H
		md.append("{host} HS=...;"); md.append(System.lineSeparator());
		// host used only for hosting UserVMs and nothing else
		md.append("{host} UH=...;"); md.append(System.lineSeparator());
		//set of unique resource offered by a host e.g. CPU, MEM
		md.append("{string} R=...;"); md.append(System.lineSeparator());
		//set of unique resources offered by a link e.g. bw gold, bw silver, ?ports?
		md.append("{string} Rhyphen=...;"); md.append(System.lineSeparator());
		//set of monitored metrics at hosts
		md.append("{string} M=...;"); md.append(System.lineSeparator());
		//set of monitored metrics at links
		md.append("{string} Mhyphen=...;"); md.append(System.lineSeparator());
		// host app in S
		md.append("{tier} shat=...;"); md.append(System.lineSeparator());
		// hosting device where host app is located in HS
		md.append("{host} hhat=...;"); md.append(System.lineSeparator());
		// amount of of resource r demand by application task t
		md.append("int a[R][T]=...;"); md.append(System.lineSeparator());
		// amount of resource r eg. bw required by channel 
		md.append("int c[Rhyphen][C]=...; "); md.append(System.lineSeparator());
		// amount of resource r available at host h
		md.append("int beta[R][H]=...; "); md.append(System.lineSeparator());
		// initial capacity of resource r available at host h
		md.append("int ibeta[R][H]=...;"); md.append(System.lineSeparator());
		//measured value of metric k at host h
		md.append("int m[M][H]=...;"); md.append(System.lineSeparator());
		//acceptable upper bound to the value of  metric k for an application task t to be assigned to a host; 
		//**** captures Boolean as well
		md.append("int wu[M][T]=...;"); md.append(System.lineSeparator());
		//acceptable lower bound to the value of  metric k for an application task t to be assigned to a host
		md.append("int wl[M][T]=...;"); md.append(System.lineSeparator());
		// amount of resource r eg. bw available at link
		md.append("int b[Rhyphen][L]=...; "); md.append(System.lineSeparator());
		//measured value of metric k at at link <u,v>;   
		md.append("int mi[Mhyphen][L]=...;"); md.append(System.lineSeparator());
		//acceptable upper bound to the value of  metric k for a channel <s,d> to be routed through a link;
		md.append("int zu[Mhyphen][C]=...; "); md.append(System.lineSeparator());
		//acceptable lower bound to the value of  metric k for a channel <s,d> to be routed through a link;
		md.append("int zl[Mhyphen][C]=...; "); md.append(System.lineSeparator());		
		// PARALELISM
		// specifies the number of threads 
		// in the settings file - .ops  is in Search control, general, number of workers
		// values in 1 to cores (was up to 4 in earlier opl cplex versions) or -1 to parallelize to all cores
		md.append("execute {cp.param.workers=-1;}"); md.append(System.lineSeparator());
		
		// *** Opt problem section 
		// Decision variables 
		// tier placement to hosts
		md.append("dvar boolean sigma[H][T]; "); md.append(System.lineSeparator());
		// channel routing through links
		md.append("dvar boolean pi[L][C]; "); md.append(System.lineSeparator());
		// Decision expression - Objective
		// Obj1 -> Response -> min total communication delay
		md.append("dexpr float Cost= sum(l in L, ch in C) (pi[l][ch]*mi[\"Delay\"][l]);"); md.append(System.lineSeparator());
		md.append("minimize Cost;"); md.append(System.lineSeparator());
		md.append("subject to {"); md.append(System.lineSeparator());
		// a task is assigned only to one host 
		md.append("forall (t in T)"); md.append(System.lineSeparator());
		md.append("ct1:"); md.append(System.lineSeparator());
		md.append("sum (h in H) sigma[h][t]==1;"); md.append(System.lineSeparator());
		// Static application components placement
		md.append("forall (i in 0..card(S)-1)"); md.append(System.lineSeparator());
		md.append("ct2:"); md.append(System.lineSeparator());
		// 1-1 
		md.append("sigma[item(HS,i)][item(S,i)]==1;"); md.append(System.lineSeparator());
		// new-> Host in UH hosts are used only to host UserVMs and nothing else
		md.append("forall (h in UH, t in T:t not in shat)"); md.append(System.lineSeparator());
		md.append("ct3:"); md.append(System.lineSeparator());
		md.append("sigma[h][t]==0;"); md.append(System.lineSeparator());
		// resource constraints 
		md.append("forall (h in H, r in R)"); md.append(System.lineSeparator());
		md.append("ct4:"); md.append(System.lineSeparator());
		md.append("sum (t in T) sigma[h][t]*a[r][t]<=beta[r][h];"); md.append(System.lineSeparator());
		// hosting device at user side hosts only the user application 
		md.append("forall (t in T, h in hhat)"); md.append(System.lineSeparator());
		md.append("ct5:"); md.append(System.lineSeparator());
		// must have enough to host static app
		md.append("sum (t in T) sigma[h][t]==1;"); md.append(System.lineSeparator());
		// host metrics constraints
		md.append("forall (k in M, h in H, t in T)"); md.append(System.lineSeparator());
		md.append("ct6:"); md.append(System.lineSeparator());
		md.append("sigma[h][t]*m[k][h]>=sigma[h][t]*wl[k][t];"); md.append(System.lineSeparator());
		md.append("forall (k in M, h in H, t in T)"); md.append(System.lineSeparator());
		md.append("ct7:"); md.append(System.lineSeparator());
		md.append("sigma[h][t]*m[k][h]<=sigma[h][t]*wu[k][t];"); md.append(System.lineSeparator());
		// link resource constraints 
		md.append("forall (l in L, r in Rhyphen)"); md.append(System.lineSeparator());
		md.append("ct8:"); md.append(System.lineSeparator());
		md.append("sum (ch in C) pi[l][ch]*c[r][ch]<=b[r][l]; "); md.append(System.lineSeparator());
		// flow conservation - unsplittable flow - collocation support
		// captures if intermediate and if source or destination
		md.append("forall (ch in C, h in H) "); md.append(System.lineSeparator());
		md.append("ct9:"); md.append(System.lineSeparator());
		// if h source no incoming u
		md.append("sum (u in H: (<u,h> in L)) pi[<u,h>][ch] + sigma[h][ch.fromtier]=="); md.append(System.lineSeparator());
		// if h destination no outgoing v
		md.append("sum (v in H: (<h,v> in L)) pi[<h,v>][ch] + sigma[h][ch.totier] ;"); md.append(System.lineSeparator());
		// no loop before reaching destination
		md.append("forall (ch in C, h in H)"); md.append(System.lineSeparator());
		md.append("ct10:"); md.append(System.lineSeparator());
		md.append("sum (u in H: (<u,h> in L)) sigma[h][ch.fromtier]*pi[<u,h>][ch]==0;"); md.append(System.lineSeparator());
		// link metrics constraints
		md.append("forall (k in Mhyphen, l in L, ch in C)"); md.append(System.lineSeparator());
		md.append("ct11:"); md.append(System.lineSeparator());
		md.append("pi[l][ch]*mi[k][l]>=pi[l][ch]*zl[k][ch];"); md.append(System.lineSeparator());
		md.append("forall (k in Mhyphen, l in L, ch in C)"); md.append(System.lineSeparator());
		md.append("ct12:"); md.append(System.lineSeparator());
		md.append("pi[l][ch]*mi[k][l]<=pi[l][ch]*zu[k][ch];"); md.append(System.lineSeparator());
		// provides that bidirectional communication between two tasks 
		// is routed through the same bidirectional overlay path; upstream 
		// and downstream of a flow is not routed separately 		
		md.append("forall (l1,l2 in L:l1.fromhost==l2.tohost && l2.fromhost==l1.tohost, ");
		md.append("ch1,ch2 in C:ch1.fromtier==ch2.totier && ch2.fromtier==ch1.totier )"); md.append(System.lineSeparator());
		md.append("ct13:"); md.append(System.lineSeparator());
		md.append("pi[l1][ch1]==pi[l2][ch2];}"); md.append(System.lineSeparator());
		// *** postprocessing section 
		md.append("execute{"); md.append(System.lineSeparator());
		// VM to DC HOST assignment
		md.append("writeln(\"@vm_id; dc_id; host_id\")"); md.append(System.lineSeparator());
		md.append("for(var i in thisOplModel.H){"); md.append(System.lineSeparator());
		md.append("for(var j in thisOplModel.T){"); md.append(System.lineSeparator());
		md.append("if(thisOplModel.sigma[i][j]==1)"); md.append(System.lineSeparator());
		md.append("writeln(j.tierid+\" \"+i.dcid+\" \"+i.hostid); }}"); md.append(System.lineSeparator());
		md.append("writeln(\" \");}");		
		return md.toString();
	}
	/*********************************************
	 * OPL 12.6.0.0 Model
	 * Author: Constantinos Vassilakis
	 * Creation Date: 18 Dec. 2017
	 *********************************************/		
	public String model_max_resources() {
		//Use constraint programming engine (CP)
		StringBuilder mr = new StringBuilder("using CP;"); mr.append(System.lineSeparator());	
		// *** Declarations ***
		// a tuple for representing application tier - component  
		mr.append("tuple tier {key int tierid;}");	mr.append(System.lineSeparator());		
		// a tuple for representing channels (between application tiers)
		mr.append("tuple channel {key tier fromtier;key tier totier;int channelid;}"); mr.append(System.lineSeparator());
		// a tuple for representing a host - hypervisor  
		mr.append("tuple host {key int dcid;key int hostid;}"); mr.append(System.lineSeparator());
		// a tuple for representing links (between hosts) 
		mr.append("tuple link {key host fromhost;key host tohost;int linkid;}"); mr.append(System.lineSeparator());
		// external data - loading values from .dat
		mr.append("{tier} T=...;"); mr.append(System.lineSeparator());
		mr.append("{host} H=...;"); mr.append(System.lineSeparator());
		mr.append("{channel} C=...;"); mr.append(System.lineSeparator());
		mr.append("{link} L=...;"); mr.append(System.lineSeparator());		
		// Static placed tiers - subset of T
 		mr.append("{tier} S=...;"); mr.append(System.lineSeparator());
		// hosts where 1-1 static placed apps are actually placed - subset of H
		mr.append("{host} HS=...;"); mr.append(System.lineSeparator());
		// host used only for hosting UserVMs and nothing else
		mr.append("{host} UH=...;"); mr.append(System.lineSeparator());
		//set of unique resource offered by a host e.g. CPU, MEM
		mr.append("{string} R=...;"); mr.append(System.lineSeparator());
		//set of unique resources offered by a link e.g. bw gold, bw silver, ?ports?
		mr.append("{string} Rhyphen=...;"); mr.append(System.lineSeparator());
		//set of monitored metrics at hosts
		mr.append("{string} M=...;"); mr.append(System.lineSeparator());
		//set of monitored metrics at links
		mr.append("{string} Mhyphen=...;"); mr.append(System.lineSeparator());
		// host app in S
		mr.append("{tier} shat=...;"); mr.append(System.lineSeparator());
		// hosting device where host app is located in HS
		mr.append("{host} hhat=...;"); mr.append(System.lineSeparator());
		// amount of of resource r demand by application task t
		mr.append("int a[R][T]=...;"); mr.append(System.lineSeparator());
		// amount of resource r eg. bw required by channel 
		mr.append("int c[Rhyphen][C]=...; "); mr.append(System.lineSeparator());
		// amount of resource r available at host h
		mr.append("int beta[R][H]=...; "); mr.append(System.lineSeparator());
		// initial capacity of resource r available at host h
		mr.append("int ibeta[R][H]=...;"); mr.append(System.lineSeparator());
		//measured value of metric k at host h
		mr.append("int m[M][H]=...;"); mr.append(System.lineSeparator());
		//acceptable upper bound to the value of  metric k for an application task t to be assigned to a host; 
		//**** captures Boolean as well
		mr.append("int wu[M][T]=...;"); mr.append(System.lineSeparator());
		//acceptable lower bound to the value of  metric k for an application task t to be assigned to a host
		mr.append("int wl[M][T]=...;"); mr.append(System.lineSeparator());
		// amount of resource r eg. bw available at link
		mr.append("int b[Rhyphen][L]=...; "); mr.append(System.lineSeparator());
		//measured value of metric k at at link <u,v>;   
		mr.append("int mi[Mhyphen][L]=...;"); mr.append(System.lineSeparator());
		//acceptable upper bound to the value of  metric k for a channel <s,d> to be routed through a link;
		mr.append("int zu[Mhyphen][C]=...; "); mr.append(System.lineSeparator());
		//acceptable lower bound to the value of  metric k for a channel <s,d> to be routed through a link;
		mr.append("int zl[Mhyphen][C]=...; "); mr.append(System.lineSeparator());		
		// PARALELISM
		// specifies the number of threads 
		// in the settings file - .ops  is in Search control, general, number of workers
		// values in 1 to cores (was up to 4 in earlier opl cplex versions) or -1 to parallelize to all cores
		mr.append("execute {cp.param.workers=-1;}"); mr.append(System.lineSeparator());
		
		// *** Opt problem section 
		// Decision variables 
		// tier placement to hosts
		mr.append("dvar boolean sigma[H][T]; "); mr.append(System.lineSeparator());
		// channel routing through links
		mr.append("dvar boolean pi[L][C]; "); mr.append(System.lineSeparator());
		// Decision expression - Objective		
		// Obj2 -> consolidation -> minimizes the number of hosts used with a preference to use the most utilized
		// so indirectly to leave those with zero utilization or low utilization unused in a current placement 
		// --- count the number of hosts used : sum(h in H) minl((sum(t in T) sigma[h][t]),1)
		// -- sum of availabilities percentages of hosts used before placement: (beta["CPU"][h]*100/ibeta["CPU"][h])
		// -- no precaution has been taken for which host will be initially selected, all have equal probability to be selected
		mr.append("dexpr float Cost=sum(h in H) (minl((sum(t in T) sigma[h][t]),1)*(beta[\"CPU\"][h]*100/ibeta[\"CPU\"][h]) );"); 
		mr.append(System.lineSeparator());
		mr.append("minimize Cost;"); mr.append(System.lineSeparator());
		mr.append("subject to {"); mr.append(System.lineSeparator());
		// a task is assigned only to one host 
		mr.append("forall (t in T)"); mr.append(System.lineSeparator());
		mr.append("ct1:"); mr.append(System.lineSeparator());
		mr.append("sum (h in H) sigma[h][t]==1;"); mr.append(System.lineSeparator());
		// Static application components placement
		mr.append("forall (i in 0..card(S)-1)"); mr.append(System.lineSeparator());
		mr.append("ct2:"); mr.append(System.lineSeparator());
		// 1-1
		mr.append("sigma[item(HS,i)][item(S,i)]==1;"); mr.append(System.lineSeparator());
		// new-> Host in UH hosts are used only to host UserVMs and nothing else
		mr.append("forall (h in UH, t in T:t not in shat)"); mr.append(System.lineSeparator());
		mr.append("ct3:"); mr.append(System.lineSeparator());
		mr.append("sigma[h][t]==0;"); mr.append(System.lineSeparator());
		// resource constraints 
		mr.append("forall (h in H, r in R)"); mr.append(System.lineSeparator());
		mr.append("ct4:"); mr.append(System.lineSeparator());
		mr.append("sum (t in T) sigma[h][t]*a[r][t]<=beta[r][h];"); mr.append(System.lineSeparator());
		// hosting device at user side hosts only the user application 
		mr.append("forall (t in T, h in hhat)"); mr.append(System.lineSeparator());
		mr.append("ct5:"); mr.append(System.lineSeparator());
		 // must have enough to host static app
		mr.append("sum (t in T) sigma[h][t]==1;"); mr.append(System.lineSeparator());
		// host metrics constraints
		mr.append("forall (k in M, h in H, t in T)"); mr.append(System.lineSeparator());
		mr.append("ct6:"); mr.append(System.lineSeparator());
		mr.append("sigma[h][t]*m[k][h]>=sigma[h][t]*wl[k][t];"); mr.append(System.lineSeparator());
		mr.append("forall (k in M, h in H, t in T)"); mr.append(System.lineSeparator());
		mr.append("ct7:"); mr.append(System.lineSeparator());
		mr.append("sigma[h][t]*m[k][h]<=sigma[h][t]*wu[k][t];"); mr.append(System.lineSeparator());
		// link resource constraints 
		mr.append("forall (l in L, r in Rhyphen)"); mr.append(System.lineSeparator());
		mr.append("ct8:"); mr.append(System.lineSeparator());
		mr.append("sum (ch in C) pi[l][ch]*c[r][ch]<=b[r][l];"); mr.append(System.lineSeparator());
		// flow conservation - unsplittable flow - collocation support
		// captures if intermediate and if source or destination
		mr.append("forall (ch in C, h in H)"); mr.append(System.lineSeparator());
		mr.append("ct9:"); mr.append(System.lineSeparator());
		// if h source no incoming u
		mr.append("sum (u in H: (<u,h> in L)) pi[<u,h>][ch] + sigma[h][ch.fromtier]== "); mr.append(System.lineSeparator());
		// if h destination no outgoing v
		mr.append("sum (v in H: (<h,v> in L)) pi[<h,v>][ch] + sigma[h][ch.totier] ;"); mr.append(System.lineSeparator());
		// no loop before reaching destination
		mr.append("forall (ch in C, h in H)"); mr.append(System.lineSeparator());
		mr.append("ct10:"); mr.append(System.lineSeparator());
		mr.append("sum (u in H: (<u,h> in L)) sigma[h][ch.fromtier]*pi[<u,h>][ch]==0; "); mr.append(System.lineSeparator());
		// link metrics constraints 
		mr.append("forall (k in Mhyphen, l in L, ch in C)"); mr.append(System.lineSeparator());
		mr.append("ct11:"); mr.append(System.lineSeparator());
		mr.append("pi[l][ch]*mi[k][l]>=pi[l][ch]*zl[k][ch];"); mr.append(System.lineSeparator());
		mr.append("forall (k in Mhyphen, l in L, ch in C)"); mr.append(System.lineSeparator());
		mr.append("ct12:"); mr.append(System.lineSeparator());
		mr.append("pi[l][ch]*mi[k][l]<=pi[l][ch]*zu[k][ch];"); mr.append(System.lineSeparator());		 
		// provides that bidirectional communication between two tasks 
		// is routed through the same bidirectional overlay path; upstream 
		// and downstream of a flow is not routed separately 		
		mr.append("forall (l1,l2 in L:l1.fromhost==l2.tohost && l2.fromhost==l1.tohost,");
		mr.append("ch1,ch2 in C:ch1.fromtier==ch2.totier && ch2.fromtier==ch1.totier)"); mr.append(System.lineSeparator());
		mr.append("ct13:"); mr.append(System.lineSeparator());
		mr.append("pi[l1][ch1]==pi[l2][ch2];}"); mr.append(System.lineSeparator());
		// *** postprocessing section
		mr.append("execute{"); mr.append(System.lineSeparator());
		// VM to DC HOST assignment
		mr.append("writeln(\"@vm_id; dc_id; host_id\")"); mr.append(System.lineSeparator());
		mr.append("for(var i in thisOplModel.H){"); mr.append(System.lineSeparator());
		mr.append("for(var j in thisOplModel.T){"); mr.append(System.lineSeparator());
		mr.append("if(thisOplModel.sigma[i][j]==1)"); mr.append(System.lineSeparator());
		mr.append("writeln(j.tierid+\" \"+i.dcid+\" \"+i.hostid); }}"); mr.append(System.lineSeparator());
		mr.append("writeln(\" \");}"); mr.append(System.lineSeparator());
	
		return mr.toString();
	}

}
