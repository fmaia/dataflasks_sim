package core;

import java.util.ArrayList;

public interface NodeInterface {


	public abstract int getNgroups();

	public abstract void setNgroups(int ngroups);

	public abstract int getGroup();

	public abstract void setGroup(int group);

	public abstract double getPosition();
	
	public abstract void receiveLocalMessage(ArrayList<Reference> received);

	public abstract void receiveMessage(ArrayList<Reference> received, int cycle);

}