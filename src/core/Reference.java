package core;

public class Reference {

	public int age;
	public double position;
	public int id;
	
	public Reference(int id,double position,int age){
		this.age = age;
		this.id = id;
		this.position = position;
	}
	
	@Override
	public boolean equals(Object obj){
		if (obj instanceof Reference){
            return ((Reference) obj).id==this.id;
		}
        else
            return false;
	}
}
