package es.reinosdeleyenda.flamethrower_lib.launcher;

public interface ReadyListener {
	public void ready(MudConnection newData);
	public void modify(MudConnection old, MudConnection newData);
}
