package saere.database.profiling;

public aspect SysArch {
	
	public pointcut databasePackage() : within(saere.database.*);
	
}
