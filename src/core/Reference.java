package core;

public class Reference {

	public int age;
	public double position;
	public int id;
	public Node noderef;
	
	public Reference(int id,double position,int age,Node n){
		this.age = age;
		this.id = id;
		this.position = position;
		this.noderef =n;
	}
	
	@Override
	public boolean equals(Object obj){
		if (obj instanceof Reference){
            return ((Reference) obj).id==this.id;
		}
        else
            return false;
	}
	
	@Override
	public Object clone(){
		Reference res = new Reference(this.id,this.position,this.age,this.noderef);
		return res;
	}
}
