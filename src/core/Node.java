package core;

import java.util.ArrayList;

public class Node {

	private ArrayList<Reference> localview;
	private int id;
	public double position;
	private int ngroups;
	private int group;
	private int replicationfactor;
	private int maxage;
	
	public Node(int id,double position, int replicationfactor,int maxage){
		this.id = id;
		this.position = position;
		this.ngroups = 1;
		this.group = 1;
		this.replicationfactor = replicationfactor;
		this.localview = new ArrayList<Reference>();
		this.maxage = maxage;
	}
	
	
	
	public synchronized int getNgroups() {
		return ngroups;
	}



	public synchronized void setNgroups(int ngroups) {
		this.ngroups = ngroups;
	}



	public synchronized int getGroup() {
		return group;
	}



	public synchronized void setGroup(int group) {
		this.group = group;
	}



	public synchronized void receiveLocalMessage(ArrayList<Reference> received){
		//ADD RECEIVED
		for (Reference r : received){
			if(group(r.position)==this.group && !(r.id==this.id)){
				if(!this.localview.contains(r)){
					this.localview.add(r);
				}
				else{
					int index = this.localview.indexOf(r);
					if(this.localview.get(index).age>r.age){
						this.localview.remove(index);
						this.localview.add(r);
					}
				}
			}
		}
	}
	
	public synchronized void receiveMessage(ArrayList<Reference> received){
		
		//AGING VIEW
		for(Reference r : localview){
			r.age = r.age + 1;
		}
		
		//ADD RECEIVED
		for (Reference r : received){
			if(group(r.position)==this.group && !(r.id==this.id)){
				if(!this.localview.contains(r)){
					this.localview.add(r);
				}
				else{
					int index = this.localview.indexOf(r);
					if(this.localview.get(index).age>r.age){
						this.localview.remove(index);
						this.localview.add(r);
					}
				}
			}
		}
		
		//CLEAN VIEW
		ArrayList<Reference> torem = new ArrayList<Reference>();
		for (Reference r : this.localview){
			if(group(r.position)!=this.group){
				torem.add(r);
			}
			else{
				if(r.age>maxage){
					torem.add(r);
				}
			}
		}
		for(Reference r : torem){
			this.localview.remove(r);
		}
		
		//SEARCH FOR VIOLATIONS
		int estimation = this.localview.size(); //countEqual();
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
	
//	private int countEqual(){
//		int res = 0;
//		for (Reference r : this.localview){
//			if(group(r.position)==this.group){
//				res = res +1;
//			}
//		}
//		return res;
//	}
	
	private int group(double peerpos){
		int temp = (int) Math.ceil((new Double(this.ngroups))*peerpos);
		if(temp == 0){
			temp = 1;
		}
		return temp;
	}
	
}
