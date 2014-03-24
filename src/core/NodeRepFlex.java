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

import java.util.ArrayList;

public class NodeRepFlex implements NodeInterface {

	
	private ArrayList<Reference> localview;
	private int id;
	public double position;
	private int ngroups;
	private int group;
	private int replicationfactorMax;
	private int replicationfactorMin;
	private int maxage;
	private boolean local;
	private int localinterval;
	
	public NodeRepFlex(int id,double position, int replicationfactorMin,int replicationfactorMax, int maxage, boolean local,int localinterval){
		this.id = id;
		this.position = position;
		this.ngroups = 1;
		this.group = 1;
		this.replicationfactorMin = replicationfactorMin;
		this.replicationfactorMax = replicationfactorMax;
		this.localview = new ArrayList<Reference>();
		this.maxage = maxage;
		this.local = local;
		this.localinterval = localinterval;
	}
	
	@Override
	public double getPosition(){
		return this.position;
	}
	
	@Override
	public synchronized int getNgroups() {
		return ngroups;
	}

	@Override
	public synchronized void setNgroups(int ngroups) {
		this.ngroups = ngroups;

	}

	@Override
	public synchronized int getGroup() {
		return this.group;
	}

	@Override
	public synchronized void setGroup(int group) {
		this.group = group;
	}

	private int group(double peerpos){
		int temp = (int) Math.ceil((new Double(this.ngroups))*peerpos);
		if(temp == 0){
			temp = 1;
		}
		return temp;
	}
	
	@Override
	public synchronized void receiveLocalMessage(ArrayList<Reference> received) {
		//ADD RECEIVED
		for (Reference r : received){
			if(group(r.position)==this.group && !(r.id==this.id)){
				int index = this.localview.indexOf(r);
				if(index==-1){
					this.localview.add(r);
				}
				else{
					Reference current = this.localview.get(index);
					if(current.age>r.age){
						current.age = r.age;
					}
				}
			}
		}

	}

	@Override
	public void receiveMessage(ArrayList<Reference> received, int cycle) {
		ArrayList<Reference> tosend = new ArrayList<Reference>();

		synchronized(this){
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
			if((estimation+1)<this.replicationfactorMin){
				if(this.ngroups>1){
					this.ngroups = this.ngroups/2; 
				}
			}
			if((estimation+1)>this.replicationfactorMax){
				this.ngroups = this.ngroups*2;
			}
			this.group = group(this.position);

			if(local){
				Reference myself = new Reference(this.id,this.position,0,this);
				tosend.add(myself);
				for(Reference r : this.localview){
					tosend.add((Reference)r.clone());
				}
			}

		}
		//System.out.println("message processed at "+this.id+" going to send local msgs.");
		//SEND LOCAL VIEW TO NEIGHBORS
		if(local && (cycle%this.localinterval==0)){
			for(Reference r : tosend){
				if(r.id!=this.id){
					r.noderef.receiveLocalMessage(tosend);
				}
			}
		}

	}

}
