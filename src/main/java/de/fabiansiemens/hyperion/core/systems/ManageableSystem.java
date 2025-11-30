package de.fabiansiemens.hyperion.core.systems;

public interface ManageableSystem {
	public void launch();
	public boolean isAvailable();
	public SystemAvailability getAvailabilityState();
	public void onError(ManageableSystem system, Throwable throwable);
	public void deactivate(boolean state);
	public void shutdown();
}
