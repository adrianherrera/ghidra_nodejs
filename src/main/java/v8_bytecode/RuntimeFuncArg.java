package v8_bytecode;

public final class RuntimeFuncArg {
	private final String name;
	private final String type;
	
	public RuntimeFuncArg(String name, String type) {
		this.name = name;
		this.type = type;
	}
	
	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}
}
