package de.dosmike.sponge.zones;

public abstract class Stoppable extends Thread {
	boolean running=false;
	
	public void onStart() {}
	public abstract void onLoop();
	public void onHalted() {}
	
	@Override
	public void run() {
		running=true;
		try {
			onStart();
			while (running) {
				onLoop();
			}
		} catch (Exception e) {
			throw new RuntimeException("Stoppable was forced to stop!", e);
		} finally {
			running=false;
			try {
				onHalted();
			} catch (Exception e) {
				throw new RuntimeException("Stoppable could not properly stop", e);
			} 
		}
		
	}
	
	public void halt() { running=false; }
	public boolean isRunning() { return running; }
}
