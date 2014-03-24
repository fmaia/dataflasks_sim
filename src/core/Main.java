/*
Copyright (c) 2014.

Universidade do Minho
Francisco Maia

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/
package core;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;

public class Main {

	public static HashMap<Integer,NodeInterface> nodelist;
	public static int cycles;
	public static int nodes;
	public static int lastid;
	public static int replicationfactor;
	public static int viewsize;
	public static Random grnd;
	public static String churntype;
	public static int nchurn;
	public static int churnstart;
	public static int churnstop;
	public static int maxage;
	public static boolean local;
	public static int localinterval;
	public static boolean flex;
	public static int replicationfactorMax;
	
	private static class Worker extends Thread{
		private NodeInterface p;
		private ArrayList<Reference> msg;
		private int cycle;
		public Worker(NodeInterface p, ArrayList<Reference> msg,int cycle){
			this.p = p;
			this.msg = msg;
			this.cycle = cycle;
		}
		public void run(){
			p.receiveMessage(msg,cycle);
		}
	}
	
	public static void main(String[] args) {
		
		lastid = 0;
		//LOADING SIMULATION PROPERTIES
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream("config.properties"));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		cycles = Integer.parseInt(prop.getProperty("cycles"));
		nodes = Integer.parseInt(prop.getProperty("nodes"));
		replicationfactor = Integer.parseInt(prop.getProperty("replicationfactor"));
		viewsize = Integer.parseInt(prop.getProperty("viewsize"));
		maxage = Integer.parseInt(prop.getProperty("maxage"));
		churntype = prop.getProperty("churntype");
		nchurn = Integer.parseInt(prop.getProperty("nchurn"));
		churnstart = Integer.parseInt(prop.getProperty("churnstart"));
		churnstop = Integer.parseInt(prop.getProperty("churnstop"));
		boolean singlechurn = (churnstop==churnstart);
		local = Boolean.parseBoolean(prop.getProperty("local"));
		localinterval = Integer.parseInt(prop.getProperty("localinterval"));
		flex = Boolean.parseBoolean(prop.getProperty("flex"));
		replicationfactorMax = Integer.parseInt(prop.getProperty("replicationfactorMax"));
		
		grnd = new Random();
		nodelist = new HashMap<Integer,NodeInterface>();
		
		//Adding initial nodes
		for(int i=1;i<=nodes;i++){
			addNode();
		}

		//Simulation Core
		int cycle = 1;
		while(cycle<=cycles){
			
			System.out.println("Cycle "+cycle);
			
			FileWriter fstream;
			try {
				fstream = new FileWriter(cycle+".txt");
				BufferedWriter fout = new BufferedWriter(fstream);
				
				
				//Couting the alive nodes at this cycle
				ArrayList<Integer> aliveids = new ArrayList<Integer>();
				int alsize = 0;
				for(Integer aid : nodelist.keySet()){
					aliveids.add(aid);
					alsize = alsize + 1;
				}
				ArrayList<Worker> threads = new ArrayList<Worker>();
				//For each alive node send a new PSS message
				for(int i : nodelist.keySet()){
					
					ArrayList<Reference> message = getRandomView(viewsize,grnd,i,aliveids,alsize);
					NodeInterface current = nodelist.get(i);
					Worker a = new Worker(current,message,cycle);
					threads.add(a);
					a.start();
					//System.out.println("Worker Launched for id" + i + " in cycle "+cycle);
				}
				for(Worker w : threads){
					try {
						w.join();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				for(int i : nodelist.keySet()){
					NodeInterface current = nodelist.get(i);
					//Logging node state after processing message
					fout.write(i+" "+current.getGroup()+" "+current.getNgroups());
					fout.write("\n");
				}
				fout.close();
				
				//CHURN---------------------------------------------------------------
				if(churnstart!=0 && cycle>=churnstart && cycle<=churnstop){
					if(churntype.equals("remove") || churntype.equals("both")){
						int nnodes = nodelist.size();
						for(int j=1;j<=nnodes;j++) {
							//aliveids = new ArrayList<Integer>();
							//alsize = 0;
							//for(Integer aid : nodelist.keySet()){
							//	aliveids.add(aid);
							//	alsize = alsize + 1;
							//}
							//removeRandomNode(aliveids,alsize);
							if(j%2==0){
								nodelist.remove(j);
							}
						}
					}
					if(churntype.equals("add") || churntype.equals("both")){
						for(int j=1;j<=nchurn;j++) {
							addNodeUniform(j,nchurn);
						}
					}

					//If start time equals stop time it means a one time membership change
					if(singlechurn){
						churnstart = 0;
					}
				}
				//--------------------------------------------------------------------
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			cycle = cycle + 1;
		}
		
	}
	
	
	private static ArrayList<Reference> getRandomView(int viewsize, Random rnd, int id, ArrayList<Integer> aliveids,int alsize){
		ArrayList<Reference> res = new ArrayList<Reference>();
		
		for(int i=1;i<=viewsize;i++){
			int pos = rnd.nextInt(alsize);
			int sample = aliveids.get(pos);
			while(res.contains(new Reference(sample,0.0,0,null)) || sample == id){
				pos = rnd.nextInt(alsize);
				sample = aliveids.get(pos);
			}
			NodeInterface t = nodelist.get(sample);
			res.add(new Reference(sample,t.getPosition(),0,t));
		}
		
		return res;
	}

	private static void addNode(){
		lastid = lastid + 1;
		double nodeposition = new Double(lastid)/new Double(nodes);
		if(flex){
			nodelist.put(lastid, new NodeRepFlex(lastid,nodeposition,replicationfactor,replicationfactorMax,maxage,local,localinterval));
		}
		else{
			nodelist.put(lastid, new Node(lastid,nodeposition,replicationfactor,maxage,local,localinterval));
		}
		System.out.println("Node added. ID:"+lastid+" POSITION:"+nodeposition);
	}
	
	private static void addNodeUniform(int localid, int nodesadded){
		lastid = lastid + 1;
		double nodeposition = new Double(localid)/new Double(nodesadded);
		if(flex){
			nodelist.put(lastid, new NodeRepFlex(lastid,nodeposition,replicationfactor,replicationfactorMax,maxage,local,localinterval));
		}
		else{
			nodelist.put(lastid, new Node(lastid,nodeposition,replicationfactor,maxage,local,localinterval));
		}
		System.out.println("Node added. ID:"+lastid+" POSITION:"+nodeposition);
	}
	
	private static void addNodeRandomPos(){
		lastid = lastid + 1;
		double nodeposition = grnd.nextDouble();
		if(flex){
			nodelist.put(lastid, new NodeRepFlex(lastid,nodeposition,replicationfactor,replicationfactorMax,maxage,local,localinterval));
		}
		else{
			nodelist.put(lastid, new Node(lastid,nodeposition,replicationfactor,maxage,local,localinterval));
		}
		System.out.println("Node added. ID:"+lastid+" POSITION:"+nodeposition);
	}
	
	private static void removeRandomNode(ArrayList<Integer> aliveids,int alsize){
		int pos = grnd.nextInt(alsize);
		int sample = aliveids.get(pos);
		nodelist.remove(sample);
	}
	
	@SuppressWarnings("unused")
	private static void removeNode(int rpos){
		nodelist.remove(rpos);
	}
	
}
