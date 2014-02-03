package core;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Main {

	public static HashMap<Integer,Node> nodelist;
	public static int nodes;
	
	public static void main(String[] args) {
		
		int nargs = args.length;
		nodes = 0;
		int cycles = 0;
		int replicationfactor = 0;
		int viewsize;
		
		if (nargs<4){
			
			return;
		}
		else{
			nodes = Integer.parseInt(args[0]);
			cycles = Integer.parseInt(args[1]);
			replicationfactor = Integer.parseInt(args[2]);
			viewsize = Integer.parseInt(args[3]);
		}
		
		Random grnd2 = new Random();
		
		
		nodelist = new HashMap<Integer,Node>();
		for(int i=1;i<=nodes;i++){
			double nodeposition = new Double(i)/new Double(nodes);
			nodelist.put(i, new Node(i,nodeposition,replicationfactor));
			int gr =(int) Math.ceil(nodeposition*64.0);
			if(gr==0) gr =1;
			System.out.println("Node created. ID:"+i+" POSITION:"+nodeposition+" GROUP:"+gr);
		}

		int cycle = 1;
		while(cycle<=cycles){
			System.out.println("Cycle "+cycle);
			FileWriter fstream;
			try {
				fstream = new FileWriter(cycle+".txt");
				BufferedWriter fout = new BufferedWriter(fstream);

				for(int i=1;i<=nodes;i++){
					ArrayList<Reference> message = getRandomView(viewsize,grnd2,i);
					
					Node current = nodelist.get(i);
					current.receiveMessage(message);
					
					fout.write(i+" "+current.group+" "+current.ngroups);
					fout.write("\n");
					
					
				}
				fout.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			cycle = cycle + 1;
		}
		
	}
	
	private static ArrayList<Reference> getRandomView(int viewsize, Random rnd, int id){
		ArrayList<Reference> res = new ArrayList<Reference>();
		//String s = "";
		for(int i=1;i<=viewsize;i++){
			int sample = rnd.nextInt(nodes) + 1;
			while(res.contains(new Reference(sample,0.0,0)) || sample == id){
				sample = rnd.nextInt(nodes) + 1;
			}
			//s = s + "("+sample+ ","+nodelist.get(sample).position+") ";
			res.add(new Reference(sample,nodelist.get(sample).position,0));
		}
		//System.out.println(s);
		return res;
	}

}
