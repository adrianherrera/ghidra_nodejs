package v8_bytecode.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class ContextVarStore {

	private final long address;
	private final String name;

	public ContextVarStore(long address, final String name) {
		this.address = address;
		this.name = name;
	}

	public long getAddress() {
		return address;
	}

	public String getName() {
		return name;
	}

	void writeTo(DataOutputStream out) throws IOException {
		out.writeLong(address);
		out.writeUTF(name);
	}

	static ContextVarStore readFrom(DataInputStream in) throws IOException {
		return new ContextVarStore(in.readLong(), in.readUTF());
	}
}
