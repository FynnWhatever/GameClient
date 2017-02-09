package flo.gameserver.client;

public class UserInput {
	String id;
	boolean pressA;
	boolean pressB;
	boolean pressStick;
	float stickX;
	float stickY;
	public UserInput(String id, boolean pressA, boolean pressB, boolean pressStick, float stickX, float stickY){
		this.id = id;
		this.pressA = pressA;
		this.pressB = pressB;
		this.pressStick = pressStick;
		this.stickX = stickX;
		this.stickY = stickY;
	}
}
