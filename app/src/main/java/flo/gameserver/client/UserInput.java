package flo.gameserver.client;

public class UserInput {
	String id;
	MainActivity1.PlayerAction action;
	int x;
	int y;
	public UserInput(String id, MainActivity1.PlayerAction action, int xvalue, int yvalue){
		this.id = id;
		this.action = action;
		x = xvalue;
		y= yvalue;
	}
}
