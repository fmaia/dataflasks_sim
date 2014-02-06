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

	public static HashMap<Integer,Node> nodelist;
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
	
	private static class Worker extends Thread{
		private Node p;
		private ArrayList<Reference> msg;
		public Worker(Node p, ArrayList<Reference> msg){
			this.p = p;
			this.msg = msg;
		}
		public void run(){
			p.receiveMessage(msg);
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
		
		grnd = new Random();
		nodelist = new HashMap<Integer,Node>();
		
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
					Node current = nodelist.get(i);
					Worker a = new Worker(current,message);
					threads.add(a);
					a.start();
					System.out.println("Worker Launched for id" + i + " in cycle "+cycle);
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
					Node current = nodelist.get(i);
					//Logging node state after processing message
					fout.write(i+" "+current.getGroup()+" "+current.getNgroups());
					fout.write("\n");
				}
				fout.close();
				
				//CHURN---------------------------------------------------------------
				if(churnstart!=0 && cycle>=churnstart && cycle<=churnstop){
					if(churntype.equals("remove") || churntype.equals("both")){
						for(int j=0;j<nchurn;j++) {
							removeRandomNode();
						}
					}
					if(churntype.equals("add") || churntype.equals("both")){
						for(int j=0;j<nchurn;j++) {
							addNodeRandomPos();
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
			Node t = nodelist.get(sample);
			res.add(new Reference(sample,t.position,0,t));
		}
		
		return res;
	}

	private static void addNode(){
		lastid = lastid + 1;
		double nodeposition = new Double(lastid)/new Double(nodes);
		nodelist.put(lastid, new Node(lastid,nodeposition,replicationfactor,maxage,local));
		System.out.println("Node added. ID:"+lastid+" POSITION:"+nodeposition);
	}
	
	private static void addNodeRandomPos(){
		lastid = lastid + 1;
		double nodeposition = grnd.nextDouble();
		nodelist.put(lastid, new Node(lastid,nodeposition,replicationfactor,maxage,local));
		System.out.println("Node added. ID:"+lastid+" POSITION:"+nodeposition);
	}
	
	private static void removeRandomNode(){
		int nnodes = nodelist.size();
		int rpos = grnd.nextInt(nnodes)+1;
		nodelist.remove(rpos);
	}
	
	@SuppressWarnings("unused")
	private static void removeNode(int rpos){
		nodelist.remove(rpos);
	}
	
}
