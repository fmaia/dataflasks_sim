package core;

import java.util.ArrayList;

public class Node {

	public ArrayList<Reference> localview;
	public int id;
	public double position;
	public int ngroups;
	public int group;
	public int replicationfactor;
	
	public Node(int id,double position, int replicationfactor){
		this.id = id;
		this.position = position;
		this.ngroups = 1;
		this.group = 1;
		this.replicationfactor = replicationfactor;
		this.localview = new ArrayList<Reference>();
	}
	
	public void receiveMessage(ArrayList<Reference> received){
		
		//ADD RECEIVED
		for (Reference r : received){
			if(group(r.position)==this.group && !this.localview.contains(r) && !(r.id==this.id)){
				this.localview.add(r);
			}
		}
		
		//CLEAN VIEW
		ArrayList<Reference> torem = new ArrayList<Reference>();
		for (Reference r : this.localview){
			if(group(r.position)!=this.group){
				torem.add(r);
			}
		}
		for(Reference r : torem){
			this.localview.remove(r);
		}
		
		//SEARCH FOR VIOLATIONS
		int estimation = countEqual();
		if(this.id==1){
			System.out.println("conta actual:"+estimation);
		}
		if((estimation+1)<this.replicationfactor){
			if(this.ngroups>1){
				this.ngroups = this.ngroups/2; 
			}
		}
		if((estimation+1)>this.replicationfactor){
			this.ngroups = this.ngroups*2;
		}
		this.group = group(this.position);
		
	}
	
	private int countEqual(){
		int res = 0;
		for (Reference r : this.localview){
			if(group(r.position)==this.group){
				res = res +1;
			}
		}
		return res;
	}
	
	private int group(double peerpos){
		int temp = (int) Math.ceil((new Double(this.ngroups))*peerpos);
		if(temp == 0){
			temp = 1;
		}
		return temp;
	}
	
}
